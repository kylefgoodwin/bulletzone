package edu.unh.cs.cs619.bulletzone.events;

import android.media.MediaPlayer;
import android.util.Log;

import org.androidannotations.annotations.EBean;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import edu.unh.cs.cs619.bulletzone.PlayerData;
import edu.unh.cs.cs619.bulletzone.R;

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
    private PlayerData playerData = PlayerData.getPlayerData();
    private MediaPlayer mediaPlayer = MediaPlayer.create(playerData.getContext(), R.raw.goblin_hit);

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
        if (event instanceof HitEvent) {
            mediaPlayer.start();
        }
        if (playerLayer != null) {
            event.applyTo(playerLayer);
        }
    }

}
