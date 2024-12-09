package edu.unh.cs.cs619.bulletzone;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;

import java.util.ArrayList;
import java.util.List;

import edu.unh.cs.cs619.bulletzone.util.FileHelper;
import edu.unh.cs.cs619.bulletzone.util.ReplayData;
import edu.unh.cs.cs619.bulletzone.util.ReplayDataFlat;

/**
 * Made by Alec Rydeen
 *
 * Activity to act as the menu between multiple different replay options.
 * Able to pick between the 5 most recent replays, including the last played game.
 * Also has button to clear the replay data
 */
@EActivity(R.layout.activity_replay)
public class ReplayActivity extends Activity {

    private static final String TAG = "ReplayActivity";

    ReplayData replayData = ReplayData.getReplayData();
    PlayerData playerData = PlayerData.getPlayerData();

    private FileHelper fileHelper;

    private List<ReplayDataFlat> replayDataList = new ArrayList<>();

    private Intent replayIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        playerData.setContext(getApplicationContext());
        fileHelper = new FileHelper(getApplicationContext());
        replayDataList = fileHelper.loadReplayList("Replays");
        replayIntent = new Intent(this, ReplayInstanceActivity_.class);
        Log.e(TAG, "onCreate");
    }

    @Click(R.id.clearReplaysButton)
    void clearReplays() {
        Log.d(TAG, "Attempting to delete replays");
        if(fileHelper.replayFileExists("Replays")) {
            fileHelper.deleteReplayFile("Replays");
            replayDataList = new ArrayList<>();
            Log.d(TAG, "Replays Deleted");
        } else {
            Log.d(TAG, "No Replay File Found");
        }

    }

    @Click(R.id.replay0Button)
    void replay0() {
        if (!replayDataList.isEmpty()) {
            replayData.loadReplay(replayDataList.get(0));
            startActivity(replayIntent);
            finish();
        }
    }

    @Click(R.id.replay1Button)
    void replay1() {
        if (replayDataList.size() > 1) {
            replayData.loadReplay(replayDataList.get(1));
            startActivity(replayIntent);
            finish();
        }
    }

    @Click(R.id.replay2Button)
    void replay2() {
        if (replayDataList.size() > 2) {
            replayData.loadReplay(replayDataList.get(2));
            startActivity(replayIntent);
            finish();
        }
    }

    @Click(R.id.replay3Button)
    void replay3() {
        if (replayDataList.size() > 3) {
            replayData.loadReplay(replayDataList.get(3));
            startActivity(replayIntent);
            finish();
        }
    }

    @Click(R.id.replay4Button)
    void replay4() {
        if (replayDataList.size() > 4) {
            replayData.loadReplay(replayDataList.get(4));
            startActivity(replayIntent);
            finish();
        }
    }

    @Click(R.id.backToMenuButton)
    void backToMenu() {
        replayData.clearReplay();
        Intent intent = new Intent(this, MenuActivity_.class);
        startActivity(intent);
        finish();
    }
}
