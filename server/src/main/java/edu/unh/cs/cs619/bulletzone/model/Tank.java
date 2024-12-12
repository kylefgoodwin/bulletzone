package edu.unh.cs.cs619.bulletzone.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.greenrobot.eventbus.EventBus;
import edu.unh.cs.cs619.bulletzone.model.events.HitEvent;
import edu.unh.cs.cs619.bulletzone.model.events.PowerUpEjectEvent;
import edu.unh.cs.cs619.bulletzone.model.events.SpawnEvent;

public class Tank extends Playable {
    private static final String TAG = "Tank";
    private static final int BASE_MOVE_INTERVAL = 500;
    private static final int BASE_FIRE_INTERVAL = 1500;

    public Tank(long id, Direction direction, String ip) {
        super(id, direction, ip);
        life = 100;
        playableType = 0;

        numberOfBullets = 0;
        allowedFireInterval = BASE_FIRE_INTERVAL;
        allowedNumberOfBullets = 2;
        lastFireTime = 0;
        bulletDamage = 30;

        allowedTurnInterval = 0;
        lastTurnTime = 0;

        allowedMoveInterval = BASE_MOVE_INTERVAL;
        lastMoveTime = 0;
        moveMultiplier = 1;

        lastEntryTime = 0;
        allowedDeployInterval = 5000;

        this.powerUpManager = new PowerUpManager(BASE_MOVE_INTERVAL, BASE_FIRE_INTERVAL, PlayableType.TANK);
        hasSoldier = false;
    }

    @Override
    public boolean handleTerrainConstraints(Terrain terrain, long millis) {
        if (terrain.isHilly() && millis < (getLastMoveTime() + (getAllowedMoveInterval() * 1.5))) {
            return false;
        } else return !terrain.isForest() || millis >= (getLastMoveTime() + (getAllowedMoveInterval() * 2L));
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

    @Override
    public boolean canEjectSoldier(){ return true; }

    @Override
    public void hit(int damage) {
        if (powerUpManager != null) {
            int finalDamage = powerUpManager.processDamage(damage);
            life -= finalDamage;

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
        if (powerUpManager != null) {
            powerUpManager.update();
            if (powerUpManager.hasActiveRepairKit()) {
                life = Math.min(100, life + 1);
            }
            allowedMoveInterval = powerUpManager.getCurrentMovementDelay();
            allowedFireInterval = powerUpManager.getCurrentFireDelay();
        }
    }

    public boolean tryEjectPowerUp(FieldHolder target) {
        if (!powerUpManager.hasPowerUps()) {
            return false;
        }

        // Try all directions in a specific order
        Direction[] directions = {
                Direction.Up,
                Direction.Right,
                Direction.Down,
                Direction.Left
        };

        // Check each direction for an available spot
        FieldHolder dropLocation = null;
        for (Direction dir : directions) {
            FieldHolder potential = target.getNeighbor(dir);
            if (potential != null && !potential.isPresent()) {
                dropLocation = potential;
                break;
            }
        }

        // If no valid location found, can't drop
        if (dropLocation == null) {
            return false;
        }

        // Try to eject the power-up
        Item powerUp = powerUpManager.ejectLastPowerUp();
        if (powerUp == null) {
            return false;
        }

        // Place the power-up in the world
        dropLocation.setFieldEntity(powerUp);
        powerUp.setParent(dropLocation);

        // Update tank stats
        allowedMoveInterval = powerUpManager.getCurrentMovementDelay();
        allowedFireInterval = powerUpManager.getCurrentFireDelay();
        moveMultiplier = 1;  // Reset move multiplier

        // Post events
        EventBus.getDefault().post(new SpawnEvent(powerUp.getIntValue(), dropLocation.getPosition()));
        EventBus.getDefault().post(new PowerUpEjectEvent(powerUp.getType()));
        return true;
    }

    private Direction getDropDirection() {
        switch (direction) {
            case Up: return Direction.Down;
            case Down: return Direction.Up;
            case Left: return Direction.Right;
            case Right: return Direction.Left;
            default: return Direction.Down;
        }
    }

    @Override
    public boolean canAcceptSoldier(){return true;}


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