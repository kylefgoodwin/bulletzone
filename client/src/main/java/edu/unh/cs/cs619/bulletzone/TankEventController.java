package edu.unh.cs.cs619.bulletzone;

import android.util.Log;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EBean;
import org.androidannotations.rest.spring.annotations.RestService;

import edu.unh.cs.cs619.bulletzone.rest.BulletZoneRestClient;

@EBean
public class TankEventController {

    @RestService
    BulletZoneRestClient restClient;
    private int lastPressedButtonId = -1;

    public TankEventController() {}

    @Background
    public void moveAsync(long playableId, int playableType, byte direction) {
        Log.d("TankEventController", "Moving playable: id=" + playableId + ", type=" + playableType + ", direction=" + direction);
        try {
            restClient.move(playableId, playableType, direction);
        } catch (Exception e) {
            Log.e("TankEventController", "Error moving: " + e.getMessage());
        }
    }

    @Background
    public void turnAsync(long playableId, int playableType, byte direction) {
        Log.d("TankEventController", "Turning playable: id=" + playableId + ", type=" + playableType + ", direction=" + direction);
        try {
            restClient.turn(playableId, playableType, direction);
        } catch (Exception e) {
            Log.e("TankEventController", "Error turning: " + e.getMessage());
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
        Log.d("TankEventController", "TurnOrMove called: playableId=" + playableId + ", type=" + playableType +
                ", direction=" + direction + ", currentButton=" + currentButtonId);

        if (lastPressedButtonId != -1 && onePointTurn(currentButtonId)) {
            this.turnAsync(playableId, playableType, direction);
        } else {
            this.moveAsync(playableId, playableType, direction);
        }
        lastPressedButtonId = currentButtonId;
    }

    @Background
    public void fire(long playableId, int playableType) {
        Log.d("TankEventController", "Fire called: playableId=" + playableId + ", type=" + playableType);
        try {
            restClient.fire(playableId, playableType);
        } catch (Exception e) {
            Log.e("TankEventController", "Error firing: " + e.getMessage());
        }
    }
}