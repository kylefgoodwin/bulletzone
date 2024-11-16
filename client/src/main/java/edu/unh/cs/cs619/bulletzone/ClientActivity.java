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
import org.androidannotations.rest.spring.annotations.RestService;
import org.androidannotations.api.BackgroundExecutor;

import edu.unh.cs.cs619.bulletzone.events.GameEventProcessor;
import edu.unh.cs.cs619.bulletzone.events.HitEvent;
import edu.unh.cs.cs619.bulletzone.events.ItemPickupEvent;
import edu.unh.cs.cs619.bulletzone.events.PowerUpEjectEvent;
import edu.unh.cs.cs619.bulletzone.rest.BZRestErrorhandler;
import edu.unh.cs.cs619.bulletzone.rest.BulletZoneRestClient;
import edu.unh.cs.cs619.bulletzone.rest.GridPollerTask;
import edu.unh.cs.cs619.bulletzone.ui.GridAdapter;
import edu.unh.cs.cs619.bulletzone.util.ClientActivityShakeDriver;
import edu.unh.cs.cs619.bulletzone.util.FileHelper;
import edu.unh.cs.cs619.bulletzone.util.ReplayData;
import edu.unh.cs.cs619.bulletzone.util.ReplayDataFlat;

import androidx.annotation.VisibleForTesting;

import com.skydoves.progressview.ProgressView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.HashSet;
import java.util.Set;

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

    FileHelper fileHelper;

    private long playableId = -1;
    private int playableType = 1;
    private int improvementType = 0;
    private long userId = -1;
    private ArrayList<?> playableSelections = new ArrayList<>(Arrays.asList("Tank", "Builder", "Soldier"));
    private ArrayList<String> improvementSelections = new ArrayList<>(Arrays.asList("destructibleWall", "indestructibleWall", "miningFacility"));
    private long lastEventTimestamp = 0;
    private Set<Long> processedItemEvents = new HashSet<>();
    private Set<Long> processedEventIds = new HashSet<>();

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
        Log.d(TAG, "onCreate called");
        EventBus.getDefault().register(this);

        fileHelper = new FileHelper(getApplicationContext());
        replayData.setInitialTimeStamp(System.currentTimeMillis());
        shakeDriver = new ClientActivityShakeDriver(this, () -> onButtonFire());
        processedItemEvents = new HashSet<>();
        processedEventIds = new HashSet<>();
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy called");

        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }

        clientController.updateReplays(getApplicationContext());

        gridPollTask.stop();
        BackgroundExecutor.cancelAll("grid_poller_task", true);
        processedItemEvents.clear();
        processedEventIds.clear();

        if (simBoardView != null) {
            simBoardView.detach();
        }

        if (eventProcessor != null) {
            eventProcessor.stop();
        }

        if (shakeDriver != null) {
            shakeDriver.stop();
        }

        super.onDestroy();
    }

    @AfterViews
    protected void afterViewInjection() {
        Log.d(TAG, "afterViewInjection called");
        userId = playerData.getUserId();
        playableId = playerData.getTankId();
        replayData.setPlayerTankID(playableId);

        if (userId != -1) {
            userIdTextView.setText("User ID: " + userId);
            fetchAndUpdateBalance();
        } else {
            userIdTextView.setText("User ID: Not logged in");
            updateBalanceUI(null);
        }

        playerData.resetPowerUps();
        updateStatsDisplay();

        SystemClock.sleep(500);
        selectImprovement.setAdapter(new ArrayAdapter<>(this, androidx.appcompat.R.layout.support_simple_spinner_dropdown_item, improvementSelections));
        selectPlayable.setAdapter(new ArrayAdapter<>(this,
                androidx.appcompat.R.layout.support_simple_spinner_dropdown_item, playableSelections));
        simBoardView.attach(gridView, tGridView, playableId);
    }

    @Background
    protected void initializeGameBoard() {
        try {
            // Get initial board state through the GameEventProcessor
            Log.d(TAG, "Initializing game board");
            gridPollTask.doPoll(eventProcessor);
        } catch (Exception e) {
            Log.e(TAG, "Error initializing game board", e);
        }
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
        Log.d(TAG, "Updating stats - Move: " + playerData.getMoveInterval() + "ms, Fire: " + playerData.getFireInterval() + "ms");

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
                int antiGravCount = playerData.getAntiGravCount();
                int fusionCount = playerData.getFusionReactorCount();

                if (antiGravCount > 0) {
                    effects.append("• Anti-Grav (").append(antiGravCount).append(")\n");
                }
                if (fusionCount > 0) {
                    effects.append("• Fusion Reactor (").append(fusionCount).append(")\n");
                }
                activeEffects.setText(effects.toString().trim());
            } else {
                activeEffects.setText("None");
            }
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
    protected void onPlayableSelect(boolean checked, int position) {
        Log.d(TAG, "Spinner position = " + position);
        playableType = position+1;
    }

    @ItemSelect({R.id.selectImprovement})
    protected void onBuildSelect(boolean checked, int position){
        Log.d(TAG,"spinnerpositon = " + position);
        improvementType = position;
    }

    @Click({R.id.buttonUp, R.id.buttonDown, R.id.buttonLeft, R.id.buttonRight})
    protected void onButtonMove(View view) {
        byte direction;
        switch (view.getId()) {
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
            default:
                return;
        }
        tankEventController.turnOrMove(view.getId(), playableId, playableType, direction);
    }

    @Click(R.id.buttonFire)
    protected void onButtonFire() {
        tankEventController.fire(playableId, playableType);
    }

    @Click(R.id.buttonBuild)
    protected void onButtonBuild() {
        if (improvementType >= 0 && improvementType < improvementSelections.size()) {
            if (improvementType == 0) {
                tankEventController.buildAsync(userId, playableId, playableType, playerData.setCurEntity("destructibleWall"));
                fetchAndUpdateBalance();
            } else if (improvementType == 1) {
                tankEventController.buildAsync(userId, playableId, playableType, playerData.setCurEntity("indestructibleWall"));
                fetchAndUpdateBalance();
            } else if (improvementType == 2) {
                tankEventController.buildAsync(userId, playableId, playableType, playerData.setCurEntity("miningFacility"));
                fetchAndUpdateBalance();
            } else {
                // Handle the case where improvementType is out of bounds
                Log.e("onButtonBuild", "Invalid improvement type index: " + improvementType);

            }
        }
    }

    @Click(R.id.buttonDismantle)
    protected void onButtonDismantle() {
        if (improvementType >= 0 && improvementType < improvementSelections.size()) {
            if (improvementType == 0) {
                tankEventController.dismantleAsync(playableId, playableType, playerData.setCurEntity("destructibleWall"));
                fetchAndUpdateBalance();
            } else if (improvementType == 1) {
                tankEventController.dismantleAsync(playableId, playableType, playerData.setCurEntity("indestructibleWall"));
                fetchAndUpdateBalance();
            } else if (improvementType == 2) {
                tankEventController.dismantleAsync(playableId, playableType, playerData.setCurEntity("miningFacility"));
                fetchAndUpdateBalance();
            } else {
                // Handle the case where improvementType is out of bounds
                Log.e("onButtonBuild", "Invalid improvement type index: " + improvementType);

            }
        }
    }

        @Click(R.id.buttonEjectSoldier)
    protected void onButtonEjectSoldier() {
        clientController.ejectSoldierAsync(playableId);
//        playerData.setSoldierEjected(true);
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
        BackgroundExecutor.cancelAll("grid_poller_task", true);
        clientController.leaveGameAsync(playableId);
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

            sleep(100);

            if (playerData.getTankLife() == 0 || playerData.getSoldierLife() == 0) {
                leaveGame();
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onItemPickup(ItemPickupEvent event) {
        // Create a unique identifier for this event
        long eventId = event.getTimeStamp() + event.getItemType();

        // Check for duplicate events
        if (event.getTimeStamp() <= lastEventTimestamp || processedItemEvents.contains(eventId)) {
            Log.d(TAG, "Skipping duplicate item pickup event");
            return;
        }

        lastEventTimestamp = event.getTimeStamp();
        processedItemEvents.add(eventId);

        Log.d(TAG, "Item pickup event received. Type: " + event.getItemType());

        switch (event.getItemType()) {
            case 1: // Thingamajig
                String message = String.format("Picked up Thingamajig! Added $%.2f credits", event.getAmount());
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                fetchAndUpdateBalance();
                break;

            case 2: // AntiGrav
                Log.d(TAG, "AntiGrav pickup - Current move interval: " + playerData.getMoveInterval());
                playerData.incrementPowerUps(2);

                int newMoveInterval = playerData.getMoveInterval() / 2;
                int newFireInterval = playerData.getFireInterval() + 100;

                playerData.setMoveInterval(newMoveInterval);
                playerData.setFireInterval(newFireInterval);

                Log.d(TAG, "Updated intervals - Move: " + newMoveInterval + ", Fire: " + newFireInterval);
                updateStatsDisplay();

                Toast.makeText(this, "Anti-Grav Power-up Acquired!", Toast.LENGTH_SHORT).show();
                showPowerUpMessage("Anti-Grav activated!\nSpeed doubled! Fire rate slightly reduced");
                break;

            case 3: // FusionReactor
                Log.d(TAG, "FusionReactor pickup - Current fire interval: " + playerData.getFireInterval());
                playerData.incrementPowerUps(3);

                newFireInterval = playerData.getFireInterval() / 2;
                newMoveInterval = playerData.getMoveInterval() + 100;

                playerData.setMoveInterval(newMoveInterval);
                playerData.setFireInterval(newFireInterval);

                Log.d(TAG, "Updated intervals - Move: " + newMoveInterval + ", Fire: " + newFireInterval);
                updateStatsDisplay();

                Toast.makeText(this, "Fusion Reactor Power-up Acquired!", Toast.LENGTH_SHORT).show();
                showPowerUpMessage("Fusion Reactor activated!\nFire rate doubled! Speed slightly reduced");
                break;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onPowerUpEjected(PowerUpEjectEvent event) {
        Log.d(TAG, "Power-up ejected event received - Type: " + event.getPowerUpType());

        playerData.decrementPowerUps(event.getPowerUpType());
        int remainingPowerUps = playerData.getActivePowerUps();

        if (remainingPowerUps == 0) {
            Log.d(TAG, "No active power-ups remaining, resetting to base values");
            playerData.resetPowerUps();
            showPowerUpMessage("Power-up ejected!\nStats reset to base values");
        } else {
            Log.d(TAG, "Recalculating stats with " + remainingPowerUps + " power-ups remaining");

            // Start with base values
            int moveInterval = 500;
            int fireInterval = 1500;

            // Apply remaining FusionReactors
            for (int i = 0; i < playerData.getFusionReactorCount(); i++) {
                fireInterval /= 2;  // Double fire rate
                moveInterval += 100; // Small movement penalty
            }

            // Apply remaining AntiGrav
            for (int i = 0; i < playerData.getAntiGravCount(); i++) {
                moveInterval /= 2;  // Double speed
                fireInterval += 100; // Small fire rate penalty
            }

            playerData.setMoveInterval(moveInterval);
            playerData.setFireInterval(fireInterval);

            String status = String.format("Power-up ejected!\nMove Speed: %dms\nFire Rate: %dms",
                    moveInterval, fireInterval);
            showPowerUpMessage(status);
        }

        updateStatsDisplay();
    }
}