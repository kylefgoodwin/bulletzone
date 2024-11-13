package edu.unh.cs.cs619.bulletzone.rest;

import android.content.ClipData;
import android.os.SystemClock;
import android.util.Log;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.rest.spring.annotations.RestService;
import org.greenrobot.eventbus.EventBus;

import java.util.HashSet;
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
    private boolean updateUsingEvents = false;
    private GameEventProcessor currentProcessor = null;
    private boolean isRunning = true;
    private Set<Integer> itemsPresent = new HashSet<>();
    private Integer lastRemovedItem = null;

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

            // Set up board but DON'T start the processor
            eventProcessor.setBoard(grid.getGrid(), tGrid.getGrid());

            while (isRunning) {
                try {
                    grid = restClient.playerGrid();
                    Set<Integer> currentItems = new HashSet<>();
                    int[][] boardState = grid.getGrid();

                    // Scan board for items
                    for (int i = 0; i < boardState.length; i++) {
                        for (int j = 0; j < boardState[i].length; j++) {
                            if (boardState[i][j] >= 3000 && boardState[i][j] <= 3003) {
                                currentItems.add(boardState[i][j]);
                            }
                        }
                    }

                    // Check for disappeared items (picked up)
                    for (Integer item : itemsPresent) {
                        if (!currentItems.contains(item) && item >= 3000 && item <= 3003) {
                            // Item was picked up
                            Log.d(TAG, "Item pickup detected: " + (item - 3000));
                            clientController.handleItemPickup(item - 3000);
                            EventBus.getDefault().post(new ItemPickupEvent(item - 3000, 0.0));
                        }
                    }

                    itemsPresent = currentItems;
                    onGridUpdate(grid, tGrid);

                    // Process events
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
    }

    @UiThread
    public void onGridUpdate(GridWrapper gw, GridWrapper tw) {
        EventBus.getDefault().post(new GridUpdateEvent(gw, tw));
    }
}