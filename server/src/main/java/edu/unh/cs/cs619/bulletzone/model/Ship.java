/**
 * Builder class that extends the playable abstract class
 * Made by Flynn O'Sullivan and Kyle Goodwin
 */
package edu.unh.cs.cs619.bulletzone.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import org.greenrobot.eventbus.EventBus;

import edu.unh.cs.cs619.bulletzone.model.events.HitEvent;

public class Ship extends Playable {

    private static final String TAG = "Ship";

    public Ship(long id, Direction direction, String ip) {
        super(id, direction, ip);
        life = 25;  // Soldiers start with 25 life points
        playableType = 3;

        numberOfBullets = 0;
        allowedFireInterval = 500;  // Minimum 500 ms between shots
        allowedNumberOfBullets = 3; // Ships can fire up to 3 bullets
        lastFireTime = 0;
        bulletDamage = 20;

        allowedMoveInterval = 750; // Ships can move no faster than once 3/4 second
        lastMoveTime = 0;
        moveMultiplier = 1;

        allowedTurnInterval = 750; // Ships can turn no faster than once 3/4 second
        lastTurnTime = 0;

        powerUpManager = new PowerUpManager(allowedMoveInterval, allowedFireInterval, PlayableType.SHIP);
    }

    @Override
    public boolean handleTerrainConstraints(Terrain terrain, long millis) {
        if (!terrain.isWater() && millis < (getLastMoveTime() + (getAllowedMoveInterval() * 1.25))) {
            return false;
         }
        setLastMoveTime(millis);
        return true;
    }

    // Copy method for Soldier
    @Override
    public FieldEntity copy() {
        Ship copy = new Ship(id, direction, ip);
        copy.life = this.life;
        return copy;
    }

    // Method to apply damage to the Soldier
    @Override
    public void hit(int damage) {
        int finalDamage = powerUpManager.processDamage(damage);
        life -= finalDamage;
        if (life <= 0) {
            System.out.println("Ship has been eliminated.");
        }
        EventBus.getDefault().post(new HitEvent(
                (int) id,
                4,
                powerUpManager.getShieldHealth(),
                finalDamage
        ));
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

    @JsonIgnore

    @Override
    public int getIntValue() {
        return (int) (40000000 + 10000 * id + 10 * life + Direction.toByte(direction));
    }

    @Override
    public String toString() {
        return "SH";
    }


}
