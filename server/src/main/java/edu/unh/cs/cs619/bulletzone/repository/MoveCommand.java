package edu.unh.cs.cs619.bulletzone.repository;

import static com.google.common.base.Preconditions.checkNotNull;

import org.apache.juli.logging.Log;
import org.greenrobot.eventbus.EventBus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.unh.cs.cs619.bulletzone.datalayer.account.BankAccount;
import edu.unh.cs.cs619.bulletzone.model.Direction;
import edu.unh.cs.cs619.bulletzone.model.FieldHolder;
import edu.unh.cs.cs619.bulletzone.model.Game;
import edu.unh.cs.cs619.bulletzone.model.IllegalTransitionException;
import edu.unh.cs.cs619.bulletzone.model.Item;
import edu.unh.cs.cs619.bulletzone.model.LimitExceededException;
import edu.unh.cs.cs619.bulletzone.model.Playable;
import edu.unh.cs.cs619.bulletzone.model.TankDoesNotExistException;
import edu.unh.cs.cs619.bulletzone.model.Terrain;
import edu.unh.cs.cs619.bulletzone.model.events.MoveEvent;
import edu.unh.cs.cs619.bulletzone.model.events.RemoveEvent;
import edu.unh.cs.cs619.bulletzone.model.events.TerrainUpdateEvent;
import edu.unh.cs.cs619.bulletzone.model.events.TurnEvent;

public class MoveCommand implements Command {
    private static final Logger log = LoggerFactory.getLogger(MoveCommand.class);
    Game game;
    long playableId;
    int playableType;
    Direction direction;
    long millis;
    Playable playable;
    private static final int FIELD_DIM = 16;

    public MoveCommand(Playable playable, int playableType, Game game, Direction direction, long currentTimeMillis) {
        this.playable = playable;
        this.playableType = playableType;
        this.game = game;
        this.direction = direction;
        this.millis = currentTimeMillis;
        this.playableId = playable.getId();
    }

    @Override
    public boolean execute() throws TankDoesNotExistException, IllegalTransitionException, LimitExceededException {
        // Check if enough time has passed since last move
        if (millis < playable.getLastMoveTime()) {
            return false;
        }

        Direction currentDirection = playable.getDirection();
        FieldHolder currentField = playable.getParent();
        FieldHolder nextField = currentField.getNeighbor(direction);
        checkNotNull(currentField.getNeighbor(direction), "Neighbor is not available");

        // Calculate terrain effects first to emit event
        boolean isTerrainField = nextField.isTerrainPresent();
        Terrain t = isTerrainField ? (Terrain) nextField.getTerrainEntityHolder() : null;

        // Always emit terrain event
        TerrainUpdateEvent event = new TerrainUpdateEvent(
                playableType,
                isTerrainField && t != null && t.isHilly(),
                isTerrainField && t != null && t.isForest(),
                isTerrainField && t != null && t.isRocky()
        );
        EventBus.getDefault().post(event);

        // Handle turning first
        if (currentDirection != direction) {
            // For opposite and perpendicular directions
            if ((currentDirection == Direction.Up && (direction == Direction.Down || direction == Direction.Left || direction == Direction.Right)) ||
                    (currentDirection == Direction.Down && (direction == Direction.Up || direction == Direction.Left || direction == Direction.Right)) ||
                    (currentDirection == Direction.Left && (direction == Direction.Right || direction == Direction.Up || direction == Direction.Down)) ||
                    (currentDirection == Direction.Right && (direction == Direction.Left || direction == Direction.Up || direction == Direction.Down))) {
                playable.setDirection(direction);
                EventBus.getDefault().post(new TurnEvent(playable.getIntValue(), playable.getPosition()));
                // Don't update lastMoveTime for rotations
                return true;
            }
        }

        // Only proceed with movement if we're facing the right direction
        if (currentDirection == direction) {
            // Calculate base movement delay
            long moveDelay = playable.getAllowedMoveInterval();

            // Apply terrain modifiers
            if (nextField.isTerrainPresent()) {
                Terrain terrain = (Terrain) nextField.getTerrainEntityHolder();
                // Always emit terrain event
                TerrainUpdateEvent terevent = new TerrainUpdateEvent(
                        playableType,
                        t != null && t.isHilly(),
                        t != null && t.isForest(),
                        t != null && t.isRocky()
                );
                EventBus.getDefault().post(terevent);

                if(playable.handleTerrainConstraints(terrain, millis)){
                    moveUnit(currentField, nextField, playable, direction);
                    return true;
                }
                return false;
                /*if (playableType == 1) { // Tank
                    if (terrain.isHilly()) {
                        moveDelay = (long)(moveDelay * 1.5);
                    } else if (terrain.isForest()) {
                        moveDelay = moveDelay * 2;
                    }
                } else if (playableType == 2) { // Builder
                    if (terrain.isRocky()) {
                        moveDelay = (long)(moveDelay * 1.5);
                    }
                    if (terrain.isForest()) {
                        return false;
                    }
                } else if (playableType == 3) { // Soldier
                    if (terrain.isForest()) {
                        moveDelay = (long)(moveDelay * 1.25);
                    }
                }*/
            }

        // Handle movement to empty space
        if (!nextField.isPresent()) {
            moveUnit(currentField, nextField, playable, direction);
            playable.setLastMoveTime(millis + playable.getAllowedMoveInterval());
            return true;
        } // Soldier re-entry
        else if (nextField.getEntity().isPlayable() && (playableType == 3 && (game.getTanks().get(playableId).gethasSoldier()))) {
            System.out.println("Re-entry");
            if(game.getTanks().get((playableId)).getPosition() == nextField.getPosition()){
                game.removeSoldier(playableId);
                game.getTanks().get(playableId).sethasSoldier(false);
                currentField.clearField();
                game.getTanks().get(playableId).setLastEntryTime(millis);
                EventBus.getDefault().post(new RemoveEvent(playable.getIntValue(), currentField.getPosition()));
                game.setSoldierEjected(false);
                return false;
            }
        }
        // Handle empty space movement
        if (!nextField.isPresent()) {
            moveUnit(currentField, nextField, playable, direction);
            playable.setLastMoveTime(millis + moveDelay);
            return true;
        }

        // Handle item pickup
        if (nextField.getEntity().isItem()) {
            Item item = (Item) nextField.getEntity();
            int itemValue = item.getIntValue();
            int itemPos = nextField.getPosition();

            handleItemPickup(item, playable);

            // Move to item location
            nextField.clearField();
            int oldPos = playable.getPosition();
            currentField.clearField();
            nextField.setFieldEntity(playable);
            playable.setParent(nextField);
            playable.setDirection(direction);

            EventBus.getDefault().post(new RemoveEvent(itemValue, itemPos));
            EventBus.getDefault().post(new MoveEvent(playable.getIntValue(), oldPos, nextField.getPosition()));

            playable.setLastMoveTime(millis + moveDelay);
            return true;
        }

        // Handle collisions
        if (nextField.getEntity().isWall() || nextField.getEntity().isPlayable()) {
            return false;
        }
        }

        return false;
    }

    private boolean handleTerrainConstraints(Playable playable, Terrain t, FieldHolder currentField, FieldHolder nextField) {


        if(playable.handleTerrainConstraints(t, millis)){
            // If we pass timing checks, move the unit
            moveUnit(currentField, nextField, playable, direction);
            // Set appropriate delay based on terrain type
            return true;
        } else {
            return false;
        }
    }

    private void handleItemPickup(Item item, Playable playable) {
        BankAccount balance = game.getBankAccount(playableId);
        switch (item.getType()) {
            case 1: // Thingamajig
                log.debug("Processing Thingamajig pickup for tank {}", playableId);
                double credits = item.getCredits();
                balance.modifyBalance(credits);
                game.modifyBalance(playableId, credits);
                break;
            case 2: // AntiGrav
                log.debug("Processing AntiGrav pickup for tank {}", playableId);
                playable.addPowerUp(item);
                break;
            case 3: // FusionReactor
                log.debug("Processing FusionReactor pickup for tank {}", playableId);
                playable.addPowerUp(item);
                break;
            case 4: // DeflectorShield
                log.debug("Processing DeflectorShield pickup for tank {}", playableId);
                playable.addPowerUp(item);
                break;
            case 5: // RepairKit
                log.debug("Processing RepairKit pickup for tank {}", playableId);
                playable.addPowerUp(item);
                break;
        }
    }

    private void moveUnit(FieldHolder currentField, FieldHolder nextField, Playable playable, Direction direction) {
        int oldPos = playable.getPosition();
        currentField.clearField();
        nextField.setFieldEntity(playable);
        playable.setParent(nextField);
        playable.setDirection(direction);
        EventBus.getDefault().post(new MoveEvent(playable.getIntValue(), oldPos, nextField.getPosition()));
    }

    @Override
    public Long executeJoin() {
        return null;
    }
}