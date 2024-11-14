/**
 * Made by Kyle Goodwin, 11/4/2024
 * Creates an implementable abstract class that allows for the instantiation of new
 * playable objects ingame. Will make move, turn, etc. code much cleaner and allow
 * for easier enforcement of constraints, and adding new playables if need be.
 */
package edu.unh.cs.cs619.bulletzone.model;

import org.greenrobot.eventbus.EventBus;

import javax.management.ListenerNotFoundException;

import edu.unh.cs.cs619.bulletzone.model.events.HitEvent;
import edu.unh.cs.cs619.bulletzone.model.events.SpawnEvent;

public abstract class Playable extends FieldEntity {
    private static final String TAG = "Playable";


    protected PowerUpManager powerUpManager;

    protected final long id;

    protected final String ip;

    protected long lastMoveTime;
    protected int allowedMoveInterval;
    protected int moveMultiplier;

    protected long lastTurnTime;
    protected int allowedTurnInterval;

    protected long lastFireTime;
    protected int allowedFireInterval;

    protected int numberOfBullets;
    protected int allowedNumberOfBullets;

    protected int life;

    protected Direction direction;

    protected boolean hasSoldier;

    public Playable(long id, Direction direction, String ip) {
        this.id = id;
        this.direction = direction;
        this.ip = ip;
    }

    public FieldEntity copy() {
        return new Tank(id, direction, ip);
    }

    public void hit(int damage) {
        life -= damage;
//        System.out.println("Life: " + life + "\n");
        if (life <= 0) {
            //handle game over scenario
        }
    }

    //Getters
    public long getLastMoveTime() {
        return lastMoveTime;
    }

    public long getAllowedMoveInterval() {
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

    public Direction getDirection() {
        return direction;
    }

    public long getId() {
        return id;
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

    //Setters
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

    public void setDirection(Direction direction) {
        this.direction = direction;
    }
    public void setLife(int life) {
        this.life = life;
    }

    public String toString() {
        return "";
    }

    public long getMoveMultiplier() {
        return moveMultiplier;
    }


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
            return false;
        }

        Direction[] directions = {Direction.Up, Direction.Right, Direction.Down, Direction.Left};

        for (Direction dir : directions) {
            FieldHolder neighbor = currentField.getNeighbor(dir);
            if (!neighbor.isPresent()) {
                Item powerUp = ejectPowerUp();
                if (powerUp != null) {
                    neighbor.setFieldEntity(powerUp);
                    powerUp.setParent(neighbor);
                    EventBus.getDefault().post(new SpawnEvent(powerUp.getIntValue(), neighbor.getPosition()));
                    return true;
                }
            }
        }

        // If no empty square found, just destroy the power-up
        ejectPowerUp();
        return true;
    }
}
