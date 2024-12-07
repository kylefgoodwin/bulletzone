package edu.unh.cs.cs619.bulletzone.repository;

import static com.google.common.base.Preconditions.checkNotNull;

import org.apache.juli.logging.Log;
import org.greenrobot.eventbus.EventBus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.unh.cs.cs619.bulletzone.datalayer.account.BankAccount;
import edu.unh.cs.cs619.bulletzone.model.Builder;
import edu.unh.cs.cs619.bulletzone.model.Direction;
import edu.unh.cs.cs619.bulletzone.model.FieldEntity;
import edu.unh.cs.cs619.bulletzone.model.FieldHolder;
import edu.unh.cs.cs619.bulletzone.model.Game;
import edu.unh.cs.cs619.bulletzone.model.IllegalTransitionException;
import edu.unh.cs.cs619.bulletzone.model.Improvement;
import edu.unh.cs.cs619.bulletzone.model.Item;
import edu.unh.cs.cs619.bulletzone.model.LimitExceededException;
import edu.unh.cs.cs619.bulletzone.model.Playable;
import edu.unh.cs.cs619.bulletzone.model.Soldier;
import edu.unh.cs.cs619.bulletzone.model.Tank;
import edu.unh.cs.cs619.bulletzone.model.TankDoesNotExistException;
import edu.unh.cs.cs619.bulletzone.model.Terrain;
import edu.unh.cs.cs619.bulletzone.model.Wall;
import edu.unh.cs.cs619.bulletzone.model.events.MoveEvent;
import edu.unh.cs.cs619.bulletzone.model.events.RemoveEvent;
import edu.unh.cs.cs619.bulletzone.model.events.TerrainUpdateEvent;
import edu.unh.cs.cs619.bulletzone.model.events.TurnEvent;
import edu.unh.cs.cs619.bulletzone.model.events.UIUpdateEvent;

public class MoveCommand implements Command {
    private static final Logger log = LoggerFactory.getLogger(MoveCommand.class);
    Game game;
    long playableId;
    int playableType;
    Direction direction;
    long millis;
    Playable playable;
    private static final int FIELD_DIM = 16;

    /**
     * Constructor for MoveCommand called each time
     * move() is called in InGameMemoryRepository
     *
     * @param playable   playable to move
     *
     * @param direction direction for tank to move
     */
    public MoveCommand(Playable playable, int playableType, Game game, Direction direction, long currentTimeMillis) {
        this.playable = playable;
        this.playableType = playableType;
        this.game = game;
        this.direction = direction;
        this.millis = currentTimeMillis;
        this.playableId = playable.getId();
    }

    /**
     * Command to move a tank with tankId in given direction
     *
     * @return true if moved, false otherwise
     * @throws TankDoesNotExistException  throws error if tank does not exist
     * @throws IllegalTransitionException unsure, not thrown
     * @throws LimitExceededException     unsure, not thrown
     */
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

        // Handle turning first (don't emit terrain events for turns)
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

            // Handle empty space movement
            if (!nextField.isPresent()) {
                // Calculate terrain effects for movement
                boolean isTerrainField = nextField.isTerrainPresent();
                Terrain t = isTerrainField ? (Terrain) nextField.getTerrainEntityHolder() : null;

                // Emit terrain event only for actual movement
                TerrainUpdateEvent event = new TerrainUpdateEvent(
                        isTerrainField && t != null && t.isHilly(),
                        isTerrainField && t != null && t.isForest(),
                        isTerrainField && t != null && t.isRocky(),
                        playableType,
                        currentField.getPosition(),
                        nextField.getPosition()
                );
                EventBus.getDefault().post(event);

                moveUnit(currentField, nextField, playable, direction, false);
                playable.setLastMoveTime(millis + moveDelay);
                return true;
            } else if (nextField.getEntity().isImprovement()) {
                if (nextField.getEntity().isWall()) {
                    playable.setDirection(direction);
                    return false;
                } else if (nextField.getEntity().isIndestructibleWall()) {
                    playable.setDirection(direction);
                    return false;
                } else if (nextField.getEntity().isMiningFacility()) {
                    playable.setDirection(direction);
                    return false;
                } else if (nextField.getEntity().isFactory()) {
                    playable.setDirection(direction);
                    return false;
                }
                Improvement improvement = (Improvement) nextField.getEntity();
                if (!playable.handleImprovements(improvement, millis)) {
                    return false;
                }
                moveUnit(currentField, nextField, playable, direction, false);
                playable.setLastMoveTime(millis + playable.getAllowedMoveInterval());
                return true;
            }
            // Soldier re-entry
            else if (nextField.getEntity().isPlayable() && (playableType == 3 || (playableType == 1 && game.getTanks().get(playableId).gethasSoldier()))) {
                if (game.getTanks().get((playableId)).getPosition() == nextField.getPosition()) {
                    game.removeSoldier(playableId);
                    game.getTanks().get(playableId).sethasSoldier(false);
                    currentField.clearField();
                    game.getTanks().get(playableId).setLastEntryTime(millis);
                    EventBus.getDefault().post(new RemoveEvent(playable.getIntValue(), currentField.getPosition()));
                    game.setSoldierEjected(false);
                    return false;
                }
            }

            // Apply terrain modifiers if movement is possible
            if (nextField.isTerrainPresent()) {
                Terrain terrain = (Terrain) nextField.getTerrainEntityHolder();
                if (playableType == 1) { // Tank
                    if (terrain.isHilly()) {
                        moveDelay = (long) (moveDelay * 1.5);
                    } else if (terrain.isForest()) {
                        moveDelay = moveDelay * 2;
                    }
                } else if (playableType == 2) { // Builder
                    if (terrain.isRocky()) {
                        moveDelay = (long) (moveDelay * 1.5);
                    }
                    if (terrain.isForest()) {
                        return false;
                    }
                } else if (playableType == 3) { // Soldier
                    if (terrain.isForest()) {
                        moveDelay = (long) (moveDelay * 1.25);
                    }
                }
            }

            // Handle item pickup with terrain event
            if (nextField.getEntity().isItem()) {
                Item item = (Item) nextField.getEntity();
                int itemValue = item.getIntValue();
                int itemPos = nextField.getPosition();

                // Emit terrain event for movement to item
                boolean isTerrainField = nextField.isTerrainPresent();
                Terrain t = isTerrainField ? (Terrain) nextField.getTerrainEntityHolder() : null;
                TerrainUpdateEvent event = new TerrainUpdateEvent(
                        isTerrainField && t != null && t.isHilly(),
                        isTerrainField && t != null && t.isForest(),
                        isTerrainField && t != null && t.isRocky(),
                        playableType,
                        currentField.getPosition(),
                        nextField.getPosition()
                );
                EventBus.getDefault().post(event);

                handleItemPickup(item, playable);

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
            else if (nextField.getEntity().isWall() || nextField.getEntity().isPlayable()) {
                playable.setDirection(direction);

                if (nextField.getEntity().isWall()) {
                    Wall wall = (Wall) nextField.getEntity();
                    int wallLife = wall.getLife();
                    int playableLife = playable.getLife();
                    if (wall.getIntValue() != 1000) {
                        if (playableType == 1) {
                            wall.hit((int) Math.ceil(playableLife * 0.1));
                            playable.hit((int) Math.floor(wallLife * 0.16));
                        } else if (playableType == 2) {
                            wall.hit((int) Math.ceil(playableLife * 0.3));
                            playable.hit((int) Math.floor(wallLife * 0.04));
                        } else if (playableType == 3) {
                            wall.hit((int) Math.ceil(playableLife * 0.4));
                            playable.hit((int) Math.floor(wallLife * 0.08));
                        }

                        if (wall.getLife() <= 0) {
                            game.getHolderGrid().get(wall.getPos()).clearField();
                        }

                        if (playable.getLife() <= 0) {
                            handleRemovingPlayable(currentField);
                        }
                    }
                } else { // Handle playable collision
                    Playable otherPlay = (Playable) nextField.getEntity();
                    int otherLife = otherPlay.getLife();
                    int playableLife = playable.getLife();

                    if (playableType == 1) {
                        otherPlay.hit((int) Math.ceil(playableLife * 0.1));
                        playable.hit((int) Math.floor(otherLife * 0.16));
                    } else if (playableType == 2) {
                        otherPlay.hit((int) Math.ceil(playableLife * 0.3));
                        playable.hit((int) Math.floor(otherLife * 0.04));
                    } else if (playableType == 3) {
                        otherPlay.hit((int) Math.ceil(playableLife * 0.4));
                        playable.hit((int) Math.floor(otherLife * 0.08));
                    }

                    if (playable.getLife() <= 0) {
                        handleRemovingPlayable(currentField);
                    }
                }
                return false;
            }
        }
        return false;
    }

    public boolean uIUpdates(Direction direction) {

        Direction currentDirection = direction;
        FieldHolder currentField = playable.getParent();
        FieldHolder nextField = currentField.getNeighbor(direction);
        checkNotNull(currentField.getNeighbor(direction), "Neighbor is not available");

        // Handle turning first
        if (currentDirection != direction) {
            // For opposite and perpendicular directions
            if ((currentDirection == Direction.Up && (direction == Direction.Down || direction == Direction.Left || direction == Direction.Right)) ||
                    (currentDirection == Direction.Down && (direction == Direction.Up || direction == Direction.Left || direction == Direction.Right)) ||
                    (currentDirection == Direction.Left && (direction == Direction.Right || direction == Direction.Up || direction == Direction.Down)) ||
                    (currentDirection == Direction.Right && (direction == Direction.Left || direction == Direction.Up || direction == Direction.Down))) {
                // Don't update lastMoveTime for rotations
                return true;
            }
        }

        // Only proceed with movement if we're facing the right direction
        if (currentDirection == direction) {
            // Calculate base movement delay

            // Handle empty space movement
            if (!nextField.isPresent()) {
                return true;
            }

            // Soldier re-entry
            if (nextField.getEntity().isPlayable() && (playableType == 3 || (playableType == 1 && game.getTanks().get(playableId).gethasSoldier()))) {
                if(game.getTanks().get((playableId)).getPosition() == nextField.getPosition()){
                    return true;
                }
            }

            // Handle item pickup
            if (nextField.getEntity().isItem()) {
                return true;
            }

            if (nextField.getEntity().isIndestructibleWall() || nextField.getEntity().isFactory()) {
                return false;
            } else if (nextField.getEntity().isWall() || nextField.getEntity().isPlayable() ||
                        nextField.getEntity().isMiningFacility() || nextField.getEntity().isRoad() ||
                        nextField.getEntity().isDeck() || nextField.getEntity().isBridge()) {
                    return true;
            }


        }
        return false;
    }

    private boolean handleTerrainConstraints (Playable playable, Terrain t, FieldHolder
    currentField, FieldHolder nextField){
        boolean hiddenMove = false;
        // Always emit terrain event
        TerrainUpdateEvent event = new TerrainUpdateEvent(
                t != null && t.isHilly(),
                t != null && t.isForest(),
                t != null && t.isRocky(),
                playableType,
                currentField.getPosition(),  // fromPosition
                nextField.getPosition()      // toPosition
        );
        EventBus.getDefault().post(event);

        // Check timing constraints for terrain types
        if (playableType == 1) { //tank
            if (t.isHilly() && (millis < playable.getLastMoveTime())) {
                return false;
            } else if (t.isForest() && (millis < playable.getLastMoveTime())) {
                System.out.println("Moving tank into forest");
                return false;
            }
        } else if (playableType == 2) { //builder
            if (t.isRocky() && (millis < playable.getLastMoveTime())) {
                return false;
            }
            if (t.isForest()) {
                return false;
            }
        } else if (playableType == 3) { //soldier
            if (t.isForest() && (millis < playable.getLastMoveTime())) {
                return false;
            }

            // Create remove event and change tankID to whomever I do NOT want to remove
            RemoveEvent remove = new RemoveEvent(playable.getIntValue(), playable.getPosition());
            remove.setTankID((int) playableId);
            EventBus.getDefault().post(remove);

            hiddenMove = true;
        }
        moveUnit(currentField, nextField, playable, direction, hiddenMove);
        playable.setLastMoveTime(millis + playable.getAllowedMoveInterval());

        // If we pass timing checks, move the unit
        moveUnit(currentField, nextField, playable, direction, false);

        // Set appropriate delay based on terrain type
        long delay = playable.getAllowedMoveInterval();
        if (playableType == 1) { // Tank
            if (t.isHilly()) {
                delay = (long) (delay * 1.5);
            } else if (t.isForest()) {
                delay = delay * 2;
            }
        } else if (playableType == 2) { // Builder
            if (t.isRocky()) {
                delay = (long) (delay * 1.5);
            }
        } else if (playableType == 3) { // Soldier
            if (t.isForest()) {
                delay = (long) (delay * 1.25);
            }
        }

        playable.setLastMoveTime(millis + delay);
        return true;
    }



    /**
     * Handle pickup of items
     * @param item Item being picked up
     * @param playable playable picking up the item
     */
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

    /**
     * Move unit from one field to another
     * @param currentField Current position
     * @param nextField Target position
     * @param playable playable to move
     * @param direction Direction of movement
     */
    private void moveUnit(FieldHolder currentField, FieldHolder nextField, Playable playable, Direction direction, boolean hiddenMove) {
        int oldPos = playable.getPosition();
        currentField.clearField();
        nextField.setFieldEntity(playable);
        playable.setParent(nextField);
        playable.setDirection(direction);

        if (hiddenMove) {
            MoveEvent move = new MoveEvent(playable.getIntValue(), oldPos, nextField.getPosition());
            move.setTankID((int) playableId);
            EventBus.getDefault().post(move);
        } else {
            EventBus.getDefault().post(new MoveEvent(playable.getIntValue(), oldPos, nextField.getPosition()));
        }
        EventBus.getDefault().post(new UIUpdateEvent(uIUpdates(Direction.Up), uIUpdates(Direction.Down),
                uIUpdates(Direction.Left), uIUpdates(Direction.Right)));
    }

    public void handleRemovingPlayable(FieldHolder currentField){
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

    @Override
    public Long executeJoin() {
        return null;
    }
}