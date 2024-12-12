/**
 * Made by Kyle Goodwin, 11/4/2024
 * Creates an implementable abstract class that allows for the instantiation of new
 * playable objects ingame. Will make move, turn, etc. code much cleaner and allow
 * for easier enforcement of constraints, and adding new playables if need be.
 */
package edu.unh.cs.cs619.bulletzone.model;

import org.greenrobot.eventbus.EventBus;

import edu.unh.cs.cs619.bulletzone.model.events.HitEvent;
import edu.unh.cs.cs619.bulletzone.model.events.SpawnEvent;

public abstract class Playable extends FieldEntity {
    private static final String TAG = "Playable";


    protected PowerUpManager powerUpManager;


    protected final long id;
    protected int userId;

    protected final String ip;

    protected long lastMoveTime;
    protected int allowedMoveInterval;
    protected int moveMultiplier;

    protected long lastTurnTime;
    protected int allowedTurnInterval;

    protected long lastFireTime;
    protected int allowedFireInterval;

    protected long lastBuildTime;
    protected int allowBuildInterval;

    protected int numberOfBullets;
    protected int allowedNumberOfBullets;
    protected int bulletDamage;

    protected long lastEntryTime;
    protected int allowedDeployInterval;

    protected int life;
    protected int playableType;

    protected Direction direction;

    protected boolean hasSoldier;
    protected boolean isHealing;

    public Playable(long id, Direction direction, String ip) {
        this.id = id;
        this.direction = direction;
        this.ip = ip;
    }

    private boolean isBuilding = false;
    private boolean isDismantling = false;

    public boolean isBuilding() {
        return isBuilding;
    }

    public boolean isDismantling() {
        return isDismantling;
    }

    public void startBuilding() {
        isBuilding = true;
    }

    public void stopBuilding() {
        isBuilding = false;
    }

    public void startDismantling() {
        isDismantling = true;
    }

    public void stopDismantling() {
        isDismantling = false;
    }

    public FieldEntity copy() {
        return new Tank(id, direction, ip);
    }

    @Override
    public void hit(int damage) {
        int finalDamage = powerUpManager.processDamage(damage);
        life -= finalDamage;
        if (life <= 0) {
            // handle game over
        }
        EventBus.getDefault().post(new HitEvent(
                (int) id,
                playableType,
                powerUpManager.getShieldHealth(),
                finalDamage
        ));
    }

    //Getters
    public int getUserId() {
        return userId;
    }

    public long getLastTurnTime() {
        return lastTurnTime;
    }

    public long getAllowedTurnInterval() {
        return allowedTurnInterval;
    }

    public long getLastMoveTime() {
        return lastMoveTime;
    }

    public int getAllowedMoveInterval() {
        return allowedMoveInterval;
    }

    public long getmoveMultiplier(){
        return moveMultiplier;
    }

    public long getLastFireTime() {
        return lastFireTime;
    }

    public long getAllowedFireInterval() {
        return allowedFireInterval;
    }

    public int getNumberOfBullets() {
        return numberOfBullets;
    }

    public int getAllowedNumberOfBullets() {
        return allowedNumberOfBullets;
    }

    public long getLastEntryTime() {
        return lastEntryTime;
    }

    public int getAllowedDeployInterval() {
        return allowedDeployInterval;
    }

    public long getLastBuildTime() {
        return lastBuildTime;
    }

    public int getAllowBuildInterval() {
        return allowBuildInterval;
    }

    public Direction getDirection() {
        return direction;
    }

    public long getId() {
        return id;
    }

    public int getPlayableType() {
        return playableType;
    }

    public abstract int getIntValue();

    public int getLife() {
        return life;
    }

    public String getIp() {
        return ip;
    }

    public boolean gethasSoldier(){
        return hasSoldier;
    }

    public void sethasSoldier(boolean set){
        hasSoldier = set;
    }

    public boolean getHealing(){
        return isHealing;
    }

    public void setHealing(boolean set){
        isHealing = set;
    }

    public int getBulletDamage() {
        return bulletDamage;
    }

    public void setBulletDamage(int bulletDamage) {
        this.bulletDamage = bulletDamage;
    }

    //Setters
    public void setLastTurnTime(long lastTurnTime) {
        this.lastTurnTime = lastTurnTime;
    }

    public void setAllowedTurnInterval(int allowedTurnInterval) {
        this.allowedTurnInterval = allowedTurnInterval;
    }

    public void setLastMoveTime(long lastMoveTime) {
        this.lastMoveTime = lastMoveTime;
    }

    public void setAllowedMoveInterval(int allowedMoveInterval) {
        this.allowedMoveInterval = allowedMoveInterval;
    }

    public void setMoveMultiplier(int moveMultiplier){
        this.moveMultiplier = moveMultiplier;
    }

    public void setLastFireTime(long lastFireTime) {
        this.lastFireTime = lastFireTime;
    }

    public void setNumberOfBullets(int numberOfBullets) {
        this.numberOfBullets = numberOfBullets;
    }

    public void setAllowedNumberOfBullets(int allowedNumberOfBullets) {
        this.allowedNumberOfBullets = allowedNumberOfBullets;
    }

    public void setLastBuildTime(long lastBuildTime) {
        this.lastBuildTime = lastBuildTime;
    }

    public void setAllowBuildInterval(int allowBuildInterval) {
        this.allowBuildInterval = allowBuildInterval;
    }

    public void setAllowedFireInterval(int allowedFireInterval) {
        this.allowedFireInterval = allowedFireInterval;
    }

    public void setLastEntryTime(long lastMoveTime) {
        this.lastEntryTime = lastMoveTime;
    }

    public void setAllowedDeployInterval(int allowedDeployInterval) {
        this.allowedDeployInterval = allowedDeployInterval;
    }

    public void setDirection(Direction direction) {
        this.direction = direction;
    }
    public void setLife(int life) {
        this.life = life;
    }

    public void setPlayableType(int playableType) {
        this.playableType = playableType;
    }

    public String toString() {
        return "";
    }

    public long getMoveMultiplier() {
        return moveMultiplier;
    }

    public abstract boolean handleTerrainConstraints(Terrain terrain, long millis);

    public abstract boolean handleImprovements(Improvement improvement, long millis);

    public boolean canBuild(){ return false; }
    public boolean canEjectSoldier(){ return false; }
    public boolean canShoot(){ return true; }
    public boolean canAcceptSoldier(){ return false; }


    public void addPowerUp(Item powerUp) {
        powerUpManager.addPowerUp(powerUp);
        updateIntervals();
        if (powerUp.isAntiGrav()) {
            setMoveMultiplier((int)(getMoveMultiplier() * 2)); // Double movement speed
        } else if (powerUp.isFusionReactor()) {
            setMoveMultiplier((int)(getMoveMultiplier() * 0.75)); // Reduce speed by 25%
        }
    }

    public Item ejectPowerUp() {
        Item powerUp = powerUpManager.ejectLastPowerUp();
        updateIntervals();
        if (powerUp != null) {
            if (powerUp.isAntiGrav()) {
                setMoveMultiplier((int)(getMoveMultiplier() / 2)); // Revert speed boost
            } else if (powerUp.isFusionReactor()) {
                setMoveMultiplier((int)(getMoveMultiplier() / 0.75)); // Revert speed reduction
            }
        }
        return powerUp;
    }

    private void updateIntervals() {
        allowedMoveInterval = powerUpManager.getCurrentMovementDelay();
        allowedFireInterval = powerUpManager.getCurrentFireDelay();
    }

    public boolean hasPowerUps() {
        return powerUpManager.hasPowerUps();
    }

    public boolean tryEjectPowerUp(FieldHolder currentField) {
        if (!hasPowerUps()) {
            System.out.println("No power-ups to eject");
            return false;
        }

        System.out.println("Attempting to eject power-up");
        Item powerUp = powerUpManager.ejectLastPowerUp();

        if (powerUp == null) {
            // Power-up was consumed (like repair kit)
            System.out.println("Power-up was consumed on ejection");
            return true;
        }

        // Try to place in an adjacent cell
        Direction[] directions = {Direction.Up, Direction.Right, Direction.Down, Direction.Left};
        for (Direction dir : directions) {
            FieldHolder neighbor = currentField.getNeighbor(dir);
            if (neighbor != null && !neighbor.isPresent()) {
                neighbor.setFieldEntity(powerUp);
                powerUp.setParent(neighbor);
                System.out.println("Power-up ejected to board position: " + neighbor.getPosition());
                EventBus.getDefault().post(new SpawnEvent(powerUp.getIntValue(), neighbor.getPosition()));
                return true;
            }
        }

        // No empty space found - destroy power-up
        System.out.println("No space to eject power-up - destroying it");
        return true;
    }

    /**
     * Updates the state of the playable entity.
     * Called periodically to handle time-based effects and states.
     */
    public void update() {
        long currentTime = System.currentTimeMillis();

        // Update power-up effects if powerUpManager exists
        if (powerUpManager != null) {
            powerUpManager.update();
            updateIntervals();
        }
    }
}