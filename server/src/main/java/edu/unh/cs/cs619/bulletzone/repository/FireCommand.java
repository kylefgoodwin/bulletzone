package edu.unh.cs.cs619.bulletzone.repository;

import org.greenrobot.eventbus.EventBus;

import java.util.TimerTask;

import edu.unh.cs.cs619.bulletzone.model.Bullet;
import edu.unh.cs.cs619.bulletzone.model.Direction;
import edu.unh.cs.cs619.bulletzone.model.FieldHolder;
import edu.unh.cs.cs619.bulletzone.model.Game;
import edu.unh.cs.cs619.bulletzone.model.Item;
import edu.unh.cs.cs619.bulletzone.model.Playable;
import edu.unh.cs.cs619.bulletzone.model.Tank;
import edu.unh.cs.cs619.bulletzone.model.Wall;
import edu.unh.cs.cs619.bulletzone.model.events.MoveEvent;
import edu.unh.cs.cs619.bulletzone.model.events.RemoveEvent;

public class FireCommand {

    Game game;
    long tankId;
    Direction direction;
    long millis;
    private static final int FIELD_DIM = 16;

    public boolean canFire(Playable playable, long currentTimeMillis, int bulletType, int[] bulletDelay) {
        if (currentTimeMillis < playable.getLastFireTime()) {
            return false;
        }
        if (playable.getNumberOfBullets() == (playable.getAllowedNumberOfBullets())) {
            return false;
        }

        // Use the tank's current fire interval which includes power-up effects
        playable.setLastFireTime(currentTimeMillis + playable.getAllowedFireInterval());
        return true;
    }

    public int assignBulletId(int[] trackActiveBullets) {
        int bulletId = -1;
        if (trackActiveBullets[0] == 0) {
            bulletId = 0;
            trackActiveBullets[0] = 1;
        } else if (trackActiveBullets[1] == 0) {
            bulletId = 1;
            trackActiveBullets[1] = 1;
        }
        return bulletId;
    }

    public void moveBulletAndHandleCollision(Game game, Bullet bullet, Playable playable, int[] trackActiveBullets, TimerTask timerTask) {
        FieldHolder currentField = bullet.getParent();
        Direction direction = bullet.getDirection();
        FieldHolder nextField = currentField.getNeighbor(direction);

        boolean isVisible = currentField.isPresent() && (currentField.getEntity() == bullet);

        try {
            if (nextField.isPresent()) {
                nextField.getEntity().hit(bullet.getDamage());

                if (nextField.getEntity() instanceof Tank) {
                    Tank t = (Tank) nextField.getEntity();
                    System.out.println("Tank is hit, tank life: " + t.getLife());
                    if (t.getLife() <= 0) {
                        t.getParent().clearField();
                        t.setParent(null);
                        game.removeTank(t.getId());
                    }
                } else if (nextField.getEntity() instanceof Wall) {
                    Wall w = (Wall) nextField.getEntity();
                    if (w.getIntValue() > 1000 && w.getIntValue() <= 2000) {
                        game.getHolderGrid().get(w.getPos()).clearField();
                    }
                } else if (nextField.getEntity() instanceof Item) {
                    Item item = (Item) nextField.getEntity();
                    nextField.clearField();
                    EventBus.getDefault().post(new RemoveEvent(item.getIntValue(), item.getPosition()));
                }

                if (isVisible) {
                    currentField.clearField();
                }
                EventBus.getDefault().post(new RemoveEvent(bullet.getIntValue(), bullet.getPosition()));
                trackActiveBullets[bullet.getBulletId()] = 0;
                playable.setNumberOfBullets(Math.max(0, playable.getNumberOfBullets() - 1));
                timerTask.cancel();
            } else {
                if (isVisible) {
                    currentField.clearField();
                }

                int oldPos = bullet.getPosition();
                nextField.setFieldEntity(bullet);
                bullet.setParent(nextField);
                int newPos = bullet.getPosition();
                if (oldPos == playable.getPosition()) {
                    System.out.println("Spawning");
                    EventBus.getDefault().post(new MoveEvent(bullet.getIntValue(), newPos, newPos));
                } else {
                    System.out.println("Moving");
                    EventBus.getDefault().post(new MoveEvent(bullet.getIntValue(), oldPos, newPos));
                }
            }
        } catch (Exception e) {
            System.err.println("Error handling bullet collision: " + e.getMessage());
            e.printStackTrace();
            // Optionally log or notify the server about the error if needed
        }
    }
}