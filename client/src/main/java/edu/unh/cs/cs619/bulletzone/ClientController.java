package edu.unh.cs.cs619.bulletzone;

import android.util.Log;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EBean;
import org.androidannotations.rest.spring.annotations.RestService;
import org.greenrobot.eventbus.EventBus;

import java.util.Random;

import edu.unh.cs.cs619.bulletzone.events.ItemPickupEvent;
import edu.unh.cs.cs619.bulletzone.rest.BZRestErrorhandler;
import edu.unh.cs.cs619.bulletzone.rest.BulletZoneRestClient;
import edu.unh.cs.cs619.bulletzone.util.BooleanWrapper;

@EBean
public class ClientController {
    private static final String TAG = "ClientController";

    @RestService
    BulletZoneRestClient restClient;

    private final Random random = new Random();

    public ClientController() {}

    @Background
    void leaveGameAsync(long playableId) {
        try {
            restClient.leave(playableId);
        } catch (Exception e) {
            Log.e(TAG, "Error leaving game", e);
        }
    }

    void setErrorHandler(BZRestErrorhandler bzRestErrorhandler) {
        restClient.setRestErrorHandler(bzRestErrorhandler);
    }

    public Double getBalance(long userId) {
        try {
            return restClient.getBalance(userId);
        } catch (Exception e) {
            Log.e(TAG, "Error getting balance", e);
            return null;
        }
    }

    @Background
    public void handleItemPickup(int itemType) {
        Log.d(TAG, "Handling item pickup of type: " + itemType);
        try {
            switch (itemType - 1) { // Adjust for 0-based type
                case 0: // Thingamajig (3001 - 3000)
                    double amount = 100 + random.nextInt(901); // Random amount between 100 and 1000
                    PlayerData playerData = PlayerData.getPlayerData();
                    Log.d(TAG, "Processing Thingamajig pickup, amount: " + amount);

                    BooleanWrapper result = restClient.depositBalance(playerData.getUserId(), amount);
                    if (result != null && result.isResult()) {
                        Log.d(TAG, "Successfully deposited " + amount + " credits");
                        EventBus.getDefault().post(new ItemPickupEvent(1, amount));
                    } else {
                        Log.e(TAG, "Failed to deposit credits");
                    }
                    break;

                case 1: // AntiGrav (3002 - 3000)
                    Log.d(TAG, "Processing AntiGrav pickup");
                    EventBus.getDefault().post(new ItemPickupEvent(2, 0));
                    break;

                case 2: // FusionReactor (3003 - 3000)
                    Log.d(TAG, "Processing FusionReactor pickup");
                    EventBus.getDefault().post(new ItemPickupEvent(3, 0));
                    break;

                default:
                    Log.w(TAG, "Unknown item type: " + itemType);
                    break;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error handling item pickup", e);
        }
    }
}