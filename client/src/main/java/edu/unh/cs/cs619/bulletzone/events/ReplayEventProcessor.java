package edu.unh.cs.cs619.bulletzone.events;

import android.util.Log;

import org.androidannotations.annotations.EBean;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

/**
 * Made by Alec Rydeen
 *
 * Class to handle the events being received from a replay Instance, without re-storing them.
 */
@EBean
public class ReplayEventProcessor {
    private final String TAG = "ReplayEventProcessor";
    private int[][] playerLayer;
    private int[][] itemLayer;
    private int[][] terrainLayer;
    private boolean isRegistered = false;

    /**
     * @param newPlayerBoard initial player layer game board
     * @param newTerrainBoard initial terrain layer game board
     * Function to set the initial board states
     */
    public void setBoard(int[][] newPlayerBoard, int[][] newTerrainBoard) {
//        Log.d(TAG, "Setting Replay Event Processor");
        playerLayer = newPlayerBoard;
        terrainLayer = newTerrainBoard;
//        Log.d(TAG, "Board updated");
    }

    /**
     * Register the EventBus
     */
    public void start() {
        EventBus.getDefault().register(this);
        isRegistered = true;
    }

    /**
     * Unregister the event bus if registered
     */
    public void stop() {
        if (isRegistered) {
            try {
                EventBus.getDefault().unregister(this);
                isRegistered = false;
            } catch (IllegalArgumentException e) {
                Log.e(TAG, "Error unregistering: " + e.getMessage());
            }
        }
    }

    @Subscribe
    public void onNewEvent(GameEvent event) {
        if (playerLayer != null) {
            event.applyTo(playerLayer);
        }
    }

}
