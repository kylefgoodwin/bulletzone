package edu.unh.cs.cs619.bulletzone;

public class PlayerData {
    private static PlayerData playerData = null;
    private long tankId = -1;
    private long userId = -1;
    private long curId = -1;
    private String curEntity;
    private int currentMap;
    // Associated map
    private int tankMap;
    private int builderMap;

    private long builderId = -1;
    private int moveInterval = 500;  // Base move interval
    private int fireInterval = 1500; // Base fire interval
    private int activePowerUps = 0; // Track number of active power-ups
    private int builderLife = 100;
    private int tankLife = 100;
    private int soldierLife = 100;

    private PlayerData() {
        this.tankMap = 0;
        this.builderMap = 0;
        this.currentMap = 0;
        this.curEntity = "";

    } // Private constructor for singleton

    public static synchronized PlayerData getPlayerData() {
        if (playerData == null) {
            playerData = new PlayerData();
        }
        return playerData;
    }

    public int getTankLife() {
        return tankLife;
    }

    public void setTankLife(int tankLife) {
        this.tankLife = tankLife;
    }

    public int getSoldierLife() {
        return soldierLife;
    }

    public void setSoldierLife(int soldierLife) {
        this.soldierLife = soldierLife;
    }

    public int getBuilderLife() {
        return builderLife;
    }

    public void setBuilderLife(int builderLife) {
        this.builderLife = builderLife;
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

    public long getBuilderId() {
        return builderId;
    }

    public void setBuilderId(long builderId) {
        this.builderId = builderId;
    }

    public long getUserId() {
        return userId;
    }

    public long getCurId() {
        return curId;
    }

    public void setCurId(long curId) {
        this.curId = curId;
    }


    public String getCurEntity() {
        return curEntity;
    }

    public void setCurEntity(String curEntity) {
        this.curEntity = curEntity;
    }

    public int getCurrentMap() {
        return currentMap;
    }

    public void setCurrentMap(int currentMap) {
        this.currentMap = currentMap;
    }

    public int getTankMap() {
        return tankMap;
    }

    public void setTankMap(int tankMap) {
        this.tankMap = tankMap;
    }

    public int getBuilderMap() {
        return builderMap;
    }

    public void setBuilderMap(int builderMap) {
        this.builderMap = builderMap;
    }


}