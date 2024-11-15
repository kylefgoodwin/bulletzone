package edu.unh.cs.cs619.bulletzone;

import android.util.Log;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EBean;
import org.androidannotations.rest.spring.annotations.RestService;
import org.greenrobot.eventbus.EventBus;

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

    /**
     * Sends either a dismantle or build command depending on what unit is currently selected
     *
     * @param playableId  unit id currently selected
     * @param entity currently selected improvement type
     */
    @Background
    public void buildOrDismantle(long playableId, int playableType, String entity) {
        if (playableType == 2) {
            // send build
            switch (playerData.getCurEntity()) {
                case "destructibleWall": {
                    double amount = -80;
                    restClient.build(playerData.getBuilderId(), playableType, playerData.getCurEntity());
                    // Remove credits to user's account
                    restClient.depositBalance(PlayerData.getPlayerData().getUserId(), amount);
                    break;
                }
                case "indestructibleWall": {
                    double amount = -150;
                    restClient.build(playerData.getBuilderId(), playableType, playerData.getCurEntity());
                    // Remove credits to user's account
                    restClient.depositBalance(PlayerData.getPlayerData().getUserId(), amount);
                    break;
                }
                case "miningFacility": {
                    double amount = -300;
                    if (restClient.getBalance(PlayerData.getPlayerData().getUserId()) >= 300) {
                        restClient.build(playerData.getBuilderId(), playableType, playerData.getCurEntity());
                        // Remove credits to user's account
                        restClient.depositBalance(PlayerData.getPlayerData().getUserId(), amount);
                        break;
                    }
                }
            }
        } else {
            Log.d(TAG, "Cannot build while controlling another vehicle");
        }
    }

    @Background
    public void buildAsync(long playableId, int playableType, String entity) {
        restClient.build(playableId, playableType, entity);
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