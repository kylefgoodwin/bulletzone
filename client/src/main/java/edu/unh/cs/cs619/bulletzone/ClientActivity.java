package edu.unh.cs.cs619.bulletzone;

import static java.lang.Thread.sleep;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.VisibleForTesting;

import com.skydoves.progressview.ProgressView;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ItemSelect;
import org.androidannotations.annotations.NonConfigurationInstance;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.api.BackgroundExecutor;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import edu.unh.cs.cs619.bulletzone.events.GameEventProcessor;
import edu.unh.cs.cs619.bulletzone.events.HitEvent;
import edu.unh.cs.cs619.bulletzone.events.ItemPickupEvent;
import edu.unh.cs.cs619.bulletzone.events.MiningCreditsEvent;
import edu.unh.cs.cs619.bulletzone.events.PowerUpEjectEvent;
import edu.unh.cs.cs619.bulletzone.events.RemoveEvent;
import edu.unh.cs.cs619.bulletzone.events.TerrainUpdateEvent;
import edu.unh.cs.cs619.bulletzone.events.UIUpdateEvent;
import edu.unh.cs.cs619.bulletzone.model.BoardCell;
import edu.unh.cs.cs619.bulletzone.model.TankItem;
import edu.unh.cs.cs619.bulletzone.rest.BZRestErrorhandler;
import edu.unh.cs.cs619.bulletzone.rest.GridPollerTask;
import edu.unh.cs.cs619.bulletzone.util.ClientActivityShakeDriver;
import edu.unh.cs.cs619.bulletzone.util.FileHelper;
import edu.unh.cs.cs619.bulletzone.util.ReplayData;

@EActivity(R.layout.activity_client)
public class ClientActivity extends Activity {

    private static final String TAG = "ClientActivity";

    @Bean
    protected GameEventProcessor eventProcessor;

    @ViewById
    protected ProgressView tankHealthBar;

    @ViewById
    ProgressView soldierHealthBar;

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

    @ViewById
    protected Button buttonBuild;

    @ViewById
    protected Button buttonDismantle;

    @ViewById
    protected Button buttonEjectSoldier;

    @ViewById
    protected Button buttonUp;

    @ViewById
    protected Button buttonDown;

    @ViewById
    protected Button buttonLeft;

    @ViewById
    protected Button buttonRight;

    @ViewById
    protected Button buttonFire;

    @ViewById
    protected ProgressView shieldHealthBar;

    @ViewById
    protected TextView repairKitStatus;

    @ViewById
    protected TextView repairKitTimer;

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

    MediaPlayer mediaPlayer;

    private long playableId = -1;
    private int playableType = 0;
    private int improvementType = 0;
    private long userId = -1;

    private ArrayList<?> playableSelections = new ArrayList<>(Arrays.asList("Tank", "Builder", "Soldier", "Ship", "Factory"));
    private ArrayList<String> improvementSelections = new ArrayList<>(Arrays.asList("destructibleWall", "indestructibleWall", "miningFacility", "road", "deck", "bridge", "factory", "tank", "builder", "soldier"));
    private long lastEventTimestamp = 0;
    private Set<Long> processedItemEvents = new HashSet<>();
    private Set<Long> processedMiningEvents = new HashSet<>();
    private Set<Long> processedEventIds = new HashSet<>();
    private Handler repairKitHandler = new Handler(Looper.getMainLooper());
    private long repairKitEndTime = 0;
    private Runnable repairKitUpdateRunnable;
    private Handler repairKitHealingHandler = new Handler(Looper.getMainLooper());
    private Runnable repairKitHealingRunnable;



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
        playerData.setContext(getApplicationContext());
        shakeDriver = new ClientActivityShakeDriver(this, () -> onButtonFire());
        processedItemEvents = new HashSet<>();
        processedEventIds = new HashSet<>();

        mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.goblin_march_song);
        mediaPlayer.start();
        mediaPlayer.setLooping(true);
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy called");

        stopRepairKitTimer();

        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }

        mediaPlayer.stop();

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

        // Initialize health bars with labels
        tankHealthBar.setProgress(100);
        tankHealthBar.setLabelText("Tank Health: 100/100");

        builderHealthBar.setProgress(80);
        builderHealthBar.setLabelText("Builder Health: 80/80");

        soldierHealthBar.setProgress(25);
        soldierHealthBar.setLabelText("Soldier Health: 25/25");

        shieldHealthBar.setProgress(0);
        shieldHealthBar.setLabelText("Shield: 0/50");
        shieldHealthBar.setVisibility(View.GONE);

        playerData.resetPowerUps();
        updateStatsDisplay();

        SystemClock.sleep(500);
        selectImprovement.setAdapter(new ArrayAdapter<>(this, androidx.appcompat.R.layout.support_simple_spinner_dropdown_item, improvementSelections));
        selectPlayable.setAdapter(new ArrayAdapter<>(this,
                androidx.appcompat.R.layout.support_simple_spinner_dropdown_item, playableSelections));
        simBoardView.attach(gridView, tGridView, playableId);

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                BoardCell cell = simBoardView.getCellAtPosition(i);

                TankItem entity;
                switch (cell.getCellType()) {
                    case "Tank":
                        entity = (TankItem) cell;
                        clientController.postLifeAsync(entity.getTankID(), 0, ClientActivity.this);
                        break;
                    case "Builder":
                        entity = (TankItem) cell;
                        clientController.postLifeAsync(entity.getTankID(), 1, ClientActivity.this);
                        break;
                    case "Soldier":
                        entity = (TankItem) cell;
                        clientController.postLifeAsync(entity.getTankID(), 2, ClientActivity.this);
                        break;
                    default:
                        break;
                }

            }
        });
    }

    @Background
    protected void initializeGameBoard() {
        try {
            // Get initial board state through the GameEventProcessor
            Log.d(TAG, "Initializing game board");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                gridPollTask.doPoll(eventProcessor);
            }
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

        // Update tank health display
        if (tankHealthBar != null) {
            tankHealthBar.setProgress(playerData.getTankLife());
            tankHealthBar.setLabelText(String.format("Tank Health: %d/100", playerData.getTankLife()));
        }

        // Update builder health display
        if (builderHealthBar != null) {
            builderHealthBar.setProgress(playerData.getBuilderLife());
            builderHealthBar.setLabelText(String.format("Builder Health: %d/80", playerData.getBuilderLife()));
        }

        // Update soldier health display
        if (soldierHealthBar != null) {
            soldierHealthBar.setProgress(playerData.getSoldierLife());
            soldierHealthBar.setLabelText(String.format("Soldier Health: %d/25", playerData.getSoldierLife()));
        }

        // Update movement and fire rate displays
        if (movementSpeedText != null) {
            movementSpeedText.setText("Movement Speed: " + playerData.getMoveInterval() + "ms");
        }
        if (fireRateText != null) {
            fireRateText.setText("Fire Rate: " + playerData.getFireInterval() + "ms");
        }

        // Update shield visibility and status
        if (shieldHealthBar != null) {
            int shieldCount = playerData.getDeflectorShieldCount();
            if (shieldCount > 0) {
                shieldHealthBar.setVisibility(View.VISIBLE);
                shieldHealthBar.setProgress(playerData.getShieldHealth());
                shieldHealthBar.setLabelText(String.format("Shield: %d/50", playerData.getShieldHealth()));
            } else {
                shieldHealthBar.setVisibility(View.GONE);
            }
        }

        // Update repair kit status
        if (repairKitStatus != null && repairKitTimer != null) {
            int repairKitCount = playerData.getRepairKitCount();
            if (repairKitCount > 0) {
                repairKitStatus.setVisibility(View.VISIBLE);
                repairKitTimer.setVisibility(View.VISIBLE);
                repairKitStatus.setText("Repair Kit: Active");
                updateRepairKitTimer();
            } else {
                repairKitStatus.setVisibility(View.GONE);
                repairKitTimer.setVisibility(View.GONE);
                stopRepairKitTimer();
            }
        }

        // Update active effects display
        if (activeEffects != null) {
            StringBuilder effects = new StringBuilder();
            int powerUps = playerData.getActivePowerUps();

            Log.d(TAG, "Active power-ups: " + powerUps);

            if (powerUps > 0) {
                int antiGravCount = playerData.getAntiGravCount();
                int fusionCount = playerData.getFusionReactorCount();
                int shieldCount = playerData.getDeflectorShieldCount();
                int repairKitCount = playerData.getRepairKitCount();

                if (antiGravCount > 0) {
                    effects.append("• Anti-Grav (").append(antiGravCount).append(")\n");
                }
                if (fusionCount > 0) {
                    effects.append("• Fusion Reactor (").append(fusionCount).append(")\n");
                }
                if (shieldCount > 0) {
                    effects.append("• Deflector Shield (").append(shieldCount).append(") - Absorbs damage\n");
                }
                if (repairKitCount > 0) {
                    effects.append("• Repair Kit (").append(repairKitCount).append(") - Auto-healing\n");
                }
                activeEffects.setText(effects.toString().trim());
            } else {
                activeEffects.setText("None");
            }
        }

        // Force UI updates using post instead of runOnUiThread
        if (tankHealthBar != null) tankHealthBar.post(() -> tankHealthBar.invalidate());
        if (builderHealthBar != null) builderHealthBar.post(() -> builderHealthBar.invalidate());
        if (soldierHealthBar != null) soldierHealthBar.post(() -> soldierHealthBar.invalidate());
        if (shieldHealthBar != null) shieldHealthBar.post(() -> shieldHealthBar.invalidate());
    }

    private void updateRepairKitTimer() {
        if (repairKitUpdateRunnable != null) {
            repairKitHandler.removeCallbacks(repairKitUpdateRunnable);
        }

        repairKitUpdateRunnable = new Runnable() {
            @Override
            public void run() {
                if (repairKitTimer != null) {
                    long currentTime = System.currentTimeMillis();
                    long timeRemaining = repairKitEndTime - currentTime;

                    if (timeRemaining > 0) {
                        int seconds = (int) (timeRemaining / 1000);
                        int minutes = seconds / 60;
                        seconds = seconds % 60;
                        repairKitTimer.setText(String.format("Time Remaining: %02d:%02d", minutes, seconds));
                        repairKitHandler.postDelayed(this, 1000);
                    } else {
                        repairKitTimer.setText("Time Remaining: 00:00");
                        playerData.decrementPowerUps(5); // Remove repair kit
                        updateStatsDisplay();
                    }
                }
            }
        };

        repairKitHandler.post(repairKitUpdateRunnable);
    }

    private void stopRepairKitTimer() {
        if (repairKitUpdateRunnable != null) {
            repairKitHandler.removeCallbacks(repairKitUpdateRunnable);
            repairKitUpdateRunnable = null;
        }
        if (repairKitHealingRunnable != null) {
            repairKitHealingHandler.removeCallbacks(repairKitHealingRunnable);
            repairKitHealingRunnable = null;
        }
    }

    @AfterInject
    void afterInject() {
        Log.d(TAG, "afterInject");
        clientController.setErrorHandler(bzRestErrorhandler);
        eventProcessor.start();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            gridPollTask.doPoll(eventProcessor);
        }
    }

    @ItemSelect({R.id.selectPlayable})
    protected void onPlayableSelect(boolean checked, int position) {
        Log.d(TAG, "Spinner position = " + position);
        playableType = position;

        // Logic block to enable / disable buttons depending on selected playable type
        if (playableType == 0) {
            buttonBuild.setEnabled(false);
            buttonDismantle.setEnabled(false);
            selectImprovement.setEnabled(false);
            buttonEjectSoldier.setEnabled(true);
            buttonDown.setEnabled(true);
            buttonRight.setEnabled(true);
            buttonLeft.setEnabled(true);
            buttonUp.setEnabled(true);
            buttonFire.setEnabled(true);
        } else if (playableType == 1) {
            buttonBuild.setEnabled(true);
            buttonDismantle.setEnabled(true);
            selectImprovement.setEnabled(true);
            buttonEjectSoldier.setEnabled(false);
            buttonDown.setEnabled(true);
            buttonRight.setEnabled(true);
            buttonLeft.setEnabled(true);
            buttonUp.setEnabled(true);
            buttonFire.setEnabled(true);
        } else if (playableType == 2) {
            buttonBuild.setEnabled(false);
            buttonDismantle.setEnabled(false);
            selectImprovement.setEnabled(false);
            buttonEjectSoldier.setEnabled(false);
            buttonDown.setEnabled(true);
            buttonRight.setEnabled(true);
            buttonLeft.setEnabled(true);
            buttonUp.setEnabled(true);
            buttonFire.setEnabled(true);
        } else if (playableType == 3){
            buttonBuild.setEnabled(false);
            buttonDismantle.setEnabled(false);
            selectImprovement.setEnabled(false);
            buttonEjectSoldier.setEnabled(false);
            buttonDown.setEnabled(true);
            buttonRight.setEnabled(true);
            buttonLeft.setEnabled(true);
            buttonUp.setEnabled(true);
            buttonFire.setEnabled(true);
        } else if (playableType == 4) {
            buttonBuild.setEnabled(true);
            buttonDismantle.setEnabled(false);
            selectImprovement.setEnabled(true);
            buttonEjectSoldier.setEnabled(false);
            buttonDown.setEnabled(false);
            buttonRight.setEnabled(false);
            buttonLeft.setEnabled(false);
            buttonUp.setEnabled(false);
            buttonFire.setEnabled(false);

        }

        playableType = position;
    }

    @ItemSelect({R.id.selectImprovement})
    protected void onBuildSelect(boolean checked, int position) {
        Log.d(TAG, "spinnerpositon = " + position);
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
            if (improvementType == 0 && playableType != 4) {
                tankEventController.buildAsync(userId, playableId, playableType, playerData.setCurEntity("destructibleWall"));

                tankEventController.removeCredits(userId, 80.0, playerData.setCurEntity("destructibleWall"));
                fetchAndUpdateBalance();
            } else if (improvementType == 1 && playableType != 4) {
                tankEventController.buildAsync(userId, playableId, playableType, playerData.setCurEntity("indestructibleWall"));

                tankEventController.removeCredits(userId, 150.0, playerData.setCurEntity("indestructibleWall"));
                fetchAndUpdateBalance();
            } else if (improvementType == 2 && playableType != 4) {
                tankEventController.buildAsync(userId, playableId, playableType, playerData.setCurEntity("miningFacility"));
                tankEventController.removeCredits(userId, 300.0, playerData.setCurEntity("miningFacility"));
                fetchAndUpdateBalance();
            } else if (improvementType == 3 && playableType != 4) {
                tankEventController.buildAsync(userId, playableId, playableType, playerData.setCurEntity("road"));

                tankEventController.removeCredits(userId, 40.0, playerData.setCurEntity("road"));
                fetchAndUpdateBalance();
            } else if (improvementType == 4 && playableType != 4) {
                tankEventController.buildAsync(userId, playableId, playableType, playerData.setCurEntity("deck"));

                tankEventController.removeCredits(userId, 150.0, playerData.setCurEntity("deck"));
                fetchAndUpdateBalance();
            } else if (improvementType == 5 && playableType != 4) {
                tankEventController.buildAsync(userId, playableId, playableType, playerData.setCurEntity("bridge"));

                tankEventController.removeCredits(userId, 150.0, playerData.setCurEntity("bridge"));
                fetchAndUpdateBalance();
            } else if (improvementType == 6 && playableType != 4) {
                tankEventController.buildAsync(userId, playableId, playableType, playerData.setCurEntity("factory"));

                tankEventController.removeCredits(userId, 150.0, playerData.setCurEntity("factory"));
                fetchAndUpdateBalance();
            } else if (improvementType == 7 && playableType == 4) {
                tankEventController.buildAsync(userId, playableId, playableType, playerData.setCurEntity("tank"));

                tankEventController.removeCredits(userId, 600.0, playerData.setCurEntity("tank"));
                fetchAndUpdateBalance();
            } else if (improvementType == 8 && playableType == 4) {
                tankEventController.buildAsync(userId, playableId, playableType, playerData.setCurEntity("builder"));

                tankEventController.removeCredits(userId, 500.0, playerData.setCurEntity("builder"));
                fetchAndUpdateBalance();
            } else if (improvementType == 9 && playableType == 4) {
                tankEventController.buildAsync(userId, playableId, playableType, playerData.setCurEntity("soldier"));

                tankEventController.removeCredits(userId, 200.00, playerData.setCurEntity("soldier"));
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
                tankEventController.dismantleAsync(userId, playableId, playableType, playerData.setCurEntity("destructibleWall"));
                tankEventController.addCredits(userId, 80.0);
                fetchAndUpdateBalance();
            } else if (improvementType == 1) {
                tankEventController.dismantleAsync(userId, playableId, playableType, playerData.setCurEntity("indestructibleWall"));
                tankEventController.addCredits(userId, 150.0);
                fetchAndUpdateBalance();
            } else if (improvementType == 2) {
                tankEventController.dismantleAsync(userId, playableId, playableType, playerData.setCurEntity("miningFacility"));
                tankEventController.addCredits(userId, 300.0);
                fetchAndUpdateBalance();
            } else if (improvementType == 3) {
                tankEventController.dismantleAsync(userId, playableId, playableType, playerData.setCurEntity("road"));

                tankEventController.addCredits(userId, 40.0);
                fetchAndUpdateBalance();
            } else if (improvementType == 4) {
                tankEventController.dismantleAsync(userId, playableId, playableType, playerData.setCurEntity("deck"));

                tankEventController.addCredits(userId, 80.0);
                fetchAndUpdateBalance();
            } else if (improvementType == 5) {
                tankEventController.dismantleAsync(userId, playableId, playableType, playerData.setCurEntity("bridge"));

                tankEventController.addCredits(userId, 120.0);
                fetchAndUpdateBalance();
            } else if (improvementType == 6) {
                tankEventController.dismantleAsync(userId, playableId, playableType, playerData.setCurEntity("factory"));

                tankEventController.addCredits(userId, 250.0);
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
        selectPlayable.setSelection(2);
        buttonEjectSoldier.setEnabled(false);
//        playerData.setSoldierEjected(true);
    }

    @Click(R.id.buttonLeave)
    void leaveGame() {
        Log.d(TAG, "leaveGame() called, tank ID: " + playableId);

        // Sell all power-ups before leaving
        sellAllPowerUps();

        BackgroundExecutor.cancelAll("grid_poller_task", true);
        clientController.leaveGameAsync(playableId);
        leaveUI();
    }

    private void sellAllPowerUps() {
        Random random = new Random();
        double total = 0;  // Accumulate in this variable

        // Check and sell AntiGrav power-ups
        int antiGravCount = playerData.getAntiGravCount();
        while (antiGravCount > 0) {
            double credit = 250 + random.nextInt(101); // 250-350 credits
            total += credit;
            antiGravCount--;
        }

        // Check and sell FusionReactor power-ups
        int fusionCount = playerData.getFusionReactorCount();
        while (fusionCount > 0) {
            double credit = 350 + random.nextInt(101); // 350-450 credits
            total += credit;
            fusionCount--;
        }

        // Check and sell DeflectorShield power-ups
        int shieldCount = playerData.getDeflectorShieldCount();
        while (shieldCount > 0) {
            double credit = 250 + random.nextInt(101); // 250-350 credits
            total += credit;
            shieldCount--;
        }

        // Check and sell RepairKit power-ups
        int repairKitCount = playerData.getRepairKitCount();
        while (repairKitCount > 0) {
            double credit = 150 + random.nextInt(101); // 150-250 credits
            total += credit;
            repairKitCount--;
        }

        final double finalTotal = total;  // Create final variable for lambda

        if (finalTotal > 0) {
            // Add credits to user's account
            tankEventController.addCredits(userId, finalTotal);

            // Show notification with final total
            runOnUiThread(() -> {
                Toast.makeText(this,
                        String.format("Sold power-ups for %.2f credits!", finalTotal),
                        Toast.LENGTH_LONG).show();
            });

            // Update balance display
            fetchAndUpdateBalance();
        }

        // Reset all power-ups after selling
        playerData.resetPowerUps();
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
    protected void onButtonEject() {
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
    public void onHitEvent(HitEvent event) {
        if (event.getPlayableId() == playableId) {
            // Remember repair kit status before health update
            boolean hasRepairKit = playerData.getRepairKitCount() > 0;
            long currentTime = System.currentTimeMillis();

            // Request updated health from server
            clientController.getLifeAsync((int) playableId, event.getPlayableType());

            try {
                Thread.sleep(100); // Brief delay to allow server response
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            // Update health bars based on event type
            updateHealthBars(event.getPlayableType());

            // Maintain repair kit status through hit
            if (hasRepairKit && currentTime < playerData.getRepairKitExpiration()) {
                if (playerData.getRepairKitCount() == 0) {
                    playerData.incrementPowerUps(5);
                }
            }

            // Update shield health if we have one
            if (playerData.getDeflectorShieldCount() > 0) {
                int shieldHealth = event.getShieldHealth();
                updateShieldHealth(shieldHealth);
            }

            // Check for death
            if (playerData.getTankLife() <= 0) {
                onButtonEjectSoldier();
            } else if (playerData.getSoldierLife() <= 0 && event.getPlayableType() == 3) {
                leaveGame();
            }
        }
    }

    @UiThread
    protected void updateHealthBars(int playableType) {
        // Update tank health bar
        if (tankHealthBar != null) {
            int tankHealth = playerData.getTankLife();
            tankHealthBar.setProgress(tankHealth);
            tankHealthBar.setLabelText(String.format("Tank Health: %d/100", tankHealth));
        }

        // Update builder health bar
        if (builderHealthBar != null) {
            int builderHealth = playerData.getBuilderLife();
            builderHealthBar.setProgress(builderHealth);
            builderHealthBar.setLabelText(String.format("Builder Health: %d/80", builderHealth));
        }

        // Update soldier health bar
        if (soldierHealthBar != null) {
            int soldierHealth = playerData.getSoldierLife();
            soldierHealthBar.setProgress(soldierHealth);
            soldierHealthBar.setLabelText(String.format("Soldier Health: %d/25", soldierHealth));
        }

        // Force a UI refresh
        runOnUiThread(() -> {
            tankHealthBar.invalidate();
            builderHealthBar.invalidate();
            soldierHealthBar.invalidate();
        });
    }

    @UiThread
    protected void updateHealthDisplay(HitEvent event) {
        // Update tank health display with correct value
        int tankHealth = playerData.getTankLife();
        tankHealthBar.setProgress(tankHealth);
        tankHealthBar.setLabelText(String.format("Tank Health: %d/100", tankHealth));

        // Handle shield display
        int shieldHealth = event.getShieldHealth();
        if (shieldHealth <= 5) { // If shield is critically low or depleted
            runOnUiThread(() -> {
                shieldHealthBar.setVisibility(View.GONE);
                playerData.setShieldHealth(0);
                playerData.decrementPowerUps(4);
                updateStatsDisplay();
            });
        } else if (playerData.getDeflectorShieldCount() > 0) {
            runOnUiThread(() -> {
                shieldHealthBar.setVisibility(View.VISIBLE);
                shieldHealthBar.setProgress(shieldHealth);
                shieldHealthBar.setLabelText(String.format("Shield: %d/50", shieldHealth));
                playerData.setShieldHealth(shieldHealth);
            });
        }

        // Update other health bars
        builderHealthBar.setProgress(playerData.getBuilderLife());
        builderHealthBar.setLabelText(String.format("Builder Health: %d/80", playerData.getBuilderLife()));

        soldierHealthBar.setProgress(playerData.getSoldierLife());
        soldierHealthBar.setLabelText(String.format("Soldier Health: %d/25", playerData.getSoldierLife()));
    }

    public void setClientController(ClientController client) {
        clientController = client;
    }

    public void setSimBoardView(SimBoardView boardView) {
        simBoardView = boardView;
    }

    @UiThread
    protected void updateShieldHealth(int shieldHealth) {
        if (shieldHealthBar != null) {
            if (shieldHealth <= 5) {
                shieldHealthBar.setVisibility(View.GONE);
                playerData.setShieldHealth(0);
                playerData.decrementPowerUps(4);
                updateStatsDisplay();
            } else {
                shieldHealthBar.setVisibility(View.VISIBLE);
                shieldHealthBar.setProgress(shieldHealth);
                shieldHealthBar.setLabelText(String.format("Shield: %d/50", shieldHealth));
                playerData.setShieldHealth(shieldHealth);
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMiningCredits(MiningCreditsEvent event) {
        // Create a unique identifier for this event
        long eventId = event.getTimeStamp() + event.getOwnerId();

        // Check for duplicate events
//        if (event.getTimeStamp() <= lastEventTimestamp || processedMiningEvents.contains(eventId)) {
//            Log.d(TAG, "Skipping duplicate mining credit event");
//            return;
//        }

        // Mark the event as processed by adding it to the sets
        lastEventTimestamp = event.getTimeStamp();
        processedMiningEvents.add(eventId);

        // Logging for debugging
        Log.d(TAG, "Mining credit event received. User ID: " + event.getOwnerId() +
                ", Credits: " + event.getCreditAmount());

        // Show a toast to inform the user
        String message = String.format("Mining Facility generated %.2f credits!", event.getCreditAmount());
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();

        // Update the balance in the UI
        fetchAndUpdateBalance();

        // Optionally add the event ID to another set if needed
        processedEventIds.add(eventId);
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
                playerData.incrementPowerUps(2);
                updateStatsDisplay();
                Toast.makeText(this, "Anti-Grav Power-up Acquired!", Toast.LENGTH_SHORT).show();
                showPowerUpMessage("Anti-Grav activated!\nSpeed doubled! Fire rate slightly reduced");
                break;

            case 3: // FusionReactor
                playerData.incrementPowerUps(3);
                updateStatsDisplay();
                Toast.makeText(this, "Fusion Reactor Power-up Acquired!", Toast.LENGTH_SHORT).show();
                showPowerUpMessage("Fusion Reactor activated!\nFire rate doubled! Speed slightly reduced");
                break;

            case 4: // Deflector Shield
                playerData.incrementPowerUps(4);
                updateStatsDisplay();
                Toast.makeText(this, "Deflector Shield Acquired!", Toast.LENGTH_SHORT).show();
                showPowerUpMessage("Shield activated!\nAbsorbing 50% damage");
                shieldHealthBar.setProgress(50);
                shieldHealthBar.setLabelText("Shield: 50/50");
                shieldHealthBar.setVisibility(View.VISIBLE);
                playerData.setShieldHealth(50);
                break;

            case 5: // Repair Kit
                playerData.incrementPowerUps(5);
                updateStatsDisplay();
                Toast.makeText(this, "Repair Kit Acquired!", Toast.LENGTH_SHORT).show();
                showPowerUpMessage("Repair Kit activated!\nAuto-healing for 120 seconds");
                repairKitEndTime = System.currentTimeMillis() + 120000; // 120 seconds
                repairKitStatus.setVisibility(View.VISIBLE);
                repairKitTimer.setVisibility(View.VISIBLE);
                updateRepairKitTimer();
                handleRepairKitHealing(); // Start the healing process
                break;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void updateUIOnMove(UIUpdateEvent event) {
        Log.d(TAG, "Updating UI based on most recent move: " + event.toString());
        buttonUp.setEnabled(event.getCanMoveUp());
        buttonDown.setEnabled(event.getCanMoveDown());
        buttonLeft.setEnabled(event.getCanMoveLeft());
        buttonRight.setEnabled(event.getCanMoveRight());
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

    private void startRepairKitHealing() {
        if (repairKitHealingRunnable != null) {
            repairKitHealingHandler.removeCallbacks(repairKitHealingRunnable);
        }

        repairKitHealingRunnable = new Runnable() {
            @Override
            public void run() {
                if (playerData.getRepairKitCount() > 0 && playerData.getTankLife() < 100) {
                    playerData.setTankLife(Math.min(100, playerData.getTankLife() + 1));
                    runOnUiThread(() -> {
                        tankHealthBar.setProgress(playerData.getTankLife());
                        tankHealthBar.setLabelText(String.format("Tank Health: %d/100", playerData.getTankLife()));
                    });
                    repairKitHealingHandler.postDelayed(this, 1000); // Heal every second
                }
            }
        };
        repairKitHealingHandler.post(repairKitHealingRunnable);
    }

    private void handleRepairKitHealing() {
        if (repairKitHealingRunnable != null) {
            repairKitHealingHandler.removeCallbacks(repairKitHealingRunnable);
        }

        repairKitHealingRunnable = new Runnable() {
            @Override
            public void run() {
                if (playerData.getRepairKitCount() > 0 && playerData.getTankLife() < 100) {
                    // Make server-side repair request first
                    clientController.repairAsync(playerData.getTankId());

                    // Small delay to allow server response
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }

                    // Update local health and UI
                    int currentHealth = playerData.getTankLife();
                    int newHealth = Math.min(100, currentHealth + 1);
                    playerData.setTankLife(newHealth);

                    runOnUiThread(() -> {
                        if (tankHealthBar != null) {
                            tankHealthBar.setProgress(newHealth);
                            tankHealthBar.setLabelText(String.format("Tank Health: %d/100", newHealth));
                            tankHealthBar.invalidate();
                        }
                    });

                    // Schedule next healing tick if still needed
                    if (playerData.getRepairKitCount() > 0 && newHealth < 100) {
                        repairKitHealingHandler.postDelayed(this, 1000);
                    }
                }
            }
        };

        // Start the healing immediately
        repairKitHealingHandler.post(repairKitHealingRunnable);
    }

//    private void handleFactoryRepair() {
//        if (repairKitHealingRunnable != null) {
//            repairKitHealingHandler.removeCallbacks(repairKitHealingRunnable);
//        }
//
//        isMoving = false; // Reset movement flag before starting repair
//
//        repairKitHealingRunnable = new Runnable() {
//            @Override
//            public void run() {
//                if (isMoving) {
//                    // Stop healing if the unit has moved
//                    repairKitHealingHandler.removeCallbacks(this);
//                    return;
//                }
//
//                if (playerData.getRepairKitCount() > 0 && playerData.getTankLife() < 100) {
//                    // Make server-side repair request first
//                    clientController.repairAsync(playerData.getTankId());
//
//                    // Small delay to allow server response
//                    try {
//                        Thread.sleep(50);
//                    } catch (InterruptedException e) {
//                        Thread.currentThread().interrupt();
//                    }
//
//                    // Update local health and UI
//                    int currentHealth = playerData.getTankLife();
//                    int newHealth = Math.min(100, currentHealth + 1);
//                    playerData.setTankLife(newHealth);
//
//                    runOnUiThread(() -> {
//                        if (tankHealthBar != null) {
//                            tankHealthBar.setProgress(newHealth);
//                            tankHealthBar.setLabelText(String.format("Tank Health: %d/100", newHealth));
//                            tankHealthBar.invalidate();
//                        }
//                    });
//
//                    // Schedule next healing tick if still needed
//                    if (playerData.getRepairKitCount() > 0 && newHealth < 100 && !isMoving) {
//                        repairKitHealingHandler.postDelayed(this, 1000);
//                    }
//                }
//            }
//        };
//
//        // Start the healing immediately
//        repairKitHealingHandler.post(repairKitHealingRunnable);
//    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onTerrainUpdate(TerrainUpdateEvent event) {
        int playableType = event.getPlayableType();
        Log.d(TAG, "Terrain Update Event received: Hilly: " + event.isHilly() +
                " Forest: " + event.isForest() +
                " Rocky: " + event.isRocky() +
                " PlayableType: " + playableType +
                " From: " + event.getFromPosition() +
                " To: " + event.getToPosition());

        // Set the playable type first
        playerData.setCurId(playableType);

        // Only update terrain state if we actually moved
        if (event.isPositionChanged()) {
            // Update terrain state
            playerData.setTerrainState(
                    event.isHilly(),
                    event.isForest(),
                    event.isRocky()
            );

            // Update UI
            updateStatsDisplay();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onRemoveEvent(RemoveEvent event) {
        Log.d(TAG, "Event Tank ID: " + event.getSoldierRemove() + " | Player Tank ID: "
                + playerData.getTankId());
        if (event.getSoldierRemove() == playerData.getTankId()) {
            selectPlayable.setSelection(0);
        }
    }
}