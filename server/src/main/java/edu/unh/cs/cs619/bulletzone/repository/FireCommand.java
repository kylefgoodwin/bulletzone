package edu.unh.cs.cs619.bulletzone.repository;

import org.greenrobot.eventbus.EventBus;
import org.springframework.stereotype.Component;

import java.util.TimerTask;

import edu.unh.cs.cs619.bulletzone.model.Bullet;
import edu.unh.cs.cs619.bulletzone.model.Direction;
import edu.unh.cs.cs619.bulletzone.model.FieldHolder;
import edu.unh.cs.cs619.bulletzone.model.Game;
import edu.unh.cs.cs619.bulletzone.model.Item;
import edu.unh.cs.cs619.bulletzone.model.Playable;
import edu.unh.cs.cs619.bulletzone.model.Tank;
import edu.unh.cs.cs619.bulletzone.model.TankDoesNotExistException;
import edu.unh.cs.cs619.bulletzone.model.Terrain;
import edu.unh.cs.cs619.bulletzone.model.Wall;
import edu.unh.cs.cs619.bulletzone.model.events.HitEvent;
import edu.unh.cs.cs619.bulletzone.model.events.MoveEvent;
import edu.unh.cs.cs619.bulletzone.model.events.RemoveEvent;
import edu.unh.cs.cs619.bulletzone.model.events.TurnEvent;

@Component
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

    public int assignBulletId(int[] trackActiveBullets, Playable playable) {
        int bulletId = -1;
        for(int i = 0; i < playable.getAllowedNumberOfBullets(); i++){
            if (trackActiveBullets[i] == 0) {
                bulletId = i;
                trackActiveBullets[i] = 1;
                break;
            }
        }
        return bulletId;
    }

    public void handleRemovingPlayable(FieldHolder currentField, Playable playable, int playableType, Game game){
        playable.getParent().clearField();
        playable.setParent(new FieldHolder(currentField.getPosition()));
        if (playableType == 1){
            game.removeTank(playable.getId());
        } else if (playableType == 2){
            game.removeBuilder(playable.getId());
        } else if (playableType == 3){
            game.removeSoldier(playable.getId());
            game.getTanks().get(playable.getId()).sethasSoldier(false);
        }
    }

    public void moveBulletAndHandleCollision(Game game, Bullet bullet, Playable playable, int playableType, int[] trackActiveBullets, TimerTask timerTask) {
        FieldHolder currentField = bullet.getParent();
        Direction direction = bullet.getDirection();
        FieldHolder nextField = currentField.getNeighbor(direction);

        boolean isVisible = currentField.isPresent() && (currentField.getEntity() == bullet);

        try {
            if (nextField.isPresent()) {
                nextField.getEntity().hit(bullet.getDamage());

                //Handle damaging playable
                if (nextField.getEntity().isPlayable()) {
                    Playable p = (Playable) nextField.getEntity();
                    System.out.println("Playable is hit, life: " + p.getLife());
                    if (p.getLife() <= 0) {
                        handleRemovingPlayable(currentField, p, p.getPlayableType(), game);
                    }
                //Handle hitting wall
                } else if (nextField.getEntity().isWall()) {
                    Wall w = (Wall) nextField.getEntity();
                    if (w.getIntValue() > 1000 && w.getIntValue() <= 2000) {
                        game.getHolderGrid().get(w.getPos()).clearField();
                    }
                //Handle hitting an item (remove it)
                } else if (nextField.getEntity().isItem()) {
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

                if (nextField.isTerrainPresent()){
                    Terrain t = (Terrain) nextField.getTerrainEntityHolder();
                    if (t.isForest()) {
                        if (isVisible) {
                            currentField.clearField();
                        }
                        EventBus.getDefault().post(new RemoveEvent(bullet.getIntValue(), bullet.getPosition()));
                        trackActiveBullets[bullet.getBulletId()] = 0;
                        playable.setNumberOfBullets(Math.max(0, playable.getNumberOfBullets() - 1));
                        timerTask.cancel();
                        return;
                    }
                }

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