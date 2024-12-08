/**
 * Builder class that extends the playable abstract class
 * Made by Flynn O'Sullivan and Kyle Goodwin
 */
package edu.unh.cs.cs619.bulletzone.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import org.greenrobot.eventbus.EventBus;

import edu.unh.cs.cs619.bulletzone.model.events.HitEvent;

public class Soldier extends Playable {

    private static final String TAG = "Soldier";

    private boolean recentlyEnteredTank;  // Tracks if the soldier recently re-entered a tank

    public Soldier(long id, Direction direction, String ip) {
        super(id, direction, ip);
        life = 25;  // Soldiers start with 25 life points
        playableType = 3;

        numberOfBullets = 0;
        allowedFireInterval = 250;  // Minimum 250 ms between shots
        allowedNumberOfBullets = 6; // Soldiers can fire up to 6 bullets
        lastFireTime = 0;
        bulletDamage = 5;

        allowedMoveInterval = 1000; // Soldiers can move no faster than once per second
        lastMoveTime = 0;
        moveMultiplier = 1;

        allowedTurnInterval = 0; // Soldiers can turn as fast as they want
        lastTurnTime = 0;

        lastEntryTime = 0;
        allowedDeployInterval = 5000;

        recentlyEnteredTank = false;
        this.powerUpManager = new PowerUpManager(allowedMoveInterval, allowedFireInterval, PlayableType.SOLDIER);
    }

    @Override
    public boolean handleTerrainConstraints(Terrain terrain, long millis) {
        if (terrain.isForest() && millis < (getLastMoveTime() + (getAllowedMoveInterval() * 1.25))) {
            return false;
        }
        return true;
    }

    @Override
    public boolean handleImprovements(Improvement improvement, long millis) {
        if (improvement.isRoad() && millis < (getLastMoveTime() + (getAllowedMoveInterval() / 2))) {
            return false;
        } else if (improvement.isBridge() && millis < (getLastMoveTime() + getAllowedMoveInterval())) {
            return false;
        } else if (improvement.isDeck() && millis < (getLastMoveTime() + getAllowedMoveInterval())) {
            return false;
        }
        return true;
    }

    // Copy method for Soldier
    @Override
    public FieldEntity copy() {
        Soldier copy = new Soldier(id, direction, ip);
        copy.life = this.life;
        return copy;
    }

    // Method to apply damage to the Soldier
    @Override
    public void hit(int damage) {
        int finalDamage = powerUpManager.processDamage(damage);
        life -= finalDamage;
        if (life <= 0) {
            System.out.println("Soldier has been eliminated.");
        }
        EventBus.getDefault().post(new HitEvent(
                (int) id,
                3,
                powerUpManager.getShieldHealth(),
                finalDamage
        ));
    }

    // Method to handle re-entering a tank
    public void enterTank() {
        life = 25;  // Restore to full health on re-entry
        recentlyEnteredTank = true;
        System.out.println("Soldier re-entered tank with full health.");
    }

    // Helper method to check if the soldier can exit the tank
    public boolean canExitTank(long currentTimeMillis) {
        // Soldier cannot be ejected for 3 seconds after re-entering
        return !recentlyEnteredTank || (currentTimeMillis - lastMoveTime >= 3000);
    }

    // Reset recently entered status after enough time has passed
    public void resetEntryStatus(long currentTimeMillis) {
        if (recentlyEnteredTank && (currentTimeMillis - lastMoveTime >= 3000)) {
            recentlyEnteredTank = false;
        }
    }

    @JsonIgnore

    @Override
    public int getIntValue() {
        return (int) (30000000 + 10000 * id + 10 * life + Direction.toByte(direction));
    }

    @Override
    public String toString() {
        return "S";
    }


}

