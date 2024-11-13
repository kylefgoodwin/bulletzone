package edu.unh.cs.cs619.bulletzone;

import android.util.Log;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EBean;
import org.androidannotations.rest.spring.annotations.RestService;
import org.greenrobot.eventbus.EventBus;

import edu.unh.cs.cs619.bulletzone.events.PowerUpEjectEvent;
import edu.unh.cs.cs619.bulletzone.rest.BulletZoneRestClient;
import edu.unh.cs.cs619.bulletzone.util.BooleanWrapper;

@EBean
public class PowerUpController {
    private static final String TAG = "PowerUpController";

    @RestService
    BulletZoneRestClient restClient;

    @Background
    public void ejectPowerUpAsync(long tankId) {
        try {
            BooleanWrapper result = restClient.ejectPowerUp(tankId);
            if (result != null && result.isResult()) {
                int powerUpType = getCurrentPowerUpType();
                Log.d(TAG, "Ejecting power-up type: " + powerUpType);
                EventBus.getDefault().post(new PowerUpEjectEvent(powerUpType));
            }
        } catch (Exception e) {
            Log.e(TAG, "Error ejecting power-up: " + e.getMessage());
        }
    }

    private int getCurrentPowerUpType() {
        PlayerData playerData = PlayerData.getPlayerData();
        // Prioritize ejecting FusionReactor first
        if (playerData.getFusionReactorCount() > 0) {
            return 3;
        } else if (playerData.getAntiGravCount() > 0) {
            return 2;
        }
        return 0;
    }
}