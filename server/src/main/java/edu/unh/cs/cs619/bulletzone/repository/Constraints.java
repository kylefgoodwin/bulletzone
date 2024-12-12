package edu.unh.cs.cs619.bulletzone.repository;

import static com.google.common.base.Preconditions.checkNotNull;

import org.greenrobot.eventbus.EventBus;
import org.springframework.stereotype.Component;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicLong;

import edu.unh.cs.cs619.bulletzone.model.Bullet;
import edu.unh.cs.cs619.bulletzone.model.Direction;
import edu.unh.cs.cs619.bulletzone.model.FieldHolder;
import edu.unh.cs.cs619.bulletzone.model.Game;
import edu.unh.cs.cs619.bulletzone.model.Tank;
import edu.unh.cs.cs619.bulletzone.model.Wall;
import edu.unh.cs.cs619.bulletzone.model.events.MoveEvent;
import edu.unh.cs.cs619.bulletzone.model.events.RemoveEvent;
import edu.unh.cs.cs619.bulletzone.model.events.TurnEvent;

/**
 * Written by Flynn O'Sullivan, Edited by Kyle Goodwin
 *
 * Splits constraints and allows cleaner code in InMemoryGameRepository to split up
 * checking the game board, and posting new events.
 */
@Component
public class Constraints {

    private final Timer timer = new Timer();
    private final AtomicLong idGenerator = new AtomicLong();
    private static final int FIELD_DIM = 16;
    private final int[] bulletDelay = {500, 1000, 1500};
    private Game game = null;

    public boolean canMove(long tankId, Game game, Direction direction, long currentTimeMillis) {
        Tank tank = game.getTanks().get(tankId);
        if (currentTimeMillis < tank.getLastFireTime()) {
            return false;
        }
        FieldHolder currentField = tank.getParent();
        System.out.println("DIRECTION TO MOVE:" + direction);
        FieldHolder nextField = currentField.getNeighbor(direction);
        checkNotNull(currentField.getNeighbor(direction), "Neightbor is not available");

        boolean isVisible = currentField.isPresent()
                && (currentField.getEntity() == tank);

        // Get the current direction of the tank
        Direction currentDirection = tank.getDirection();

        if (currentDirection != direction) {
            // Check if the direction is a valid turn (sideways)
            if ((currentDirection == Direction.Up && (direction == Direction.Left || direction == Direction.Right))
                    || (currentDirection == Direction.Down && (direction == Direction.Left || direction == Direction.Right))
                    || (currentDirection == Direction.Left && (direction == Direction.Up || direction == Direction.Down))
                    || (currentDirection == Direction.Right && (direction == Direction.Up || direction == Direction.Down))) {
                // Turn the tank and trigger a TurnEvent
                tank.setDirection(direction);
                EventBus.getDefault().post(new TurnEvent(tank.getIntValue(), tank.getPosition()));  // Trigger turn event
                System.out.println("Tank is turning to " + direction);
                return true;  // Tank has turned, no movement yet
            }
        }
        if (!nextField.isPresent()) {
            // If the next field is empty move the user
            int fieldIndex = currentField.getPosition();
            int row = fieldIndex / FIELD_DIM;
            int col = fieldIndex % FIELD_DIM;

            // Check if the tank is at the gameboard edges and trying to move out of bounds
            boolean isAtLeftEdge = (col == 0) && direction == Direction.Left;
            boolean isAtRightEdge = (col == FIELD_DIM - 1) && direction == Direction.Right;
            boolean isAtTopEdge = (row == 0) && direction == Direction.Up;
            boolean isAtBottomEdge = (row == FIELD_DIM - 1) && direction == Direction.Down;

            if (isAtLeftEdge || isAtRightEdge || isAtTopEdge || isAtBottomEdge) {
                System.out.println("Next field is out of bounds, movement blocked.");
                return false;
            }

            // Check if the tank is visible on the field (just to prevent weird cases)
            if (!isVisible) {
                System.out.println("You have already been eliminated.");
                return false;
            }

            int oldPos = tank.getPosition();
            currentField.clearField();
            nextField.setFieldEntity(tank);
            tank.setParent(nextField);
            int newPos = tank.getPosition();
            tank.setDirection(direction);
            EventBus.getDefault().post(new MoveEvent(tank.getIntValue(), oldPos, newPos));

        } else if (nextField.getEntity() instanceof Wall) {
            Wall w = (Wall) nextField.getEntity();
            if (w.getIntValue() > 1000 && w.getIntValue() <= 2000) {
                System.out.println("Next field contains a wall, movement blocked.");
                tank.setDirection(direction);
                return false;
            }

        } else if (nextField.getEntity() instanceof Tank) {
            System.out.println("Next field contains a tank, movement blocked.");
            tank.setDirection(direction);
            return false;
        }
        tank.setLastMoveTime(currentTimeMillis + tank.getAllowedMoveInterval());

        return true;
    }

    public boolean canTurn(long tankId, Game game, Direction direction, long currentTimeMillis){
        Tank tank = game.getTanks().get(tankId);
        if (currentTimeMillis < tank.getLastFireTime()) {
            return false;
        }
        FieldHolder currentField = tank.getParent();
        System.out.println("DIRECTION TO TURN:" + direction);
        checkNotNull(currentField.getNeighbor(direction), "Neightbor is not available");

        boolean isVisible = currentField.isPresent()
                && (currentField.getEntity() == tank);

        // Get the current direction of the tank
        Direction currentDirection = tank.getDirection();

        if (currentDirection != direction) {
            // Check if the direction is a valid turn (sideways)
            if ((currentDirection == Direction.Up && (direction == Direction.Left || direction == Direction.Right))
                    || (currentDirection == Direction.Down && (direction == Direction.Left || direction == Direction.Right))
                    || (currentDirection == Direction.Left && (direction == Direction.Up || direction == Direction.Down))
                    || (currentDirection == Direction.Right && (direction == Direction.Up || direction == Direction.Down))) {
                // Turn the tank and trigger a TurnEvent
                tank.setDirection(direction);
                EventBus.getDefault().post(new TurnEvent(tank.getIntValue(), tank.getPosition()));  // Trigger turn event
                System.out.println("Tank is turning to " + direction);
                return true;  // Tank has turned, no movement yet
            }
        }

        if (!isVisible) {
            System.out.println("You have already been eliminated.");
            return false;
        }

        tank.setLastMoveTime(currentTimeMillis+tank.getAllowedMoveInterval());


        return false;
    }

    public boolean canFire(Tank tank, long currentTimeMillis, int bulletType, int[] bulletDelay) {
        // Check if the tank is allowed to fire (0.5-second interval)

        if (currentTimeMillis < tank.getLastFireTime()) {
            return false;
        }
        if (tank.getNumberOfBullets() == (tank.getAllowedNumberOfBullets())) {
            return false;
        }
        if (bulletType < 1 || bulletType > 3) {
            System.out.println("Bullet type must be 1, 2 or 3, set to 1 by default.");
            bulletType = 1;
        }

        // Update the tank's last fire time
        tank.setLastFireTime(currentTimeMillis + bulletDelay[bulletType - 1]);
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

    public void moveBulletAndHandleCollision(Game game, Bullet bullet, Tank tank, int[] trackActiveBullets, TimerTask timerTask) {
        FieldHolder currentField = bullet.getParent();
        Direction direction = bullet.getDirection();
        FieldHolder nextField = currentField.getNeighbor(direction);

        boolean isVisible = currentField.isPresent() && (currentField.getEntity() == bullet);

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
            }

            if (isVisible) {
                currentField.clearField();
            }
            EventBus.getDefault().post(new RemoveEvent(bullet.getIntValue(), bullet.getPosition(), 0));
            trackActiveBullets[bullet.getBulletId()] = 0;
            tank.setNumberOfBullets(tank.getNumberOfBullets() - 1);
            timerTask.cancel();
        } else {
            if (isVisible) {
                currentField.clearField();
            }

            int oldPos = bullet.getPosition();
            nextField.setFieldEntity(bullet);
            bullet.setParent(nextField);
            int newPos = bullet.getPosition();
            if(oldPos == tank.getPosition()){
                System.out.println("Spawning");
                EventBus.getDefault().post(new MoveEvent(bullet.getIntValue(), newPos, newPos));
            } else {
                System.out.println("Moving");
                EventBus.getDefault().post(new MoveEvent(bullet.getIntValue(), oldPos, newPos));
            }
        }
    }

//    public boolean canFireMoreBullets(Tank tank) {
//        // Check if the tank has reached the maximum number of bullets in the game (Z = 2)
//        return tank.getNumberOfBullets() < (tank.getAllowedNumberOfBullets() + 1);
//    }

}

