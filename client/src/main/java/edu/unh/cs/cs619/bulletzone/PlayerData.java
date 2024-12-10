package edu.unh.cs.cs619.bulletzone;

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;

import edu.unh.cs.cs619.bulletzone.util.InputCommand;

public class PlayerData {
    private static final String TAG = "PlayerData";
    private static PlayerData instance = null;
    private static final int BASE_MOVE_INTERVAL = 500;
    private static final int BASE_FIRE_INTERVAL = 1500;

    // Basic player data
    private long tankId = -1;
    private long userId = -1;
    private int currentMap = 0;
    private String curEntity = "";
    private int curId = 0;
    private int[] tankMap = new int[5];
    private int[] builderMap = new int[5];
    private int builderNumber = 0;
    private int tankNumber = 0;
    private String[] improvements = new String[10];
    private int improvementNumber = 0;

    // Health and status
    private boolean soldierEjected = false;
    private int tankLife = 100;
    private int builderLife = 80;
    private int soldierLife = 25;
    private long initialTimeStamp;
    private boolean soldierHidden = false; // Is my soldier hidden?

    // Power-up states
    private int activePowerUps = 0;
    private int antiGravCount = 0;
    private int fusionReactorCount = 0;
    private int deflectorShieldCount = 0;
    private int repairKitCount = 0;
    private int shieldHealth = 50;
    private long repairKitExpiration = 0;

    // Movement and combat intervals
    private int moveInterval = BASE_MOVE_INTERVAL;
    private int fireInterval = BASE_FIRE_INTERVAL;

    // Terrain state
    private boolean onHillyTerrain = false;
    private boolean onForestTerrain = false;
    private boolean onRockyTerrain = false;

    // Application Context
    private Context context;

    private PlayerData() {
        resetPlayerData();
    }

    public static PlayerData getPlayerData() {
        if (instance == null) {
            instance = new PlayerData();
        }
        return instance;
    }

    public void resetPlayerData() {
        tankId = -1;
        userId = -1;
        currentMap = 0;
        curEntity = "";
        curId = 0;
        tankNumber = 0;
        builderNumber = 0;
        improvementNumber = 0;
        soldierEjected = false;
        tankLife = 100;
        builderLife = 80;
        soldierLife = 25;
        tankMap = new int[5];
        builderMap = new int[5];
        improvements = new String[10];
        resetPowerUps();
        resetTerrainState();
    }

    public void resetPowerUps() {
        antiGravCount = 0;
        fusionReactorCount = 0;
        deflectorShieldCount = 0;
        repairKitCount = 0;
        shieldHealth = 0;
        activePowerUps = 0;
        moveInterval = BASE_MOVE_INTERVAL;
        fireInterval = BASE_FIRE_INTERVAL;
        repairKitExpiration = 0;
    }

    private void resetTerrainState() {
        onHillyTerrain = false;
        onForestTerrain = false;
        onRockyTerrain = false;
        // Reapply power-up effects after resetting terrain
        updateMovementAndCombatRates();
    }

    public void setTerrainState(boolean hilly, boolean forest, boolean rocky) {
        boolean terrainChanged = (onHillyTerrain != hilly || onForestTerrain != forest || onRockyTerrain != rocky);

        if (terrainChanged) {
            Log.d(TAG, "Previous terrain state - Hilly: " + onHillyTerrain +
                    " Forest: " + onForestTerrain +
                    " Rocky: " + onRockyTerrain);

            onHillyTerrain = hilly;
            onForestTerrain = forest;
            onRockyTerrain = rocky;

            Log.d(TAG, "New terrain state - Hilly: " + hilly +
                    " Forest: " + forest +
                    " Rocky: " + rocky +
                    " for playable type: " + curId);

            // Force refresh of movement and combat rates
            updateMovementAndCombatRates();
        }
    }

    private void updateMovementAndCombatRates() {
        // Reset to base values
        moveInterval = BASE_MOVE_INTERVAL;
        fireInterval = BASE_FIRE_INTERVAL;

        Log.d(TAG, "Base rates - Move: " + moveInterval + " Fire: " + fireInterval);

        // Apply terrain effects first
        if (onHillyTerrain || onForestTerrain || onRockyTerrain) {
            applyTerrainEffects();
            Log.d(TAG, "After terrain effects - Move: " + moveInterval + " Fire: " + fireInterval);
        }

        // Then apply power-up effects
        applyPowerUpEffects();
        Log.d(TAG, "After power-up effects - Move: " + moveInterval + " Fire: " + fireInterval);
    }

    private void applyTerrainEffects() {
        Log.d(TAG, "applyTerrainEffects for unit type: " + curId);

        // Common terrain effects for all units
        if (onHillyTerrain) {
            moveInterval = (int)(moveInterval * 1.5); // 50% slower
            Log.d(TAG, "Applied hilly terrain effect - moveInterval: " + moveInterval);
        }
        if (onForestTerrain) {
            moveInterval = moveInterval * 2; // 100% slower
            fireInterval = (int)(fireInterval * 1.5); // 50% slower firing
            Log.d(TAG, "Applied forest terrain effect - moveInterval: " + moveInterval + " fireInterval: " + fireInterval);
        }
        if (onRockyTerrain) {
            moveInterval = (int)(moveInterval * 1.5); // 50% slower
            Log.d(TAG, "Applied rocky terrain effect - moveInterval: " + moveInterval);
        }
    }

    public void incrementPowerUps(int powerUpType) {
        switch (powerUpType) {
            case 2: // AntiGrav
                antiGravCount++;
                break;
            case 3: // FusionReactor
                fusionReactorCount++;
                break;
            case 4: // DeflectorShield
                deflectorShieldCount++;
                shieldHealth = 50;
                break;
            case 5: // RepairKit
                repairKitCount++;
                repairKitExpiration = System.currentTimeMillis() + 120000;
                Log.d(TAG, "Repair kit added, expires at: " + repairKitExpiration);
                break;
        }
        activePowerUps++;
        updateMovementAndCombatRates();
    }

    public void decrementPowerUps(int powerUpType) {
        switch (powerUpType) {
            case 2: // AntiGrav
                if (antiGravCount > 0) {
                    antiGravCount--;
                    recalculateDelays();
                }
                break;
            case 3: // FusionReactor
                if (fusionReactorCount > 0) {
                    fusionReactorCount--;
                    recalculateDelays();
                }
                break;
            case 4: // DeflectorShield
                if (deflectorShieldCount > 0) {
                    deflectorShieldCount--;
                    shieldHealth = 0; // Immediately remove shield protection
                }
                break;
            case 5: // RepairKit
                if (repairKitCount > 0) {
                    repairKitCount--;
                    repairKitExpiration = 0; // Immediately stop repair effect
                }
                break;
        }
        if (activePowerUps > 0) {
            activePowerUps--;
        }
    }

    public boolean isRepairKitActive() {
        long currentTime = System.currentTimeMillis();
        // Check both count and expiration
        if (repairKitCount > 0 && currentTime < repairKitExpiration) {
            return true;
        } else if (currentTime >= repairKitExpiration) {
            // Auto-cleanup expired repair kits
            if (repairKitCount > 0) {
                repairKitCount = 0;
                repairKitExpiration = 0;
                activePowerUps = Math.max(0, activePowerUps - 1);
            }
        }
        return false;
    }

    private void recalculateDelays() {
        // Reset to base values
        moveInterval = 500;
        fireInterval = 1500;

        // Apply AntiGrav effects
        for (int i = 0; i < antiGravCount; i++) {
            moveInterval /= 2;
            fireInterval += 100;
        }

        // Apply FusionReactor effects
        for (int i = 0; i < fusionReactorCount; i++) {
            fireInterval /= 2;
            moveInterval += 100;
        }
    }

    public long getRepairKitExpiration() {
        return repairKitExpiration;
    }

    public void setRepairKitExpiration(long expiration) {
        this.repairKitExpiration = expiration;
        Log.d(TAG, "Set repair kit expiration to: " + expiration);
    }

    // Existing getters
    public Context getContext() {
        return context;
    }
    public long getTankId() { return tankId; }
    public long getUserId() { return userId; }
    public int getCurrentMap() { return currentMap; }
    public String getCurEntity() { return curEntity; }
    public int getCurId() { return curId; }
    public int[] getTankMap() { return tankMap; }
    public int[] getBuilderMap() { return builderMap; }
    public int getTankNumber() { return tankNumber; }
    public int getBuilderNumber() { return builderNumber; }
    public String[] getAllImprovements() { return improvements; }
    public String getImprovement(int i) { return improvements[i]; }
    public int getImprovementNumber() { return improvementNumber; }
    public boolean getSoldierEjected() { return soldierEjected; }
    public int getTankLife() { return tankLife; }
    public int getBuilderLife() { return builderLife; }
    public int getSoldierLife() { return soldierLife; }
    public int getDeflectorShieldCount() { return deflectorShieldCount; }
    public int getRepairKitCount() { return repairKitCount; }
    public int getShieldHealth() { return shieldHealth; }
    public int getActivePowerUps() { return activePowerUps; }
    public int getAntiGravCount() { return antiGravCount; }
    public int getFusionReactorCount() { return fusionReactorCount; }
    public int getMoveInterval() { return moveInterval; }
    public int getFireInterval() { return fireInterval; }
    public long getInitialTimeStamp() { return initialTimeStamp; }

    // Existing setters
    public void setTankId(long tankId) { this.tankId = tankId; }
    public void setUserId(long userId) { this.userId = userId; }
    public void setCurrentMap(int currentMap) { this.currentMap = currentMap; }
    public String setCurEntity(String curEntity) {
        this.curEntity = curEntity;
        return curEntity;
    }
    public void setCurId(int curId) { this.curId = curId; }
    public void setTankMap(int i) {
        tankMap[tankNumber] = i;
        tankNumber++;
    }
    public void setBuilderMap(int i) {
        builderMap[builderNumber] = i;
        builderNumber++;
    }
    public void setImprovement(int i, String improvement) {
        improvements[i] = improvement;
        improvementNumber++;
    }
    public void setContext(Context context) {
        this.context = context;
    }

    public void updateTerrainEffects(int playableType, boolean isHilly, boolean isForest, boolean isRocky) {
        // Reset to base values first
        moveInterval = 500;
        fireInterval = 1500;

        // Apply terrain effects
        if (playableType == 1) { // Tank
            if (isHilly) {
                moveInterval = (int)(moveInterval * 1.5); // 50% slower movement
            }
            if (isForest) {
                moveInterval = moveInterval * 2; // 100% slower movement
                fireInterval = (int)(fireInterval * 1.5); // 50% slower firing
            }
        } else if (playableType == 2) { // Builder
            if (isRocky) {
                moveInterval = (int)(moveInterval * 1.5); // 50% slower movement
            }
        } else if (playableType == 3) { // Soldier
            if (isForest) {
                moveInterval = (int)(moveInterval * 1.25); // 25% slower movement
            }
        }

        // Apply power-up effects after terrain effects
        applyPowerUpEffects();
    }

    private void applyPowerUpEffects() {
        // Apply AntiGrav effects
        for (int i = 0; i < antiGravCount; i++) {
            moveInterval /= 2;
            fireInterval += 100;
        }

        // Apply FusionReactor effects
        for (int i = 0; i < fusionReactorCount; i++) {
            fireInterval /= 2;
            moveInterval += 100;
        }
    }

    public void setShieldHealth(int health) {
        // Only set shield health if we actually have a shield
        if (deflectorShieldCount > 0) {
            this.shieldHealth = health;
        } else {
            this.shieldHealth = 0;
        }
    }

    public void handleHit(int damage, int shieldHealth) {
        // Update shield health first
        this.shieldHealth = shieldHealth;

        // If shield was depleted, remove the power-up
        if (this.shieldHealth <= 0 && deflectorShieldCount > 0) {
            decrementPowerUps(4);
        }

        // Keep repair kit active even after hits if it hasn't expired
        if (repairKitCount > 0 && System.currentTimeMillis() < repairKitExpiration) {
            // Don't need to do anything - just ensuring we don't deactivate it
        }
    }

    public void setSoldierHidden(boolean hideSoldier) {
        this.soldierHidden = hideSoldier;
    }

    public boolean getSoldierHidden() {
        return this.soldierHidden;
    }

    public void setSoldierEjected(boolean soldierEjected) { this.soldierEjected = soldierEjected; }
    public void setTankLife(int tankLife) { this.tankLife = tankLife; }
    public void setBuilderLife(int builderLife) { this.builderLife = builderLife; }
    public void setSoldierLife(int soldierLife) { this.soldierLife = soldierLife; }
    public void setMoveInterval(int interval) { this.moveInterval = interval; }
    public void setFireInterval(int interval) { this.fireInterval = interval; }
    public void setInitialTimeStamp(long timeStamp) { this.initialTimeStamp = timeStamp; }
}