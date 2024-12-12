package edu.unh.cs.cs619.bulletzone;

import android.util.Log;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.androidannotations.rest.spring.annotations.RestService;
import org.greenrobot.eventbus.EventBus;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import edu.unh.cs.cs619.bulletzone.events.ItemPickupEvent;
import edu.unh.cs.cs619.bulletzone.events.SpawnEvent;
import edu.unh.cs.cs619.bulletzone.rest.BulletZoneRestClient;
import edu.unh.cs.cs619.bulletzone.util.BooleanWrapper;

/**
 * Made by Alec Rydeen
 * This class takes the responsibility of communicating with the Rest Client away from the ClientActivity,
 * and moves it here, which is then used inside of ClientActivity. Focused on Tank Controls
 */

// Controller Class to move rest client calls for tank controls outside of ClientActivity
@EBean
public class TankEventController {

    @RestService
    BulletZoneRestClient restClient;
    private final ConcurrentHashMap<Long, Boolean> activeMiningFacilities = new ConcurrentHashMap<>();

    private int lastPressedButtonId = -1;

    @Bean
    ClientController clientController;
    PlayerData playerData;
    boolean isMining;
    double miningFacilityCount = 0;
    private static final String TAG = "TankEventController";

    public TankEventController() {}

    @Background
    public void moveAsync(long playableId, int playableType, byte direction) {
        restClient.move(playableId, playableType, direction);
    }

    @Background
    public void turnAsync(long playableId, int playableType, byte direction) {
        restClient.turn(playableId, playableType, direction);
    }

    @Background
    public void ejectSoldier(long playableId){
        restClient.ejectSoldier(playableId);
    }

    public Double getBalance(long userId) {
        try {
            return restClient.getBalance(userId);
        } catch (Exception e) {
            Log.e(TAG, "Error getting balance", e);
            return null;
        }
    }

    /**
     * Sends either a dismantle or build command depending on what unit is currently selected
     *
     * @param playableId  unit id currently selected
     * @param entity currently selected improvement type
     */
    @Background
    public void buildAsync(long userId, long playableId, int playableType, String entity) {
        if (Objects.equals(entity, "destructibleWall")) {
            Log.d(TAG, "Balance (destructibleWall)" + restClient.getBalance(userId));
            if (restClient.getBalance(userId) >= 80.0 && playableType != 3) {
                restClient.build(playableId, playableType, entity);
            }
        } else if (Objects.equals(entity, "indestructibleWall")) {
            Log.d(TAG, "Balance (indestructibleWall)" + restClient.getBalance(userId));
            if (restClient.getBalance(userId) >= 150.0 && playableType != 3) {
                restClient.build(playableId, playableType, entity);
            }
        } else if (Objects.equals(entity, "miningFacility")) {
            Log.d(TAG, "Balance (miningFacility)" + restClient.getBalance(userId));
            if (restClient.getBalance(userId) >= 300.0 && playableType != 3) {
                restClient.build(playableId, playableType, entity);
            }
            miningFacilityCount++;

        } else if (Objects.equals(entity, "road")) {
            Log.d(TAG, "Balance (road)" + restClient.getBalance(userId));
            if (restClient.getBalance(userId) >= 40.0 && playableType != 3) {
                restClient.build(playableId, playableType, entity);
            }
        } else if (Objects.equals(entity, "deck")) {
            Log.d(TAG, "Balance (deck)" + restClient.getBalance(userId));
            if (restClient.getBalance(userId) >= 80.0 && playableType != 3) {
                restClient.build(playableId, playableType, entity);
            }
        } else if (Objects.equals(entity, "bridge")) {
            Log.d(TAG, "Balance (bridge)" + restClient.getBalance(userId));
            if (restClient.getBalance(userId) >= 120.0 && playableType != 3) {
                restClient.build(playableId, playableType, entity);
            }
        } else if (Objects.equals(entity, "factory")) {
            Log.d(TAG, "Balance (factory)" + restClient.getBalance(userId));
            if (restClient.getBalance(userId) >= 250.0 && playableType != 3) {
                restClient.build(playableId, playableType, entity);
            } else if (playableType == 3) {
                restClient.build(playableId, playableType, entity);
            }
        } else if (Objects.equals(entity, "tank")) {
            Log.d(TAG, "Balance (tank)" + restClient.getBalance(userId));
            if (restClient.getBalance(userId) >= 600.0 && playableType != 3) {
                restClient.build(playableId, playableType, entity);
            } else if (playableType == 3) {
                restClient.build(playableId, playableType, entity);
            }
        } else if (Objects.equals(entity, "builder")) {
            Log.d(TAG, "Balance (builder)" + restClient.getBalance(userId));
            if (restClient.getBalance(userId) >= 500.0 && playableType != 3) {
                restClient.build(playableId, playableType, entity);
            } else if (playableType == 3) {
                restClient.build(playableId, playableType, entity);
            }
        } else if (Objects.equals(entity, "soldier")) {
            Log.d(TAG, "Balance (soldier)" + restClient.getBalance(userId));
            if (restClient.getBalance(userId) >= 200.0 && playableType != 3) {
                restClient.build(playableId, playableType, entity);
            } else if (playableType == 3) {
                restClient.build(playableId, playableType, entity);
            }
        } else if (Objects.equals(entity, "ship")) {
            Log.d(TAG, "Balance (ship)" + restClient.getBalance(userId));
            if (restClient.getBalance(userId) >= 400.0 && playableType != 3) {
                restClient.build(playableId, playableType, entity);
            }
        }
    }
    @Background
    public void addCredits(long userId, double amount) {
        Log.d(TAG, "Processing build, amount: " + amount);

        BooleanWrapper result = restClient.depositBalance(userId, amount);
        if (result != null && result.isResult()) {
            Log.d(TAG, "Transaction successful: " + amount + " credits");
        } else {
            Log.e(TAG, "Transaction failed: " + amount + " credits");
        }
    }

    @Background
    public void removeCredits(long userId, double amount, String entity, int playableType) {
        Log.d(TAG, "Processing build, amount: " + amount);
        if (Objects.equals(entity, "destructibleWall")) {
            Log.d(TAG, "Balance (destructibleWall)" + restClient.getBalance(userId));
            if (restClient.getBalance(userId) >= 80.0 ) {
                BooleanWrapper result = restClient.deductBalance(userId, amount);
                if (result != null && result.isResult()) {
                    Log.d(TAG, "Transaction successful: " + amount + " credits");
                } else {
                    Log.e(TAG, "Transaction failed: " + amount + " credits");
                }
            }
        } else if (Objects.equals(entity, "indestructibleWall")) {
            Log.d(TAG, "Balance (indestructibleWall)" + restClient.getBalance(userId));
            if (restClient.getBalance(userId) >= 150.0 ) {
                BooleanWrapper result = restClient.deductBalance(userId, amount);
                if (result != null && result.isResult()) {
                    Log.d(TAG, "Transaction successful: " + amount + " credits");
                } else {
                    Log.e(TAG, "Transaction failed: " + amount + " credits");
                }
            }
        } else if (Objects.equals(entity, "miningFacility")) {
            Log.d(TAG, "Balance (miningFacility)" + restClient.getBalance(userId));
            if (restClient.getBalance(userId) >= 300.0 ) {
                BooleanWrapper result = restClient.deductBalance(userId, amount);
                if (result != null && result.isResult()) {
                    Log.d(TAG, "Transaction successful: " + amount + " credits");
                } else {
                    Log.e(TAG, "Transaction failed: " + amount + " credits");
                }
            }

        } else if (Objects.equals(entity, "road")) {
            Log.d(TAG, "Balance (road)" + restClient.getBalance(userId));
            if (restClient.getBalance(userId) >= 40.0 ) {
                BooleanWrapper result = restClient.deductBalance(userId, amount);
                if (result != null && result.isResult()) {
                    Log.d(TAG, "Transaction successful: " + amount + " credits");
                } else {
                    Log.e(TAG, "Transaction failed: " + amount + " credits");
                }
            }
        } else if (Objects.equals(entity, "deck")) {
            Log.d(TAG, "Balance (deck)" + restClient.getBalance(userId));
            if (restClient.getBalance(userId) >= 80.0 ) {
                BooleanWrapper result = restClient.deductBalance(userId, amount);
                if (result != null && result.isResult()) {
                    Log.d(TAG, "Transaction successful: " + amount + " credits");
                } else {
                    Log.e(TAG, "Transaction failed: " + amount + " credits");
                }
            }
        } else if (Objects.equals(entity, "bridge")) {
            Log.d(TAG, "Balance (bridge)" + restClient.getBalance(userId));
            if (restClient.getBalance(userId) >= 120.0 ) {
                BooleanWrapper result = restClient.deductBalance(userId, amount);
                if (result != null && result.isResult()) {
                    Log.d(TAG, "Transaction successful: " + amount + " credits");
                } else {
                    Log.e(TAG, "Transaction failed: " + amount + " credits");
                }
            }
        } else if (Objects.equals(entity, "factory")) {
            Log.d(TAG, "Balance (factory)" + restClient.getBalance(userId));
            if (restClient.getBalance(userId) >= 250.0 ) {
                BooleanWrapper result = restClient.deductBalance(userId, amount);
                if (result != null && result.isResult()) {
                    Log.d(TAG, "Transaction successful: " + amount + " credits");
                } else {
                    Log.e(TAG, "Transaction failed: " + amount + " credits");
                }
            }
        } else if (Objects.equals(entity, "tank")) {
            Log.d(TAG, "Balance (tank)" + restClient.getBalance(userId));
            if (restClient.getBalance(userId) >= 600.0 && playableType != 3) {
                BooleanWrapper result = restClient.deductBalance(userId, amount);
                if (result != null && result.isResult()) {
                    Log.d(TAG, "Transaction successful: " + amount + " credits");
                } else {
                    Log.e(TAG, "Transaction failed: " + amount + " credits");
                }
            } else if (playableType == 3) {
                if (restClient.getBalance(userId) >= 1000.0) {
                    BooleanWrapper result = restClient.deductBalance(userId, amount);
                    if (result != null && result.isResult()) {
                        Log.d(TAG, "Transaction successful: " + amount + " credits");
                    } else {
                        Log.e(TAG, "Transaction failed: " + amount + " credits");
                    }
                } else {
                    BooleanWrapper result1 = restClient.depositBalance(userId, amount);
                    if (result1 != null && result1.isResult()) {
                        Log.d(TAG, "Transaction successful: " + amount + " credits");
                    } else {
                        Log.e(TAG, "Transaction failed: " + amount + " credits");
                    }
                    BooleanWrapper result2 = restClient.deductBalance(userId, amount);
                    if (result2 != null && result2.isResult()) {
                        Log.d(TAG, "Transaction successful: " + amount + " credits");
                    } else {
                        Log.e(TAG, "Transaction failed: " + amount + " credits");
                    }
                }
            }
        } else if (Objects.equals(entity, "builder")) {
            Log.d(TAG, "Balance (builder)" + restClient.getBalance(userId));
            if (restClient.getBalance(userId) >= 500.0 && playableType != 3) {
                BooleanWrapper result = restClient.deductBalance(userId, amount);
                if (result != null && result.isResult()) {
                    Log.d(TAG, "Transaction successful: " + amount + " credits");
                } else {
                    Log.e(TAG, "Transaction failed: " + amount + " credits");
                }
            } else if (playableType == 3) {
                if (restClient.getBalance(userId) >= 1000.0) {
                    BooleanWrapper result = restClient.deductBalance(userId, amount);
                    if (result != null && result.isResult()) {
                        Log.d(TAG, "Transaction successful: " + amount + " credits");
                    } else {
                        Log.e(TAG, "Transaction failed: " + amount + " credits");
                    }
                } else {
                    BooleanWrapper result1 = restClient.depositBalance(userId, amount);
                    if (result1 != null && result1.isResult()) {
                        Log.d(TAG, "Transaction successful: " + amount + " credits");
                    } else {
                        Log.e(TAG, "Transaction failed: " + amount + " credits");
                    }
                    BooleanWrapper result2 = restClient.deductBalance(userId, amount);
                    if (result2 != null && result2.isResult()) {
                        Log.d(TAG, "Transaction successful: " + amount + " credits");
                    } else {
                        Log.e(TAG, "Transaction failed: " + amount + " credits");
                    }
                }
            }
        } else if (Objects.equals(entity, "soldier")) {
            Log.d(TAG, "Balance (soldier)" + restClient.getBalance(userId));
            if (restClient.getBalance(userId) >= 200.0 && playableType != 3) {
                BooleanWrapper result = restClient.deductBalance(userId, amount);
                if (result != null && result.isResult()) {
                    Log.d(TAG, "Transaction successful: " + amount + " credits");
                } else {
                    Log.e(TAG, "Transaction failed: " + amount + " credits");
                }
            } else if (playableType == 3) {
                if (restClient.getBalance(userId) >= 1000.0) {
                    BooleanWrapper result = restClient.deductBalance(userId, amount);
                    if (result != null && result.isResult()) {
                        Log.d(TAG, "Transaction successful: " + amount + " credits");
                    } else {
                        Log.e(TAG, "Transaction failed: " + amount + " credits");
                    }
                } else {
                    BooleanWrapper result1 = restClient.depositBalance(userId, amount);
                    if (result1 != null && result1.isResult()) {
                        Log.d(TAG, "Transaction successful: " + amount + " credits");
                    } else {
                        Log.e(TAG, "Transaction failed: " + amount + " credits");
                    }
                    BooleanWrapper result2 = restClient.deductBalance(userId, amount);
                    if (result2 != null && result2.isResult()) {
                        Log.d(TAG, "Transaction successful: " + amount + " credits");
                    } else {
                        Log.e(TAG, "Transaction failed: " + amount + " credits");
                    }
                }
            }
        } else if (Objects.equals(entity, "ship")) {
            Log.d(TAG, "Balance (ship)" + restClient.getBalance(userId));
            if (restClient.getBalance(userId) >= 400.0 && playableType != 3) {
                BooleanWrapper result = restClient.deductBalance(userId, amount);
                if (result != null && result.isResult()) {
                    Log.d(TAG, "Transaction successful: " + amount + " credits");
                } else {
                    Log.e(TAG, "Transaction failed: " + amount + " credits");
                }
            }
        }
    }

    @Background
    public void startMiningFacility(long userId, long playableId) {
        // Mark the mining facility as active
        activeMiningFacilities.put(playableId, true);

        try {
            while (miningFacilityCount != 0) {
                // Add credits to the user's battlefield cache
                BooleanWrapper result = restClient.depositBalance(userId, miningFacilityCount);
                if (result != null && result.isResult()) {
                    Log.d(TAG, "Mining credits: " + miningFacilityCount + " credits");
                } else {
                    Log.e(TAG, "Transaction failed: " + miningFacilityCount + " credits");
                }
                Log.d(TAG, "Added 1 credit to user " + userId + " from mining facility " + playableId);
                Thread.sleep(1000); // Simulate 1 second
            }
        } catch (InterruptedException e) {
            Log.e(TAG, "Mining facility " + playableId + " interrupted", e);
        }
    }


    /**
     * Sends either a dismantle or build command depending on what unit is currently selected
     *
     * @param playableId  unit id currently selected
     * @param entity currently selected improvement type
     */
    @Background
    public void dismantleAsync(long userId, long playableId, int playableType, String entity) {
        // send build
        if (Objects.equals(entity, "destructibleWall")) {
            restClient.build(playableId, playableType, entity);
            // Remove credits to user's account
        } else if (Objects.equals(entity, "indestructibleWall")) {
            restClient.build(playableId, playableType, entity);
        } else if (Objects.equals(entity, "miningFacility")) {
            if (miningFacilityCount != 0) {
                activeMiningFacilities.remove(playableId); // Stop mining for this facility
                restClient.build(playableId, playableType, entity);
                miningFacilityCount--;
            } else {
                Log.e(TAG, "Mining facility " + playableId + " is not active or not owned by user " + userId);
            }
        } else if (Objects.equals(entity, "road")) {
            restClient.build(playableId, playableType, entity);
        } else if (Objects.equals(entity, "deck")) {
            restClient.build(playableId, playableType, entity);
        } else if (Objects.equals(entity, "bridge")) {
            restClient.build(playableId, playableType, entity);
        } else if (Objects.equals(entity, "factory")) {
            restClient.build(playableId, playableType, entity);
        }
    }

    private boolean onePointTurn(int currentButtonId) {
        // Check if the previous and current directions are 90-degree turns
        if ((lastPressedButtonId == R.id.buttonUp && currentButtonId == R.id.buttonLeft) ||
                (lastPressedButtonId == R.id.buttonUp && currentButtonId == R.id.buttonRight) ||
                (lastPressedButtonId == R.id.buttonDown && currentButtonId == R.id.buttonLeft) ||
                (lastPressedButtonId == R.id.buttonDown && currentButtonId == R.id.buttonRight) ||
                (lastPressedButtonId == R.id.buttonLeft && currentButtonId == R.id.buttonUp) ||
                (lastPressedButtonId == R.id.buttonLeft && currentButtonId == R.id.buttonDown) ||
                (lastPressedButtonId == R.id.buttonRight && currentButtonId == R.id.buttonUp) ||
                (lastPressedButtonId == R.id.buttonRight && currentButtonId == R.id.buttonDown)) {
            return true;
        }
        return false;
    }

    @Background
    public void turnOrMove(int currentButtonId, long playableId, int playableType, byte direction) {
        if (lastPressedButtonId != -1 && onePointTurn(currentButtonId)) {
//            Log.d(TAG, "One-point turn detected: from " + lastPressedButtonId + " to " + viewId);
            this.turnAsync(playableId, playableType, direction);
        } else {
            this.moveAsync(playableId, playableType, direction);
        }
        lastPressedButtonId = currentButtonId;
    }

    @Background
    public void strafeLeft(int currentButtonId, long playableId, int playableType) {
        if (lastPressedButtonId != -1 && onePointTurn(currentButtonId)) {
            this.turnAsync(playableId, playableType, (byte) 6);
            this.moveAsync(playableId, playableType, (byte) 6);
            this.moveAsync(playableId, playableType, (byte) 6);
        } else {
            this.moveAsync(playableId, playableType, (byte) 6);
            this.moveAsync(playableId, playableType, (byte) 6);
        }
        lastPressedButtonId = currentButtonId;
    }

    @Background
    public void strafeRight(int currentButtonId, long playableId, int playableType) {
        if (lastPressedButtonId != -1 && onePointTurn(currentButtonId)) {
            this.turnAsync(playableId, playableType, (byte) 2);
            this.moveAsync(playableId, playableType, (byte) 2);
            this.moveAsync(playableId, playableType, (byte) 2);
        } else {
            this.moveAsync(playableId, playableType, (byte) 2);
            this.moveAsync(playableId, playableType, (byte) 2);
        }
        lastPressedButtonId = currentButtonId;
    }

    @Background
    public void fire(long playableId, int playableType) {
        restClient.fire(playableId, playableType);
    }

}