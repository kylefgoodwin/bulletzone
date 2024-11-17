package edu.unh.cs.cs619.bulletzone;

import android.util.Log;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EBean;
import org.androidannotations.rest.spring.annotations.RestService;
import org.greenrobot.eventbus.EventBus;

import java.util.Objects;

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
    private int lastPressedButtonId = -1;
    ClientController clientController;
    PlayerData playerData;
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
        // send build
        if (Objects.equals(entity, "destructibleWall")) {
            if (restClient.getBalance(userId) >= 80.0) {
                restClient.build(playableId, playableType, entity);
                // Remove credits to user's account
                double amount = -80.0;
                Log.d(TAG, "Processing build on Destructible Wall, amount: " + amount);

                BooleanWrapper result = restClient.depositBalance(userId, amount);
                if (result != null && result.isResult()) {
                    Log.d(TAG, "Successfully withdrew " + amount + " credits");
                } else {
                    Log.e(TAG, "Failed to withdraw credits");
                }
            }

        } else if (Objects.equals(entity, "indestructibleWall")) {
            if (restClient.getBalance(userId) >= 150.0) {
                restClient.build(playableId, playableType, entity);
                // Remove credits to user's account
                double amount = -150.0;
                Log.d(TAG, "Processing build on Indestructible Wall, amount: " + amount);

                BooleanWrapper result = restClient.depositBalance(userId, amount);
                if (result != null && result.isResult()) {
                    Log.d(TAG, "Successfully withdrew " + amount + " credits");
                } else {
                    Log.e(TAG, "Failed to withdraw credits");
                }
            }
        } else if (Objects.equals(entity, "miningFacility")) {
            if (restClient.getBalance(userId) >= 300.0) {
                restClient.build(playableId, playableType, entity);
                // Remove credits to user's account
                double amount = -300.0;
                Log.d(TAG, "Processing build on Mining Facility, amount: " + amount);

                BooleanWrapper result = restClient.depositBalance(userId, amount);
                if (result != null && result.isResult()) {
                    Log.d(TAG, "Successfully withdrew " + amount + " credits");
                } else {
                    Log.e(TAG, "Failed to withdraw credits");
                }
            }
        }

    }

    /**
     * Sends either a dismantle or build command depending on what unit is currently selected
     *
     * @param playableId  unit id currently selected
     * @param entity currently selected improvement type
     */
    @Background
    public void dismantleAsync(long playableId, int playableType, String entity) {
        // send build
        if (Objects.equals(entity, "destructibleWall")) {
            restClient.build(playableId, playableType, entity);
            // Remove credits to user's account
            double amount = 80.0;
            PlayerData playerData = PlayerData.getPlayerData();
            Log.d(TAG, "Processing build on Destructible Wall, amount: " + amount);

            BooleanWrapper result = restClient.depositBalance(playerData.getUserId(), amount);
            if (result != null && result.isResult()) {
                Log.d(TAG, "Successfully withdrew " + amount + " credits");
            } else {
                Log.e(TAG, "Failed to withdraw credits");
            }
        } else if (Objects.equals(entity, "indestructibleWall")) {
            restClient.build(playableId, playableType, entity);
            // Remove credits to user's account
            double amount = 150.0;
            PlayerData playerData = PlayerData.getPlayerData();
            Log.d(TAG, "Processing build on Indestructible Wall, amount: " + amount);

            BooleanWrapper result = restClient.depositBalance(playerData.getUserId(), amount);
            if (result != null && result.isResult()) {
                Log.d(TAG, "Successfully withdrew " + amount + " credits");
            } else {
                Log.e(TAG, "Failed to withdraw credits");
            }
        } else if (Objects.equals(entity, "miningFacility")) {
            restClient.build(playableId, playableType, entity);
            // Remove credits to user's account
            double amount = 300.0;
            PlayerData playerData = PlayerData.getPlayerData();
            Log.d(TAG, "Processing build on Mining Facility, amount: " + amount);

            BooleanWrapper result = restClient.depositBalance(playerData.getUserId(), amount);
            if (result != null && result.isResult()) {
                Log.d(TAG, "Successfully withdrew " + amount + " credits");
            } else {
                Log.e(TAG, "Failed to withdraw credits");
            }
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
    public void fire(long playableId, int playableType) {
        restClient.fire(playableId, playableType);
    }

}