package edu.unh.cs.cs619.bulletzone;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.Spinner;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ItemSelect;
import org.androidannotations.annotations.ViewById;

import java.util.ArrayList;
import java.util.Arrays;

import edu.unh.cs.cs619.bulletzone.events.ReplayEventProcessor;
import edu.unh.cs.cs619.bulletzone.rest.GridReplayTask;
import edu.unh.cs.cs619.bulletzone.util.ReplayData;

/**
 * Made by Alec Rydeen
 *
 * Activity to act as the actual replay instance, where the replay is shown
 * Has options to play and pause the replay, as well as to adjust
 * the speed at which the replay is shown
 */
@EActivity(R.layout.activity_replay_instance)
public class ReplayInstanceActivity extends Activity {

    private static final String TAG = "ReplayInstanceActivity";

    @ViewById
    protected GridView replayGridView;
    @ViewById
    protected GridView replaytGridView;
    @ViewById
    protected Spinner speedMenu;

    @Bean
    SimBoardView simBoardView;

    @Bean
    GridReplayTask gridReplayTask;

    @Bean
    protected ReplayEventProcessor replayEventProcessor;

    ReplayData replayData = ReplayData.getReplayData();

    int replayPaused = -1;
    int replaySpeed = 0;

    private ArrayList<?> speedSelections = new ArrayList<>(Arrays.asList("1x", "2x", "3x", "4x"));

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e(TAG, "onCreate");
    }

    @AfterViews
    protected void afterViewInjection() {
        Log.d(TAG, "afterViewInjection");
        SystemClock.sleep(500);
        simBoardView.replayAttach(replayGridView, replaytGridView);
        speedMenu.setAdapter(new ArrayAdapter<>(
                this, androidx.appcompat.R.layout.support_simple_spinner_dropdown_item, speedSelections
        ));
    }

    @AfterInject
    void afterInject() {
        Log.d(TAG, "afterInject");
        replayEventProcessor.start();
        gridReplayTask.startReplay(replayEventProcessor);
    }

    @ItemSelect({R.id.speedMenu})
    protected void onPlayableSelect(boolean checked, int position){
        Log.d(TAG,"spinnerpositon = " + position);
        replaySpeed = position + 1;
        gridReplayTask.setSpeed(replaySpeed);
    }

    @Click(R.id.backToReplaysButton)
    void backToReplays() {
        Intent intent = new Intent(this, ReplayActivity_.class);
        startActivity(intent);
        finish();
    }

    @Click(R.id.playPauseButton)
    void playPause() {
        if (replayPaused == -1) {
            Log.d(TAG, "Starting Replay");
            replayPaused = 0;
            gridReplayTask.setPaused(replayPaused);
            gridReplayTask.doReplay();
        } else {
            if (replayPaused == 0) {
                Log.d(TAG, "Pausing Replay");
                replayPaused = 1;
                gridReplayTask.setPaused(replayPaused);
            } else if (replayPaused == 1) {
                Log.d(TAG, "Un-pausing Replay");
                replayPaused = 0;
                gridReplayTask.setPaused(replayPaused);
            }
        }
    }


}
