package edu.unh.cs.cs619.bulletzone;

public class PlayerData {
    private static PlayerData playerData = null;
    private long tankId = -1;
    private long userId = -1;
    private int moveInterval = 500;  // Base move interval
    private int fireInterval = 1500; // Base fire interval
    private int activePowerUps = 0; // Track number of active power-ups
    private int fusionReactorCount = 0;
    private int antiGravCount = 0;
    private Long builderLife;
    private Long tankLife;
    private Long soldierLife;

    private PlayerData() {} // Private constructor for singleton

    public static synchronized PlayerData getPlayerData() {
        if (playerData == null) {
            playerData = new PlayerData();
        }
        return playerData;
    }

    // Life-related methods
    public Long getTankLife() {
        return tankLife;
    }

    public void setTankLife(Long tankLife) {
        this.tankLife = tankLife;
    }

    public Long getSoldierLife() {
        return soldierLife;
    }

    public void setSoldierLife(Long soldierLife) {
        this.soldierLife = soldierLife;
    }

    public Long getBuilderLife() {
        return builderLife;
    }

    public void setBuilderLife(Long builderLife) {
        this.builderLife = builderLife;
    }

    public long getTankId() {
        return tankId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    // ID-related methods
    public void setTankId(long tankId) {
        this.tankId = tankId;
    }

    public long getUserId() {
        return userId;
    }

    // Interval getters and setters
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

    // Power-up management methods
    public void incrementPowerUps(int type) {
        activePowerUps++;
        if (type == 2) { // AntiGrav
            antiGravCount++;
        } else if (type == 3) { // FusionReactor
            fusionReactorCount++;
        }
    }

    public void decrementPowerUps(int type) {
        if (activePowerUps > 0) {
            activePowerUps--;
            if (type == 2 && antiGravCount > 0) {
                antiGravCount--;
            } else if (type == 3 && fusionReactorCount > 0) {
                fusionReactorCount--;
            }
        }
    }

    public int getActivePowerUps() {
        return activePowerUps;
    }

    public int getAntiGravCount() {
        return antiGravCount;
    }

    public int getFusionReactorCount() {
        return fusionReactorCount;
    }

    public void resetPowerUps() {
        activePowerUps = 0;
        antiGravCount = 0;
        fusionReactorCount = 0;
        moveInterval = 500;   // Reset to base value
        fireInterval = 1500;  // Reset to base value
    }
}