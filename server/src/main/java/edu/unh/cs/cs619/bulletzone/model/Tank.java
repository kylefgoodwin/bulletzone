package edu.unh.cs.cs619.bulletzone.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import org.greenrobot.eventbus.EventBus;

import edu.unh.cs.cs619.bulletzone.model.events.HitEvent;

public class Tank extends Playable {
    private static final String TAG = "Tank";

    public Tank(long id, Direction direction, String ip) {
        super(id, direction, ip);
        life = 100;
        playableType = 1;

        numberOfBullets = 0;
        allowedFireInterval = 1500;
        allowedNumberOfBullets = 2;
        lastFireTime = 0;
        bulletDamage = 30;

        allowedTurnInterval = 0;
        lastTurnTime = 0;

        allowedMoveInterval = 500;
        lastMoveTime = 0;
        moveMultiplier = 1;  // Initialize move multiplier

        lastEntryTime = 0;
        allowedDeployInterval = 5000;

        powerUpManager = new PowerUpManager(allowedMoveInterval, allowedFireInterval);
        hasSoldier = false;
    }

    @Override
    public boolean handleTerrainConstraints(Terrain terrain, long millis) {
        if (terrain.isHilly() && millis < (getLastMoveTime() + (getAllowedMoveInterval() * 1.5))) {
            return false;
        } else if (terrain.isForest() && millis < (getLastMoveTime() + (getAllowedMoveInterval() * 2))) {
            return false;
        }
        return true;
    }

    @Override
    public boolean canEjectSoldier(){ return true; }

    @Override
    public void hit(int damage) {
        if (powerUpManager != null) {
            int finalDamage = powerUpManager.processDamage(damage);
            life -= finalDamage;

            // Post event with updated shield status
            EventBus.getDefault().post(new HitEvent(
                    (int) id,
                    playableType,
                    powerUpManager.getShieldHealth(),
                    finalDamage
            ));

            if (life <= 0) {
                life = 0;
            }
        } else {
            life -= damage;
        }
    }

    @Override
    public void update() {
        super.update();
        if (powerUpManager != null && powerUpManager.hasActiveRepairKit()) {
            life = Math.min(100, life + 1); // Heal 1 point per second
        }
    }

    public boolean tryEjectPowerUp(FieldHolder target) {
        if (powerUpManager != null && powerUpManager.hasPowerUps()) {
            Item powerUp = powerUpManager.ejectLastPowerUp();
            if (powerUp != null) {
                // Handle immediate destruction of repair kit
                if (powerUp.getIntValue() == 3005) { // RepairKit
                    return true;
                }

                // Place other power-ups on the field
                Direction dropDirection = getDropDirection();
                FieldHolder dropLocation = target.getNeighbor(dropDirection);

                if (dropLocation != null && !dropLocation.isPresent()) {
                    dropLocation.setFieldEntity(powerUp);
                    powerUp.setParent(dropLocation);
                    return true;
                }
            }
        }
        return false;
    }

    private Direction getDropDirection() {
        // Get opposite direction of current facing
        switch (direction) {
            case Up:
                return Direction.Down;
            case Down:
                return Direction.Up;
            case Left:
                return Direction.Right;
            case Right:
                return Direction.Left;
            default:
                return Direction.Down;
        }
    }

    @JsonIgnore
    @Override
    public int getIntValue() {
        return (int) (10000000 + 10000 * id + 10 * life + Direction.toByte(direction));
    }

    @Override
    public String toString() {
        return "T";
    }
}