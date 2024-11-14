package edu.unh.cs.cs619.bulletzone.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import org.greenrobot.eventbus.EventBus;

import edu.unh.cs.cs619.bulletzone.model.events.HitEvent;
import edu.unh.cs.cs619.bulletzone.model.events.SpawnEvent;

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
    public void hit(int damage) {
        life -= damage;
        if (life <= 0) {
            //handle game over scenario
        }
        System.out.println("Tank id: " + id + " Tank Life: " + life);
        EventBus.getDefault().post(new HitEvent((int) id, 1));
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