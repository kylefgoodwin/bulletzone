package edu.unh.cs.cs619.bulletzone.events;

import android.util.Log;

import org.androidannotations.annotations.EBean;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import androidx.annotation.VisibleForTesting;

import edu.unh.cs.cs619.bulletzone.util.ReplayData;

@EBean
public class GameEventProcessor {
    private static final String TAG = "GameEventProcessor";
    private int[][] playerLayer;
    private int[][] itemLayer;
    private int[][] terrainLayer;
    ReplayData replayData = ReplayData.getReplayData();
    private boolean isRegistered = false;
    private EventBus eb = EventBus.getDefault();

    public void setBoard(int[][] newPlayerBoard, int[][] newTerrainBoard) {
        Log.d("Event Processor", "Setting Event Processor");
        playerLayer = newPlayerBoard;
        terrainLayer = newTerrainBoard;
        Log.d(TAG, "Board updated");
    }

    @VisibleForTesting
    public void setEventBus(EventBus eventBus) {
        this.eb = eventBus;
    }

    public void start() {
        if (!isRegistered) {
            Log.d(TAG, "Attempting to register with EventBus");
            eb.register(this); // Use injected EventBus
            isRegistered = true;
            Log.d(TAG, "Successfully registered with EventBus");
        } else {
            Log.d(TAG, "Already registered, skipping registration");
        }
    }

    public void stop() {
        if (isRegistered) {
            Log.d(TAG, "Attempting to unregister from EventBus");
            try {
                eb.unregister(this); // Use injected EventBus
                Log.d(TAG, "Successfully unregistered from EventBus");
            } catch (IllegalArgumentException e) {
                Log.e(TAG, "Error unregistering: " + e.getMessage());
            }
            isRegistered = false;
        } else {
            Log.d(TAG, "Not registered, skipping unregister");
        }
    }

    @Subscribe
    public void onNewEvent(GameEvent event) {
        if (playerLayer != null) {
            Log.d(TAG, "Applying " + event);
            replayData.addGameEvent(event);
            event.applyTo(playerLayer);
        } else {
            Log.w(TAG, "Board is null, cannot apply event: " + event);
        }
    }

    public boolean isRegistered() {
        return isRegistered;
    }
}