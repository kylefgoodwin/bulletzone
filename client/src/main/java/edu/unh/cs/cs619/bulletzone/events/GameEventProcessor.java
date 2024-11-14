package edu.unh.cs.cs619.bulletzone.events;

import android.util.Log;

import androidx.annotation.VisibleForTesting;

import org.androidannotations.annotations.EBean;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import edu.unh.cs.cs619.bulletzone.util.ReplayData;

@EBean
public class GameEventProcessor {
    private static final String TAG = "GameEventProcessor";
    private final Object boardLock = new Object();
    private volatile int[][] playerLayer;
    private volatile int[][] terrainLayer;
    private boolean isRegistered = false;
    private EventBus eb = EventBus.getDefault();
    private ReplayData replayData = ReplayData.getReplayData();
    private long lastEventTimestamp = 0;

    public void setBoard(int[][] newPlayerBoard, int[][] newTerrainBoard) {
        synchronized (boardLock) {
            // Initialize arrays if needed
            if (playerLayer == null || playerLayer.length != 16) {
                playerLayer = new int[16][16];
            }
            if (terrainLayer == null || terrainLayer.length != 16) {
                terrainLayer = new int[16][16];
            }

            // Copy data if provided
            if (newPlayerBoard != null) {
                for (int i = 0; i < 16; i++) {
                    for (int j = 0; j < 16; j++) {
                        playerLayer[i][j] = newPlayerBoard[i][j];
                    }
                }
            }

            if (newTerrainBoard != null) {
                for (int i = 0; i < 16; i++) {
                    for (int j = 0; j < 16; j++) {
                        terrainLayer[i][j] = newTerrainBoard[i][j];
                    }
                }
            }

            Log.d(TAG, "Board updated successfully");
        }
    }

    @VisibleForTesting
    public void setEventBus(EventBus eventBus) {
        if (eventBus != null) {
            this.eb = eventBus;
        }
    }

    public void start() {
        if (!isRegistered) {
            Log.d(TAG, "Attempting to register with EventBus");
            eb.register(this);
            isRegistered = true;
            Log.d(TAG, "Successfully registered with EventBus");
        }
    }

    public void stop() {
        if (isRegistered) {
            try {
                eb.unregister(this);
                isRegistered = false;
            } catch (IllegalArgumentException e) {
                Log.e(TAG, "Error unregistering: " + e.getMessage());
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onNewEvent(GameEvent event) {
        if (event == null) {
            Log.w(TAG, "Received null event");
            return;
        }

        synchronized (boardLock) {
            if (playerLayer == null) {
                playerLayer = new int[16][16];
            }

            // Check for duplicate events
            if (event.getTimeStamp() <= lastEventTimestamp) {
                Log.d(TAG, "Skipping duplicate event");
                return;
            }
            lastEventTimestamp = event.getTimeStamp();

            try {
                event.applyTo(playerLayer);
                replayData.addGameEvent(event);
                Log.d(TAG, "Successfully applied event: " + event);
            } catch (Exception e) {
                Log.e(TAG, "Error applying event: " + e.getMessage(), e);
            }
        }
    }

    public boolean hasBoard() {
        synchronized (boardLock) {
            return playerLayer != null && terrainLayer != null;
        }
    }

    public boolean isRegistered() {
        return isRegistered;
    }

    public int[][] getPlayerLayer() {
        synchronized (boardLock) {
            if (playerLayer == null) {
                playerLayer = new int[16][16];
            }
            int[][] copy = new int[16][16];
            for (int i = 0; i < 16; i++) {
                System.arraycopy(playerLayer[i], 0, copy[i], 0, 16);
            }
            return copy;
        }
    }

    public int[][] getTerrainLayer() {
        synchronized (boardLock) {
            if (terrainLayer == null) {
                terrainLayer = new int[16][16];
            }
            int[][] copy = new int[16][16];
            for (int i = 0; i < 16; i++) {
                System.arraycopy(terrainLayer[i], 0, copy[i], 0, 16);
            }
            return copy;
        }
    }
}