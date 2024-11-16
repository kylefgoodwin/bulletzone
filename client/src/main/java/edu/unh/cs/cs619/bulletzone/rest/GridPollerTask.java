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

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import edu.unh.cs.cs619.bulletzone.ClientController;
import edu.unh.cs.cs619.bulletzone.events.GameEvent;
import edu.unh.cs.cs619.bulletzone.events.GameEventProcessor;
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

    ReplayData replayData = ReplayData.getReplayData();

    private long previousTimeStamp = -1;
    private GameEventProcessor currentProcessor = null;
    private boolean isRunning = true;

    // Custom class to track items with their positions
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
            return String.format("Item %d at (%d,%d)", itemType, x, y);
        }
    }

    private final Set<ItemLocation> itemsPresent = new HashSet<>();
    private final Set<ItemLocation> processedItemPickups = new HashSet<>();

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
//            Log.d(TAG, replayData.toString());
            onGridUpdate(grid, tGrid);
            previousTimeStamp = grid.getTimeStamp();

            eventProcessor.setBoard(grid.getGrid(), tGrid.getGrid());

            while (isRunning) {
                try {
                    grid = restClient.playerGrid();
                    Set<ItemLocation> currentItems = new HashSet<>();
                    int[][] boardState = grid.getGrid();

                    // Scan board for items and track their locations
                    for (int i = 0; i < boardState.length; i++) {
                        for (int j = 0; j < boardState[i].length; j++) {
                            int value = boardState[i][j];
                            if (value >= 3000 && value <= 3003) {
                                currentItems.add(new ItemLocation(value, i, j));
                            }
                        }
                    }

                    // Check for disappeared items (picked up)
                    for (ItemLocation item : itemsPresent) {
                        if (!currentItems.contains(item) &&
                                item.itemType >= 3000 &&
                                item.itemType <= 3003) {

                            // Check if this exact item (type and location) was processed
                            if (!processedItemPickups.contains(item)) {
                                Log.d(TAG, String.format("Item pickup detected: type %d at position (%d,%d)",
                                        (item.itemType - 3000), item.x, item.y));

                                clientController.handleItemPickup(item.itemType - 3000);
                                processedItemPickups.add(item);

                                // Clean up old processed items that are no longer on the board
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

    public void stop() {
        Log.d(TAG, "Stopping GridPollerTask");
//        Log.d(TAG, replayData.toString());
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