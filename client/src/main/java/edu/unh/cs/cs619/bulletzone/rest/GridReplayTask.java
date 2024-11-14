package edu.unh.cs.cs619.bulletzone.rest;

import android.util.Log;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.UiThread;
import org.greenrobot.eventbus.EventBus;

import edu.unh.cs.cs619.bulletzone.events.GameEvent;
import edu.unh.cs.cs619.bulletzone.events.ReplayEventProcessor;
import edu.unh.cs.cs619.bulletzone.events.UpdateBoardEvent;
import edu.unh.cs.cs619.bulletzone.util.GridWrapper;
import edu.unh.cs.cs619.bulletzone.util.ReplayData;

@EBean
public class GridReplayTask {
    private static final String TAG = "GridReplayTask";

    private ReplayEventProcessor currentProcessor;
    ReplayData replayData = ReplayData.getReplayData();
    private int replayIndex = 0;
    private long diffStamp = 0; //Used to calculate the difference between time stamps
    private int speed = 1;
    private int paused = 0;

    public void startReplay(ReplayEventProcessor eventProcessor) {
        currentProcessor = eventProcessor;

        GridWrapper grid = replayData.getInitialGrid();
        grid.setGrid(replayData.initialGridToSet);
        GridWrapper tGrid = replayData.getInitialTerrainGrid();
        onGridUpdate(grid, tGrid);

        eventProcessor.setBoard(grid.getGrid(), tGrid.getGrid());
    }

    public void setPaused(int value) {
        paused = value;
    }

    public void setSpeed(int value) {
        speed = value;
    }

    @Background(id = "grid_replay_task")
    public void doReplay() {
        try {
            Log.d(TAG, "Stating GridReplayTask");

            while (replayData.getEventAt(replayIndex) != null) {

                while (paused == 1);

                GameEvent currEvent = replayData.getEventAt(replayIndex);

//                Log.d(TAG, "DiffStamp: " + diffStamp);
//                Log.d(TAG, "Curr Event Delta: " + currEvent.getDeltaTimeStamp());
                long waitForMillis = (currEvent.getDeltaTimeStamp() - diffStamp) / speed;
                diffStamp = currEvent.getDeltaTimeStamp();
//                Log.d(TAG, "Waiting for: " + waitForMillis + " Milliseconds");

                Thread.sleep(waitForMillis);

//                Log.d(TAG, "Posting " + currEvent.getClass() + "Event");
                EventBus.getDefault().post(currEvent);
                EventBus.getDefault().post(new UpdateBoardEvent());
                Log.d(TAG, currEvent.toString());
                replayIndex++;
            }
//            Log.d(TAG, "Exited While Loop");
        } catch (Exception exe) {
            Log.e(TAG, "Unexpected error in doReplay", exe);
        }
    }

    @UiThread
    public void onGridUpdate(GridWrapper gw, GridWrapper tw) {
        EventBus.getDefault().post(new GridUpdateEvent(gw, tw));
    }
}
