package edu.unh.cs.cs619.bulletzone;

import static java.lang.Thread.sleep;
import static java.sql.Types.NULL;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Spinner;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import org.androidannotations.annotations.*;
import org.androidannotations.api.BackgroundExecutor;

import edu.unh.cs.cs619.bulletzone.events.GameEventProcessor;
import edu.unh.cs.cs619.bulletzone.events.HitEvent;
import edu.unh.cs.cs619.bulletzone.events.ItemPickupEvent;
import edu.unh.cs.cs619.bulletzone.events.PowerUpEjectEvent;
import edu.unh.cs.cs619.bulletzone.rest.BZRestErrorhandler;
import edu.unh.cs.cs619.bulletzone.rest.GridPollerTask;
import edu.unh.cs.cs619.bulletzone.util.ClientActivityShakeDriver;
import edu.unh.cs.cs619.bulletzone.util.ReplayData;

import androidx.annotation.VisibleForTesting;

import com.skydoves.progressview.ProgressView;

import java.util.ArrayList;
import java.util.Arrays;

@EActivity(R.layout.activity_client)
public class ClientActivity extends Activity {

    private static final String TAG = "ClientActivity";

    @Bean
    protected GameEventProcessor eventProcessor;

    @ViewById
    protected ProgressView tankHealthBar;

    @ViewById ProgressView soldierHealthBar;

    @ViewById
    protected ProgressView builderHealthBar;

    @ViewById
    protected GridView gridView;

    @ViewById
    protected GridView tGridView;

    @ViewById
    protected TextView userIdTextView;

    @ViewById
    protected TextView balanceTextView;

    @ViewById
    protected TextView statusTextView;

    @ViewById
    protected TextView eventBusStatus;

    @ViewById
    protected TextView activeEffects;

    @ViewById
    protected TextView itemInfoText;

    @ViewById
    protected TextView powerUpLabel;

    @ViewById
    protected TextView movementSpeedText;

    @ViewById
    protected TextView fireRateText;

    @ViewById
    protected Spinner selectPlayable;

    @ViewById
    protected Spinner selectImprovement;

    @NonConfigurationInstance
    @Bean
    GridPollerTask gridPollTask;

    @Bean
    BZRestErrorhandler bzRestErrorhandler;

    @Bean
    TankEventController tankEventController;

    @Bean
    ClientController clientController;

    @Bean
    SimBoardView simBoardView;

    ClientActivityShakeDriver shakeDriver;

    PlayerData playerData = PlayerData.getPlayerData();

    ReplayData replayData = ReplayData.getReplayData();

    private long playableId = -1;
    private int playableType = 1;
    private int improvementType = 1;
    private long userId = -1;
    private ArrayList<?> playableSelections = new ArrayList<>(Arrays.asList("Tank", "Builder", "Soldier"));
    private ArrayList<String> improvementSelections = new ArrayList<>(Arrays.asList("destructibleWall", "indestructibleWall", "miningFacility"));

    // For testing purposes only
    @VisibleForTesting
    public void setTankEventController(TankEventController controller) {
        this.tankEventController = controller;
    }

    // For testing purposes only
    @VisibleForTesting
    public void moveTest(View view) {
        onButtonMove(view);
    }

    // For testing purposes only
    @VisibleForTesting
    public void fireTest() {
        onButtonFire();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);

        replayData.setInitialTimeStamp(System.currentTimeMillis());

        shakeDriver = new ClientActivityShakeDriver(this, new ClientActivityShakeDriver.OnShakeListener() {
            @Override
            public void onShake() {
                onButtonFire();
            }
        });

        Log.e(TAG, "onCreate");
    }

    @Override
    protected void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
        Log.d(TAG, "onDestroy called");

        gridPollTask.stop();
        BackgroundExecutor.cancelAll("grid_poller_task", true);

        simBoardView.detach();
        if (eventProcessor != null) {
            eventProcessor.stop();
        }

        shakeDriver.stop();
    }

    @AfterViews
    protected void afterViewInjection() {
        Log.d(TAG, "afterViewInjection called");
        userId = playerData.getUserId();
        playableId = playerData.getTankId();
        if (userId != -1) {
            userIdTextView.setText("User ID: " + userId);
            fetchAndUpdateBalance();
        } else {
            userIdTextView.setText("User ID: Not logged in");
            updateBalanceUI(null);
        }

        // Initialize stat displays with base values
        playerData.resetPowerUps(); // Ensure we start with base values
        updateStatsDisplay();

        SystemClock.sleep(500);
        selectImprovement.setAdapter(new ArrayAdapter<>(this, androidx.appcompat.R.layout.support_simple_spinner_dropdown_item, improvementSelections));
        selectPlayable.setAdapter(new ArrayAdapter<>(this, androidx.appcompat.R.layout.support_simple_spinner_dropdown_item, playableSelections));
        simBoardView.attach(gridView, tGridView, playableId);
    }

    @Background
    void fetchAndUpdateBalance() {
        try {
            Log.d(TAG, "Fetching balance for userId: " + userId);
            Double balance = clientController.getBalance(userId);
            Log.d(TAG, "Received balance: " + balance);
            updateBalanceUI(balance);
        } catch (Exception e) {
            Log.e(TAG, "Error fetching balance for userId: " + userId, e);
            updateBalanceUI(null);
        }
    }

    @UiThread
    void showPowerUpMessage(String message) {
        if (itemInfoText != null) {
            itemInfoText.setText(message);
            // Clear message after delay
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                if (itemInfoText != null) {
                    itemInfoText.setText("");
                }
            }, 3000);
        }
    }

    @UiThread
    void updateBalanceUI(Double balance) {
        if (balanceTextView != null) {
            if (balance != null) {
                balanceTextView.setText(String.format("Balance: $%.2f", balance));
            } else {
                balanceTextView.setText("Balance: Unavailable");
            }
        }
    }

    @UiThread
    protected void updateStatsDisplay() {
        Log.d(TAG, "Updating stats display - Move: " + playerData.getMoveInterval() + "ms, Fire: " + playerData.getFireInterval() + "ms");

        if (movementSpeedText != null) {
            movementSpeedText.setText("Movement Speed: " + playerData.getMoveInterval() + "ms");
        }
        if (fireRateText != null) {
            fireRateText.setText("Fire Rate: " + playerData.getFireInterval() + "ms");
        }

        if (activeEffects != null) {
            StringBuilder effects = new StringBuilder();
            int powerUps = playerData.getActivePowerUps();

            Log.d(TAG, "Active power-ups: " + powerUps);

            if (powerUps > 0) {
                if (playerData.getMoveInterval() < 500) {
                    effects.append("• Anti-Grav Speed Boost\n");
                    effects.append("  Movement: " + playerData.getMoveInterval() + "ms\n");
                }
                if (playerData.getFireInterval() < 1500) {
                    effects.append("• Fusion Reactor Fire Boost\n");
                    effects.append("  Fire Rate: " + playerData.getFireInterval() + "ms\n");
                }
                activeEffects.setText(effects.toString().trim());
            } else {
                activeEffects.setText("None");
            }

            Log.d(TAG, "Active effects text set to: " + effects.toString());
        }
    }

    @AfterInject
    void afterInject() {
        Log.d(TAG, "afterInject");
        clientController.setErrorHandler(bzRestErrorhandler);
        eventProcessor.start();
        gridPollTask.doPoll(eventProcessor);
    }

    @ItemSelect({R.id.selectPlayable})
    protected void onPlayableSelect(boolean checked, int position){
        Log.d(TAG,"spinnerpositon = " + position);
        playableType = position+1;
    }

    @ItemSelect({R.id.selectImprovement})
    protected void onBuildSelect(boolean checked, int position){
        Log.d(TAG,"spinnerpositon = " + position);
        improvementType = position+1;
    }

    @Click({R.id.buttonUp, R.id.buttonDown, R.id.buttonLeft, R.id.buttonRight})
    protected void onButtonMove(View view) {
        final int viewId = view.getId();
        byte direction = 0;
        switch (viewId) {
            case R.id.buttonUp:
                direction = 0;
                break;
            case R.id.buttonDown:
                direction = 4;
                break;
            case R.id.buttonLeft:
                direction = 6;
                break;
            case R.id.buttonRight:
                direction = 2;
                break;
        }
        tankEventController.turnOrMove(viewId, playableId, playableType, direction);
    }

    @Click(R.id.buttonFire)
    protected void onButtonFire() {
        tankEventController.fire(playableId, playableType);
    }

    @Click(R.id.buttonBuildOrDismantle)
    protected void onButtonBuild() {
        if (improvementType >= 0 && improvementType < improvementSelections.size()) {
            tankEventController.buildAsync(playableId, playableType, playerData.getImprovement(improvementType));
        } else {
            // Handle the case where improvementType is out of bounds
            Log.e("onButtonBuild", "Invalid improvement type index: " + improvementType);
        }
    }

    @Click(R.id.buttonEjectSoldier)
    protected void onButtonEjectSoldier() {
        clientController.ejectSoldierAsync(playableId);
    }

    @Click(R.id.buttonLeave)
    void leaveGame() {
        Log.d(TAG, "leaveGame() called, tank ID: " + playableId);
        BackgroundExecutor.cancelAll("grid_poller_task", true);
        clientController.leaveGameAsync(playableId);
        leaveUI();
    }

    @Click(R.id.buttonLogin)
    void login() {
        Intent intent = new Intent(this, AuthenticateActivity_.class);
        startActivity(intent);
    }

    @Click(R.id.buttonLogout)
    void logout() {
        Log.d(TAG, "logout() called");
        logoutUI();
    }

    @UiThread
    void leaveUI() {
        Log.d(TAG, "leaveUI() called");
        Intent intent = new Intent(this, MenuActivity_.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @UiThread
    void logoutUI() {
        Log.d(TAG, "logoutUI() called");
        userId = -1;
        if (userIdTextView != null) {
            userIdTextView.setText("User ID: Not logged in");
        }
        Intent intent = new Intent(this, AuthenticateActivity_.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Click(R.id.buttonEject)
    protected void onButtonEject(){
        clientController.ejectPowerUpAsync(playableId);
    }

    @UiThread
    void updatePowerUpStatus(int powerUpType) {
        StringBuilder status = new StringBuilder();
        boolean hasActivePowerUps = false;

        // Update stats based on power-up type
        if (powerUpType == 2) { // AntiGrav
            hasActivePowerUps = true;

            // Calculate new values
            int newMoveInterval = playerData.getMoveInterval() / 2;
            int newFireInterval = playerData.getFireInterval() + 100;

            // Update status display
            status.append("• Anti-Grav:\n");
            status.append("  - 2x Movement Speed\n");
            status.append("  - +0.1s Fire Delay");

            // Update player data
            playerData.setMoveInterval(newMoveInterval);
            playerData.setFireInterval(newFireInterval);

            if (itemInfoText != null) {
                itemInfoText.setText("Anti-Grav acquired! Speed up, fire slower");
            }

        } else if (powerUpType == 3) { // FusionReactor
            hasActivePowerUps = true;

            // Calculate new values
            int newFireInterval = playerData.getFireInterval() / 2;
            int newMoveInterval = playerData.getMoveInterval() + 100;

            // Update status display
            status.append("• Fusion Reactor:\n");
            status.append("  - 2x Fire Rate\n");
            status.append("  - +0.1s Movement Delay");

            // Update player data
            playerData.setFireInterval(newFireInterval);
            playerData.setMoveInterval(newMoveInterval);

            if (itemInfoText != null) {
                itemInfoText.setText("Fusion Reactor acquired! Fire faster, move slower");
            }
        }

        // Update active effects display
        if (!hasActivePowerUps) {
            status.append("None");
        }

        if (activeEffects != null) {
            activeEffects.setText(status.toString());
        }

        // Update stat displays
        movementSpeedText.setText("Movement Speed: " + playerData.getMoveInterval() + "ms");
        fireRateText.setText("Fire Rate: " + playerData.getFireInterval() + "ms");

        // Clear info text after delay
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (itemInfoText != null) {
                itemInfoText.setText("");
            }
        }, 3000);
    }

    @Subscribe
    public void onHitEvent(HitEvent event) throws InterruptedException {
//        Log.d("onHitEvent", "Hit");
        if (event.getPlayableId() == playableId) {
            clientController.getLifeAsync((int) playableId);
            sleep(100);
//            Log.d("onHitEvent", "tank life: " + playerData.getTankLife());
            tankHealthBar.setProgress(playerData.getTankLife());
            builderHealthBar.setProgress(playerData.getBuilderLife());
            soldierHealthBar.setProgress(playerData.getSoldierLife());
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onItemPickup(ItemPickupEvent event) {
        Log.d(TAG, "Item pickup event received. Type: " + event.getItemType());

        if (event.getItemType() == 1) { // Thingamajig
            String message = String.format("Picked up Thingamajig! Added $%.2f credits", event.getAmount());
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
            fetchAndUpdateBalance();
        } else if (event.getItemType() == 2) { // AntiGrav
            Log.d(TAG, "AntiGrav pickup: Current move interval = " + playerData.getMoveInterval());

            playerData.incrementPowerUps();

            // Halve the move interval (double speed)
            int newMoveInterval = playerData.getMoveInterval() / 2;
            // Add 100ms to fire interval
            int newFireInterval = playerData.getFireInterval() + 100;

            Log.d(TAG, "AntiGrav applying: New move interval = " + newMoveInterval + ", new fire interval = " + newFireInterval);

            playerData.setMoveInterval(newMoveInterval);
            playerData.setFireInterval(newFireInterval);

            updateStatsDisplay();

            String effectStr = String.format("Anti-Grav activated!\nSpeed: %dms → %dms\nFire rate: %dms → %dms",
                    newMoveInterval * 2, newMoveInterval,
                    newFireInterval - 100, newFireInterval);

            Toast.makeText(this, "Anti-Grav Power-up Acquired!", Toast.LENGTH_SHORT).show();
            showPowerUpMessage(effectStr);

        } else if (event.getItemType() == 3) { // FusionReactor
            Log.d(TAG, "FusionReactor pickup: Current fire interval = " + playerData.getFireInterval());

            playerData.incrementPowerUps();

            // Halve the fire interval (double fire rate)
            int newFireInterval = playerData.getFireInterval() / 2;
            // Add 100ms to move interval
            int newMoveInterval = playerData.getMoveInterval() + 100;

            Log.d(TAG, "FusionReactor applying: New fire interval = " + newFireInterval + ", new move interval = " + newMoveInterval);

            playerData.setMoveInterval(newMoveInterval);
            playerData.setFireInterval(newFireInterval);

            updateStatsDisplay();

            String effectStr = String.format("Fusion Reactor activated!\nFire rate: %dms → %dms\nSpeed: %dms → %dms",
                    newFireInterval * 2, newFireInterval,
                    newMoveInterval - 100, newMoveInterval);

            Toast.makeText(this, "Fusion Reactor Power-up Acquired!", Toast.LENGTH_SHORT).show();
            showPowerUpMessage(effectStr);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onPowerUpEjected(PowerUpEjectEvent event) {
        Log.d(TAG, "Power-up ejected event received");

        playerData.decrementPowerUps();

        if (playerData.getActivePowerUps() == 0) {
            Log.d(TAG, "No active power-ups remaining, resetting to base values");
            playerData.resetPowerUps();
            showPowerUpMessage("Power-up ejected! Stats reset:\nMove Speed: 500ms\nFire Rate: 1500ms");
        } else {
            Log.d(TAG, "Power-up ejected, remaining: " + playerData.getActivePowerUps());
            String status = String.format("Power-up ejected!\nCurrent Move Speed: %dms\nCurrent Fire Rate: %dms",
                    playerData.getMoveInterval(), playerData.getFireInterval());
            showPowerUpMessage(status);
        }

        updateStatsDisplay();
    }
}