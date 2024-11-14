package edu.unh.cs.cs619.bulletzone.rest;

import android.os.SystemClock;
import android.util.Log;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.rest.spring.annotations.RestService;
import org.greenrobot.eventbus.EventBus;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import edu.unh.cs.cs619.bulletzone.ClientController;
import edu.unh.cs.cs619.bulletzone.events.GameEvent;
import edu.unh.cs.cs619.bulletzone.events.GameEventProcessor;
import edu.unh.cs.cs619.bulletzone.events.ItemPickupEvent;
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
    private final Map<Integer, ItemInfo> itemTracker = new HashMap<>();

    private static class ItemInfo {
        final int x;
        final int y;
        final int value;

        ItemInfo(int x, int y, int value) {
            this.x = x;
            this.y = y;
            this.value = value;
        }

        @Override
        public String toString() {
            return String.format("Item %d at [%d,%d]", value, x, y);
        }
    }

    @Background(id = "grid_poller_task")
    public void doPoll(GameEventProcessor eventProcessor) {
        try {
            Log.d(TAG, "Starting GridPollerTask");
            currentProcessor = eventProcessor;

            // Get initial grid state
            GridWrapper grid = restClient.playerGrid();
            GridWrapper tGrid = restClient.terrainGrid();
            replayData.setInitialGrids(grid, tGrid);
            replayData.initialGridToSet = grid.getGrid();

            Log.d(TAG, "Initial grid state obtained");
            onGridUpdate(grid, tGrid);
            previousTimeStamp = grid.getTimeStamp();

            // Do initial board scan
            scanBoardForItems(grid.getGrid());

            // Set up board
            Log.d(TAG, "Setting up game board");
            eventProcessor.setBoard(grid.getGrid(), tGrid.getGrid());

            while (isRunning) {
                try {
                    grid = restClient.playerGrid();

                    // Get current items
                    Map<Integer, ItemInfo> currentItems = getCurrentItems(grid.getGrid());

                    // Check for item pickups
                    checkForPickups(currentItems);

                    // Update our tracking
                    itemTracker.clear();
                    itemTracker.putAll(currentItems);

                    onGridUpdate(grid, tGrid);
                    processEvents();

                } catch (Exception e) {
                    Log.e(TAG, "Error in polling loop", e);
                }

                SystemClock.sleep(100);
            }
        } catch (Exception e) {
            Log.e(TAG, "Fatal error in doPoll", e);
            isRunning = false;
        }
    }

    private void scanBoardForItems(int[][] board) {
        Log.d(TAG, "Starting initial board scan");
        itemTracker.clear();

        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                int value = board[i][j];
                if (isItem(value)) {
                    itemTracker.put(value, new ItemInfo(i, j, value));
                    Log.d(TAG, String.format("Initial scan found item %d at [%d,%d]", value, i, j));
                }
            }
        }

        Log.d(TAG, String.format("Initial scan complete. Found %d items", itemTracker.size()));
    }

    private Map<Integer, ItemInfo> getCurrentItems(int[][] board) {
        Map<Integer, ItemInfo> items = new HashMap<>();
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                int value = board[i][j];
                if (isItem(value)) {
                    items.put(value, new ItemInfo(i, j, value));
                }
            }
        }
        return items;
    }

    private void checkForPickups(Map<Integer, ItemInfo> currentItems) {
        // Check for items that were in our tracker but are no longer on the board
        for (Map.Entry<Integer, ItemInfo> entry : itemTracker.entrySet()) {
            int itemValue = entry.getKey();
            ItemInfo itemInfo = entry.getValue();

            if (!currentItems.containsKey(itemValue)) {
                // Item was picked up
                Log.d(TAG, String.format("Item pickup detected: %s", itemInfo));

                try {
                    int itemType = itemValue - 3000;
                    double value = calculateItemValue(itemType);

                    clientController.handleItemPickup(itemType);
                    EventBus.getDefault().post(new ItemPickupEvent(itemType, value));

                    Log.d(TAG, String.format("Posted pickup event for item type %d with value %f", itemType, value));
                } catch (Exception e) {
                    Log.e(TAG, "Error processing item pickup", e);
                }
            }
        }
    }

    private boolean isItem(int value) {
        return value >= 3000 && value <= 3003;
    }

    private double calculateItemValue(int itemType) {
        if (itemType == 1) { // Thingamajig
            return 100 + new Random().nextInt(901);
        }
        return 0.0;
    }

    private void processEvents() {
        try {
            GameEventCollectionWrapper events = restClient.events(previousTimeStamp);
            boolean haveEvents = false;

            for (GameEvent event : events.getEvents()) {
                Log.d(TAG, "Processing event: " + event);
                if (currentProcessor != null && currentProcessor.isRegistered()) {
                    EventBus.getDefault().post(event);
                    previousTimeStamp = event.getTimeStamp();
                    haveEvents = true;
                }
            }

            if (haveEvents) {
                EventBus.getDefault().post(new UpdateBoardEvent());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error processing events", e);
        }
    }

    public void stop() {
        Log.d(TAG, "Stopping GridPollerTask");
        Log.d(TAG, replayData.toString());
        isRunning = false;
        currentProcessor = null;
    }

    @UiThread
    public void onGridUpdate(GridWrapper gw, GridWrapper tw) {
        EventBus.getDefault().post(new GridUpdateEvent(gw, tw));
    }
}