package edu.unh.cs.cs619.bulletzone;

import android.content.Context;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.rest.spring.annotations.RestService;
import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import edu.unh.cs.cs619.bulletzone.events.ItemPickupEvent;
import edu.unh.cs.cs619.bulletzone.rest.BZRestErrorhandler;
import edu.unh.cs.cs619.bulletzone.rest.BulletZoneRestClient;
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
    public void repairAsync(long playableId) {
        try {
            restClient.repairPlayable(playableId);
            Log.d(TAG, "Repair request sent for tank: " + playableId);
        } catch (Exception e) {
            Log.e(TAG, "Error repairing tank: " + e.getMessage());
        }
    }

    @Background
    public void postLifeAsync(int playableId, int playableType, ClientActivity context) {
        try {
            // If it's our own playable, use cached values
            if (playableId == PlayerData.getPlayerData().getTankId()) {
                int health = -1;
                switch (playableType) {
                    case 0: // Tank
                        health = PlayerData.getPlayerData().getTankLife();
                        break;
                    case 1: // Builder
                        health = PlayerData.getPlayerData().getBuilderLife();
                        break;
                    case 2: // Soldier
                        health = PlayerData.getPlayerData().getSoldierLife();
                        break;
                }
                if (health != -1) {
                    showLifeToast(health, context);
                    return;
                }
            }

            int life = restClient.getLife(playableId, playableType).getResult();
            showLifeToast(life, context);
        } catch (Exception e) {
//            Log.e(TAG, "Error getting life", e);
            showLifeToast(-1, context);
        }
    }

    public void setRestClient(BulletZoneRestClient restClient) {
        this.restClient = restClient;
    }

    @UiThread
    void showLifeToast(int life, ClientActivity context) {
        if (life != -1) {
            Toast.makeText(context, "Health: " + life, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, "Failed to fetch life.", Toast.LENGTH_SHORT).show();
        }
    }

    @Background
    public void getLifeAsync(int playableId, int playableType) {
        try {
            if (playableType == 0) {
                int oldLife = PlayerData.getPlayerData().getTankLife();

                // Try to repair if repair kit is active and health isn't full
                if (PlayerData.getPlayerData().isRepairKitActive() && oldLife < 100) {
                    repairAsync(playableId);
                    SystemClock.sleep(50);
                }

                int newLifeTank = restClient.getLife(playableId, playableType).getResult();
                if (newLifeTank >= 0) {  // Only update if valid response
                    PlayerData.getPlayerData().setTankLife(newLifeTank);
                }
            } else if (playableType == 1) {
                int oldLife = PlayerData.getPlayerData().getBuilderLife();

                if (PlayerData.getPlayerData().isRepairKitActive() && oldLife < 80) {
                    repairAsync(playableId);
                    SystemClock.sleep(50);
                }

                int newLifeBuilder = restClient.getLife(playableId, playableType).getResult();
                if (newLifeBuilder >= 0) {
                    PlayerData.getPlayerData().setBuilderLife(newLifeBuilder);
                }
            } else if (playableType == 2) {
                int oldLife = PlayerData.getPlayerData().getSoldierLife();

                if (PlayerData.getPlayerData().isRepairKitActive() && oldLife < 25) {
                    repairAsync(playableId);
                    SystemClock.sleep(50);
                }

                int newLifeSoldier = restClient.getLife(playableId, playableType).getResult();
                if (newLifeSoldier >= 0) {
                    PlayerData.getPlayerData().setSoldierLife(newLifeSoldier);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting life: " + e.getMessage());
        }
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

        switch (itemType) {
            case 1: // Thingamajig
                double amount = 100 + random.nextDouble() * 900; // Random 100-1000
                EventBus.getDefault().post(new ItemPickupEvent(itemType, amount));
                break;
            case 2: // AntiGrav
                EventBus.getDefault().post(new ItemPickupEvent(itemType, 300));
                break;
            case 3: // FusionReactor
                EventBus.getDefault().post(new ItemPickupEvent(itemType, 400));
                break;
            case 4: // DeflectorShield
                EventBus.getDefault().post(new ItemPickupEvent(itemType, 300));
                break;
            case 5: // RepairKit
                EventBus.getDefault().post(new ItemPickupEvent(itemType, 200));
                break;
            default:
                Log.w(TAG, "Unknown item type: " + itemType);
                break;
        }
    }

    public void updateReplays(Context context) {
        FileHelper fileHelper = new FileHelper(context);
        List<ReplayDataFlat> replayDataList = new ArrayList<>();

        if (!fileHelper.replayFileExists("Replays")) {
            fileHelper.saveReplayList("Replays", List.of(replayData.turnToFlat()));
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