package edu.unh.cs.cs619.bulletzone.rest;

import android.os.Build;
import android.os.SystemClock;
import android.util.Log;

import androidx.annotation.RequiresApi;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.rest.spring.annotations.RestService;
import org.greenrobot.eventbus.EventBus;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import edu.unh.cs.cs619.bulletzone.ClientController;
import edu.unh.cs.cs619.bulletzone.PlayerData;
import edu.unh.cs.cs619.bulletzone.TankEventController;
import edu.unh.cs.cs619.bulletzone.PlayerData;
import edu.unh.cs.cs619.bulletzone.events.GameEvent;
import edu.unh.cs.cs619.bulletzone.events.GameEventProcessor;
import edu.unh.cs.cs619.bulletzone.events.ItemPickupEvent;
import edu.unh.cs.cs619.bulletzone.events.MiningCreditsEvent;
import edu.unh.cs.cs619.bulletzone.events.UpdateBoardEvent;
import edu.unh.cs.cs619.bulletzone.util.GameEventCollectionWrapper;
import edu.unh.cs.cs619.bulletzone.util.GridWrapper;
import edu.unh.cs.cs619.bulletzone.util.ReplayData;

@EBean
public class GridPollerTask {
    private static final String TAG = "GridPollerTask";

    @RestService
    BulletZoneRestClient restClient;

    @Bean
    ClientController clientController;

    @Bean
    TankEventController tankEventController;

    PlayerData playerData = PlayerData.getPlayerData();

    ReplayData replayData = ReplayData.getReplayData();

    private long previousTimeStamp = -1;
    private long lastUpdateTime = System.currentTimeMillis();
    private GameEventProcessor currentProcessor = null;
    private boolean isRunning = true;
    private long userId = playerData.getUserId();
    private long playableId = playerData.getTankId();
    double miningFacilityCount = 0;

    private static class ItemLocation {
        final int itemType;
        final int x;
        final int y;

        ItemLocation(int itemType, int x, int y) {
            this.itemType = itemType;
            this.x = x;
            this.y = y;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ItemLocation that = (ItemLocation) o;
            return itemType == that.itemType && x == that.x && y == that.y;
        }

        @Override
        public int hashCode() {
            return Objects.hash(itemType, x, y);
        }

        @Override
        public String toString() {
            return String.format("Item %d at (%d,%d)", itemType - 3000, x, y);
        }
    }

    private final Set<ItemLocation> itemsPresent = new HashSet<>();
    private final Set<ItemLocation> processedItemPickups = new HashSet<>();
    Map<ItemLocation, Long> miningFacilityOwners = new HashMap<>();
    private Map<Long, Long> lastMiningTimestampMap = new HashMap<>();

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Background(id = "grid_poller_task")
    public void doPoll(GameEventProcessor eventProcessor) {
        replayData.initialGridToSet = restClient.playerGrid().getGrid();
        try {
            Log.d(TAG, "Starting GridPollerTask");
            currentProcessor = eventProcessor;

            // Get initial grid state
            GridWrapper grid = restClient.playerGrid();
            GridWrapper tGrid = restClient.terrainGrid();
            replayData.setInitialGrids(grid, tGrid);
            onGridUpdate(grid, tGrid);
            previousTimeStamp = grid.getTimeStamp();

            eventProcessor.setBoard(grid.getGrid(), tGrid.getGrid());

            while (isRunning) {
                try {
                    grid = restClient.playerGrid();
                    Set<ItemLocation> currentItems = new HashSet<>();
                    Map<Long, Boolean> userHasFacility = new HashMap<>();
                    int[][] boardState = grid.getGrid();

                    // Update tank stats periodically (every 1 second)
                    long currentTime = System.currentTimeMillis();
                    if (currentTime - lastUpdateTime >= 1000) {
                        updatePlayerStats(boardState);
                        lastUpdateTime = currentTime;
                    }

                    // Scan board for items and track their locations
                    for (int i = 0; i < boardState.length; i++) {
                        for (int j = 0; j < boardState[i].length; j++) {
                            int value = boardState[i][j];
                            // Store actual board values (3001-3005)
                            if (value >= 3001 && value <= 3005) {
                                currentItems.add(new ItemLocation(value, i, j));
                            }
                            // Check for mining facility
                            if (value == 920) {
                                ItemLocation facility = new ItemLocation(value, i, j);

                                // Determine the owner of the facility
                                Long ownerIdBoxed = miningFacilityOwners.get(facility);

                                if (ownerIdBoxed == null) {
                                    long newOwnerId = playerData.getUserId(); // Assign userId from playerData if new facility
                                    miningFacilityOwners.put(facility, newOwnerId);
                                    ownerIdBoxed = newOwnerId;
                                }

                                long ownerId = ownerIdBoxed; // Safely unbox after null check
                                userHasFacility.put(ownerId, true); // Mark that the user owns this facility

                            }
                            // Check for mining facility
                            if (value == 930) {
                                ItemLocation factory = new ItemLocation(value, i, j);

                            }
                        }
                    }

                    // Process mining facility ownership
                    for (Map.Entry<ItemLocation, Long> entry : miningFacilityOwners.entrySet()) {
                        ItemLocation facility = entry.getKey();
                        long ownerId = entry.getValue();

                        // Check if the facility is still on the board
                        if (!Boolean.TRUE.equals(userHasFacility.get(ownerId))) {
                            Log.d(TAG, "User " + ownerId + " has no mining facilities left on the board. Skipping credit.");
                            continue;
                        }

                        if (lastMiningTimestampMap.containsKey(ownerId)) {
                            long lastAwardTime = lastMiningTimestampMap.get(ownerId);
                            if (currentTime - lastAwardTime < 1000) {
                                // If less than 1 second has passed, skip awarding credits
                                Log.d(TAG, "Skipping mining credit for user " + ownerId + " due to rate limit.");
                                continue;
                            }
                        }

                        // Add credits for the user who owns this facility
                        tankEventController.addCredits(ownerId, 1.0); // Add 1 credit
                        EventBus.getDefault().post(new MiningCreditsEvent(1, ownerId, 1.0));
                        Log.d(TAG, "Added credits for user " + ownerId + " for facility at " + facility);

                        // Update the timestamp for this user to the current time
                        lastMiningTimestampMap.put(ownerId, currentTime);
                    }

                    // Check for disappeared items (picked up)
                    for (ItemLocation item : itemsPresent) {
                        if (!currentItems.contains(item)) {
                            if (!processedItemPickups.contains(item)) {
                                // Convert to 1-5 range for handling
                                int itemType = item.itemType - 3000;
                                Log.d(TAG, String.format("Item pickup detected: type %d at position (%d,%d)",
                                        itemType, item.x, item.y));

                                if (itemType >= 1 && itemType <= 5) {
                                    clientController.handleItemPickup(itemType);
                                    processedItemPickups.add(item);
                                }

                                processedItemPickups.removeIf(processed ->
                                        !itemsPresent.contains(processed) &&
                                                !processed.equals(item));
                            }
                            break;
                        }
                    }

                    itemsPresent.clear();
                    itemsPresent.addAll(currentItems);
                    onGridUpdate(grid, tGrid);

                    // Process events
                    GameEventCollectionWrapper events = restClient.events(previousTimeStamp);
                    boolean haveEvents = false;

                    for (GameEvent event : events.getEvents()) {
                        if (currentProcessor != null && currentProcessor.isRegistered()) {
                            Log.d(TAG, "Posting: " + event.toString());
                            EventBus.getDefault().post(event);
                            previousTimeStamp = event.getTimeStamp();
                            haveEvents = true;
                        }
                    }

                    if (haveEvents) {
                        EventBus.getDefault().post(new UpdateBoardEvent());
                    }

                } catch (Exception e) {
                    Log.e(TAG, "Error in polling", e);
                }

                SystemClock.sleep(100);
            }
        } catch (Exception e) {
            Log.e(TAG, "Unexpected error in doPoll", e);
        }
    }

    private void updatePlayerStats(int[][] boardState) {
        try {
            // Find our tank based on raw server value pattern (10xxxxxx where x is tank ID)
            int tankId = -1;
            for (int i = 0; i < boardState.length && tankId == -1; i++) {
                for (int j = 0; j < boardState[i].length; j++) {
                    int value = boardState[i][j];
                    if (value >= 10000000 && value < 20000000) {
                        tankId = (value - 10000000) / 10000;
                        break;
                    }
                }
            }

            if (tankId != -1) {
                playerData.setTankId(tankId);
                int oldLife = playerData.getTankLife();

                // Check if repair kit should heal
                if (playerData.getRepairKitCount() > 0) {
                    long currentTime = System.currentTimeMillis();
                    if (currentTime < playerData.getRepairKitExpiration()) {
                        // If health isn't full, send request to server to get updated health
                        if (oldLife < 100) {
                            clientController.getLifeAsync(tankId, 0);
                            Log.d(TAG, "Repair kit active, requesting health update. Current health: " + oldLife);
                        }
                    } else {
                        // Repair kit expired
                        playerData.decrementPowerUps(5);
                        Log.d(TAG, "Repair kit expired and removed");
                    }
                } else {
                    // Normal health update when no repair kit
                    clientController.getLifeAsync(tankId, 0);
                }

                // Update builder and soldier health as needed
                if (playerData.getBuilderNumber() > 0) {
                    clientController.getLifeAsync(tankId, 1);
                }

                if (playerData.getSoldierEjected()) {
                    clientController.getLifeAsync(tankId, 2);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error updating player stats: " + e.getMessage());
        }
    }

    public void stop() {
        Log.d(TAG, "Stopping GridPollerTask");
        isRunning = false;
        currentProcessor = null;
        processedItemPickups.clear();
        itemsPresent.clear();
    }

    @UiThread
    public void onGridUpdate(GridWrapper gw, GridWrapper tw) {
        EventBus.getDefault().post(new GridUpdateEvent(gw, tw));
    }
}