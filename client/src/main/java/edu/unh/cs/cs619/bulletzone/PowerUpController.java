package edu.unh.cs.cs619.bulletzone;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EBean;
import org.androidannotations.rest.spring.annotations.RestService;
import org.greenrobot.eventbus.EventBus;

import edu.unh.cs.cs619.bulletzone.events.PowerUpEjectEvent;
import edu.unh.cs.cs619.bulletzone.rest.BulletZoneRestClient;
import edu.unh.cs.cs619.bulletzone.util.BooleanWrapper;

@EBean
public class PowerUpController {

    @RestService
    BulletZoneRestClient restClient;

    @Background
    public void ejectPowerUpAsync(long tankId, int powerUpType) {
        BooleanWrapper result = restClient.ejectPowerUp(tankId);
        if (result.isResult()) {
            EventBus.getDefault().post(new PowerUpEjectEvent(powerUpType));
        }
    }
}