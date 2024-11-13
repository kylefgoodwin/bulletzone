package edu.unh.cs.cs619.bulletzone;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.NonConfigurationInstance;

import java.util.ArrayList;

import edu.unh.cs.cs619.bulletzone.events.Database.EventDatabaseHandler;
import edu.unh.cs.cs619.bulletzone.rest.GridPollerTask;
import edu.unh.cs.cs619.bulletzone.util.FileHelper;
import edu.unh.cs.cs619.bulletzone.util.ReplayData;

/**
 * Made by Alec Rydeen
 * Activity that acts as an intermediary between logging in and joining the game.
 * Takes the join game responsibility away from the ClientActivity, and moves it between here and
 * MenuController.
 */

@EActivity(R.layout.activity_menu)
public class MenuActivity extends Activity {

    private static final String TAG = "MenuActivity";

    private long userId = -1;
    private long tankId = -1;

    @Bean
    MenuController menuController;

    private FileHelper fileHelper;

    private ReplayData replayData = ReplayData.getReplayData();

    PlayerData playerData = PlayerData.getPlayerData();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fileHelper = new FileHelper(this);
        Log.e(TAG, "onCreate");
    }

    /**
     * After the view is injected, get the USER_ID passed from authentication activity
     */
    @AfterViews
    protected void afterViewInjection() {
        Log.d(TAG, "afterViewInjection");
        userId = playerData.getUserId();
//        replayData.setEventDatabaseHandler(databaseHandler);
    }

    /**
     * Join the game using the same functionality from the ClientActivity, joining, create the
     * new Intent, and pass USER_ID and TANK_ID to it, and start the ClientActivity
     */
    @Click(R.id.joinButton)
    @Background
    void join() {
        try {
            tankId = menuController.joinAsync();
            // Start the Client activity
            Intent intent = new Intent(this, ClientActivity_.class);
            playerData.setTankId(tankId);
//            Log.d("MenuActivity", "Starting ClientActivity_");
            startActivity(intent);
//            Log.d("MenuActivity", "ClientActivity_ started");
            finish();
        } catch (Exception e) {
//            Log.e(TAG, "Error joining game", e);
        }
    }

    @Click(R.id.replayButton)
    @Background
    void replays() {
        try {
            Intent intent = new Intent(this, ReplayActivity_.class);
            startActivity(intent);
            finish();
        } catch (Exception e) {

        }

    }

    public void joinTest() {
        this.join();
    }

    public void setMenuController(MenuController menuController) {
        this.menuController = menuController;
    }
}
