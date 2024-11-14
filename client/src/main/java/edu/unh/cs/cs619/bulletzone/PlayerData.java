package edu.unh.cs.cs619.bulletzone;

public class PlayerData {
    private static PlayerData playerData = null;
    private long tankId = -1;
    private long userId = -1;
    private int moveInterval = 500;  // Base move interval
    private int fireInterval = 1500; // Base fire interval
    private int activePowerUps = 0; // Track number of active power-ups
    private int lastPowerUpType = 0;  // 0=none, 2=AntiGrav, 3=FusionReactor

    private PlayerData() {} // Private constructor for singleton

    public static synchronized PlayerData getPlayerData() {
        if (playerData == null) {
            playerData = new PlayerData();
        }
        return playerData;
    }

    public void setTankId(long tankId) {
        this.tankId = tankId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public int getMoveInterval() {
        return moveInterval;
    }

    public void setMoveInterval(int interval) {
        if (interval > 0) {
            this.moveInterval = interval;
        }
    }

    public int getFireInterval() {
        return fireInterval;
    }

    public void setFireInterval(int interval) {
        if (interval > 0) {
            this.fireInterval = interval;
        }
    }

    public void incrementPowerUps() {
        activePowerUps++;
    }

    public void decrementPowerUps() {
        if (activePowerUps > 0) {
            activePowerUps--;
        }
    }

    public int getActivePowerUps() {
        return activePowerUps;
    }

    public void resetPowerUps() {
        activePowerUps = 0;
        moveInterval = 500;   // Reset to base value
        fireInterval = 1500;  // Reset to base value
    }

    public long getTankId() {
        return tankId;
    }


    public long getUserId() {
        return userId;
    }

    public void setLastPowerUpType(int type) {
        this.lastPowerUpType = type;
    }

    public int getLastPowerUpType() {
        return lastPowerUpType;
    }

    public void clearLastPowerUpType() {
        this.lastPowerUpType = 0;
    }
}