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
        playableType = 1;

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

        powerUpManager = new PowerUpManager(BASE_MOVE_INTERVAL, BASE_FIRE_INTERVAL);
        hasSoldier = false;
    }

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

        Item powerUp = powerUpManager.ejectLastPowerUp();
        if (powerUp == null) {
            // Handle repair kit ejection
            EventBus.getDefault().post(new PowerUpEjectEvent(5));
            return true;
        }

        // Update tank stats
        allowedMoveInterval = powerUpManager.getCurrentMovementDelay();
        allowedFireInterval = powerUpManager.getCurrentFireDelay();
        moveMultiplier = 1;  // Reset move multiplier

        Direction dropDirection = getDropDirection();
        FieldHolder dropLocation = target.getNeighbor(dropDirection);

        if (dropLocation != null && !dropLocation.isPresent()) {
            dropLocation.setFieldEntity(powerUp);
            powerUp.setParent(dropLocation);

            // Post both spawn and eject events
            EventBus.getDefault().post(new SpawnEvent(powerUp.getIntValue(), dropLocation.getPosition()));
            EventBus.getDefault().post(new PowerUpEjectEvent(powerUp.getType()));
            return true;
        }

        return false;
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