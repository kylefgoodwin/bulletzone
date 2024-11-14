package edu.unh.cs.cs619.bulletzone.events;

import android.util.Log;

import org.androidannotations.annotations.EBean;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

@EBean
public class ReplayEventProcessor {
    private final String TAG = "ReplayEventProcessor";
    private int[][] playerLayer;
    private int[][] itemLayer;
    private int[][] terrainLayer;

    public void setBoard(int[][] newPlayerBoard, int[][] newTerrainBoard) {
        Log.d(TAG, "Setting Replay Event Processor");
        playerLayer = newPlayerBoard;
        terrainLayer = newTerrainBoard;
        Log.d(TAG, "Board updated");
    }

    public void start() {
        EventBus.getDefault().register(this);
    }

    @Subscribe
    public void onNewEvent(GameEvent event) {
        if (playerLayer != null) {
            event.applyTo(playerLayer);
        }
    }

}
