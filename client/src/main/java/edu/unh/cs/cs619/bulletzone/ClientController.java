package edu.unh.cs.cs619.bulletzone;

import android.content.Context;
import android.util.Log;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EBean;
import org.androidannotations.rest.spring.annotations.RestService;
import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import edu.unh.cs.cs619.bulletzone.events.ItemPickupEvent;
import edu.unh.cs.cs619.bulletzone.rest.BZRestErrorhandler;
import edu.unh.cs.cs619.bulletzone.rest.BulletZoneRestClient;
import edu.unh.cs.cs619.bulletzone.util.BooleanWrapper;
import edu.unh.cs.cs619.bulletzone.util.FileHelper;
import edu.unh.cs.cs619.bulletzone.util.ReplayData;
import edu.unh.cs.cs619.bulletzone.util.ReplayDataFlat;

@EBean
public class ClientController {
    private static final String TAG = "ClientController";

    @RestService
    BulletZoneRestClient restClient;

    private ReplayData replayData = ReplayData.getReplayData();

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

    @Background
    public void getLifeAsync(int playableId) {
        int newLifeBuilder = restClient.getLife(playableId, 2).getResult();
        int newLifeTank = restClient.getLife(playableId, 1).getResult();
        int newLifeSoldier = restClient.getLife(playableId, 3).getResult();

//        Log.d("getLifeAsync", "Tank Life from Server: " + newLifeTank);

        PlayerData.getPlayerData().setTankLife(newLifeTank);
        PlayerData.getPlayerData().setBuilderLife(newLifeBuilder);

        if (newLifeSoldier >= 0) {
            PlayerData.getPlayerData().setSoldierLife(newLifeSoldier);
        }

//        Log.d("LifeCheck", "Builder Life: " + PlayerData.getPlayerData().getBuilderLife() + "\n");
//        Log.d("LifeCheck", "Goblin Life: " + PlayerData.getPlayerData().getTankLife() + "\n");
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

    public void updateReplays(Context context) {
        FileHelper fileHelper = new FileHelper(context);

        List<ReplayDataFlat> replayDataList = new ArrayList<>();

        if (!fileHelper.replayFileExists("Replays")) {
            fileHelper.saveReplayList(
                    "Replays", List.of(replayData.turnToFlat()));
        } else {
            replayDataList = fileHelper.loadReplayList("Replays");
            if (replayDataList.size() == 5 || replayDataList.size() == 4) {
                fileHelper.saveReplayList("Replays", List.of(
                        replayData.turnToFlat(),
                        replayDataList.get(0),
                        replayDataList.get(1),
                        replayDataList.get(2),
                        replayDataList.get(3)));
            } else if (replayDataList.size() == 3) {
                fileHelper.saveReplayList("Replays", List.of(
                        replayData.turnToFlat(),
                        replayDataList.get(0),
                        replayDataList.get(1),
                        replayDataList.get(2)));
            } else if (replayDataList.size() == 2) {
                fileHelper.saveReplayList("Replays", List.of(
                        replayData.turnToFlat(),
                        replayDataList.get(0),
                        replayDataList.get(1)));
            } else if (replayDataList.size() == 1) {
                fileHelper.saveReplayList("Replays", List.of(
                        replayData.turnToFlat(),
                        replayDataList.get(0)));
            }
        }
    }

    @Background
    public void ejectPowerUpAsync(long tankId) {
        restClient.ejectPowerUp(tankId);
    }

    @Background
    public void ejectSoldierAsync(long tankId) {
        restClient.ejectSoldier(tankId);
    }
}