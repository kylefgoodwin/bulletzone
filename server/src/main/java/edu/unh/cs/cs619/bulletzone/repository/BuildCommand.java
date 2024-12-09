package edu.unh.cs.cs619.bulletzone.repository;

import org.greenrobot.eventbus.EventBus;

import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import edu.unh.cs.cs619.bulletzone.datalayer.account.BankAccount;
import edu.unh.cs.cs619.bulletzone.model.Bridge;
import edu.unh.cs.cs619.bulletzone.model.Builder;
import edu.unh.cs.cs619.bulletzone.model.Deck;
import edu.unh.cs.cs619.bulletzone.model.Direction;
import edu.unh.cs.cs619.bulletzone.model.Factory;
import edu.unh.cs.cs619.bulletzone.model.FieldEntity;
import edu.unh.cs.cs619.bulletzone.model.FieldHolder;
import edu.unh.cs.cs619.bulletzone.model.Game;
import edu.unh.cs.cs619.bulletzone.model.MiningFacility;
import edu.unh.cs.cs619.bulletzone.model.Road;
import edu.unh.cs.cs619.bulletzone.model.TankDoesNotExistException;
import edu.unh.cs.cs619.bulletzone.model.Wall;
import edu.unh.cs.cs619.bulletzone.model.events.RemoveEvent;
import edu.unh.cs.cs619.bulletzone.model.events.SpawnEvent;

public class BuildCommand implements Command {
    Game game;
    long builderId;
    String entity;
    private int miningFacilityCount = 0;
    private static final int FIELD_DIM = 16;
    private ScheduledFuture<?> creditTask;
//    private final ConcurrentHashMap<Long, Integer> facilityOwnerMap = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);


    /**
     * Constructor for buildCommand called each time
     * build() is called in InGameMemoryRepository
     *
     * @param builderId which builder to build
     */
    public BuildCommand(long builderId, Game game, String entity) {
        this.game = game;
        this.entity = entity;
        this.builderId = builderId;
    }

    /**
     * @return true if block is built
     * @throws TankDoesNotExistException if builder doesn't exist
     */
    @Override
    public boolean execute() throws TankDoesNotExistException {
        Builder builder = game.getBuilders().get(builderId);
        if (builder == null) {
            throw new TankDoesNotExistException(builderId);
        }
        FieldHolder currentField = builder.getParent();
        Direction direction = Direction.Up;
        if (builder.getDirection() == Direction.Up) {
            direction = Direction.Down;
        } else if (builder.getDirection() == Direction.Right) {
            direction = Direction.Left;
        } else if (builder.getDirection() == Direction.Left) {
            direction = Direction.Right;
        }
        boolean isVisible = currentField.isPresent()
                && (currentField.getEntity() == builder);
        FieldHolder nextField = currentField.getNeighbor(direction);
        int built = builder.getAllowBuildInterval();
        BankAccount balance = game.getBankAccount(builderId);
        if (!nextField.isPresent()) { //nothing there, can build
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
                System.out.println("Next field is out of bounds, building blocked.");
                return false;
            }

            // Check if the tank is visible on the field (just to prevent weird cases)
            if (!isVisible) {
                System.out.println("You have already been eliminated.");
                return false;
            }
            int nextIndex = nextField.getPosition();
            int currentIndex = currentField.getPosition();
            int currentValue = currentField.getEntity().getIntValue();
            if (Objects.equals(entity, "destructibleWall")) {
                if (balance.getBalance() >= 80.0) {
                    long millis = System.currentTimeMillis();
                    builder.setLastBuildTime(System.currentTimeMillis());
                    builder.startBuilding();
                    while (builder.getLastBuildTime() - millis < built) {
                        System.out.println("Building...");
                        builder.setLastBuildTime(System.currentTimeMillis());
                    }
                    Wall destructibleWall = new Wall(1500, nextIndex);
                    game.getHolderGrid().get(nextIndex).setFieldEntity(destructibleWall);
                    double credits = -80.0;
//                    balance.modifyBalance(credits);
                    game.modifyBalance(builderId, credits);
                    builder.stopBuilding();
                    EventBus.getDefault().post(new SpawnEvent(destructibleWall.getIntValue(), nextIndex));
                    return true;
                } else {
                    System.out.println("You don't have enough credits: " + balance.getBalance() + ", building blocked.");
                    return false;
                }
            } else if (Objects.equals(entity, "indestructibleWall")) {

                if (balance.getBalance() >= 150.0) {
                    long millis = System.currentTimeMillis();
                    builder.setLastBuildTime(System.currentTimeMillis());
                    builder.startBuilding();
                    while (builder.getLastBuildTime() - millis < built) {
                        System.out.println("Building...");
                        builder.setLastBuildTime(System.currentTimeMillis());
                    }
                    Wall indestructibleWall = new Wall();
                    game.getHolderGrid().get(nextIndex).setFieldEntity(indestructibleWall);
                    double credits = -150.0;
//                    balance.modifyBalance(credits);
                    game.modifyBalance(builderId, credits);
                    builder.stopBuilding();
                    EventBus.getDefault().post(new SpawnEvent(indestructibleWall.getIntValue(), nextIndex));
                    return true;
                } else {
                    System.out.println("You don't have enough credits: " + balance.getBalance() + ", building blocked.");
                    return false;
                }
            } else if (Objects.equals(entity, "miningFacility")) {

                if (balance.getBalance() >= 300.0) {
                    long millis = System.currentTimeMillis();
                    builder.setLastBuildTime(System.currentTimeMillis());
                    builder.startBuilding();
                    while (builder.getLastBuildTime() - millis < built) {
                        System.out.println("Building...");
                        builder.setLastBuildTime(System.currentTimeMillis());
                    }
                    MiningFacility miningFacility = new MiningFacility(920, nextIndex);
                    game.getHolderGrid().get(nextIndex).setFieldEntity(miningFacility);
                    double credits = -300.0;
//                    balance.modifyBalance(credits);
                    game.modifyBalance(builderId, credits);
                    // Track the facility's owner
                    miningFacilityCount+=1;

                    // Start adding credits for this MiningFacility
                    startCreditTask(game, builderId);
                    builder.stopBuilding();
                    EventBus.getDefault().post(new SpawnEvent(miningFacility.getIntValue(), nextIndex));
                    return true;
                } else {
                    System.out.println("You don't have enough credits: " + balance.getBalance() + ", building blocked.");
                    return false;
                }

            } else if (Objects.equals(entity, "road")) {

                if (balance.getBalance() >= 40.0) {
                    long millis = System.currentTimeMillis();
                    builder.setLastBuildTime(System.currentTimeMillis());
                    builder.startBuilding();
                    while (builder.getLastBuildTime() - millis < built) {
                        System.out.println("Building...");
                        builder.setLastBuildTime(System.currentTimeMillis());
                    }
                    Road road = new Road();
                    game.getHolderGrid().get(nextIndex).setFieldEntity(road);
                    double credits = -40.0;
//                    balance.modifyBalance(credits);
                    game.modifyBalance(builderId, credits);
                    builder.stopBuilding();
                    EventBus.getDefault().post(new SpawnEvent(road.getIntValue(), nextIndex));
                    return true;
                } else {
                    System.out.println("You don't have enough credits: " + balance.getBalance() + ", building blocked.");
                    return false;
                }
            } else if (Objects.equals(entity, "deck")) {

                if (balance.getBalance() >= 80.0) {
                    long millis = System.currentTimeMillis();
                    builder.setLastBuildTime(System.currentTimeMillis());
                    builder.startBuilding();
                    while (builder.getLastBuildTime() - millis < built) {
                        System.out.println("Building...");
                        builder.setLastBuildTime(System.currentTimeMillis());
                    }
                    Deck deck = new Deck();
                    game.getHolderGrid().get(nextIndex).setFieldEntity(deck);
                    double credits = -80.0;
//                    balance.modifyBalance(credits);
                    game.modifyBalance(builderId, credits);
                    builder.stopBuilding();
                    EventBus.getDefault().post(new SpawnEvent(deck.getIntValue(), nextIndex));
                    return true;
                } else {
                    System.out.println("You don't have enough credits: " + balance.getBalance() + ", building blocked.");
                    return false;
                }
            } else if (Objects.equals(entity, "bridge")) {

                if (balance.getBalance() >= 120.0) {
                    long millis = System.currentTimeMillis();
                    builder.setLastBuildTime(System.currentTimeMillis());
                    builder.startBuilding();
                    while (builder.getLastBuildTime() - millis < built) {
                        System.out.println("Building...");
                        builder.setLastBuildTime(System.currentTimeMillis());
                    }
                    Bridge bridge = new Bridge();
                    game.getHolderGrid().get(nextIndex).setFieldEntity(bridge);
                    double credits = -120.0;
//                    balance.modifyBalance(credits);
                    game.modifyBalance(builderId, credits);
                    builder.stopBuilding();
                    EventBus.getDefault().post(new SpawnEvent(bridge.getIntValue(), nextIndex));
                    return true;
                } else {
                    System.out.println("You don't have enough credits: " + balance.getBalance() + ", building blocked.");
                    return false;
                }
            } else if (Objects.equals(entity, "factory")) {

                if (balance.getBalance() >= 250.0) {
                    long millis = System.currentTimeMillis();
                    builder.setLastBuildTime(System.currentTimeMillis());
                    builder.startBuilding();
                    while (builder.getLastBuildTime() - millis < built) {
                        System.out.println("Building...");
                        builder.setLastBuildTime(System.currentTimeMillis());
                    }
                    Factory factory = new Factory();
                    game.getHolderGrid().get(nextIndex).setFieldEntity(factory);
                    double credits = -250.0;
//                    balance.modifyBalance(credits);
                    game.modifyBalance(builderId, credits);
                    builder.stopBuilding();
                    EventBus.getDefault().post(new SpawnEvent(factory.getIntValue(), nextIndex));
                    return true;
                } else {
                    System.out.println("You don't have enough credits: " + balance.getBalance() + ", building blocked.");
                    return false;
                }
            }
        } else {
            int fieldIndex = currentField.getPosition();
            int nextIndex = nextField.getPosition();

            int row = fieldIndex / FIELD_DIM;
            int col = fieldIndex % FIELD_DIM;

            // Check if the tank is at the gameboard edges and trying to move out of bounds
            boolean isAtLeftEdge = (col == 0) && direction == Direction.Left;
            boolean isAtRightEdge = (col == FIELD_DIM - 1) && direction == Direction.Right;
            boolean isAtTopEdge = (row == 0) && direction == Direction.Up;
            boolean isAtBottomEdge = (row == FIELD_DIM - 1) && direction == Direction.Down;

            if (isAtLeftEdge || isAtRightEdge || isAtTopEdge || isAtBottomEdge) {
                System.out.println("Next field is out of bounds, building blocked.");
                return false;
            }

            // Check if the tank is visible on the field (just to prevent weird cases)
            if (!isVisible) {
                System.out.println("You have already been eliminated.");
                return false;
            }
            FieldEntity entityInNextField = nextField.getEntity();

            if (entityInNextField.isImprovement()) {
                if (entityInNextField.isWall()) {
                    long millis = System.currentTimeMillis();
                    builder.setLastBuildTime(System.currentTimeMillis());
                    builder.startDismantling();
                    while (builder.getLastBuildTime() - millis < built) {
                        if (!builder.isDismantling()) {
                            return false;
                        }
                        System.out.println("Dismantling wall...");
                        builder.setLastBuildTime(System.currentTimeMillis());
                    }
                    nextField.clearField();
                    double credits = 80.0;
                    balance.modifyBalance(credits);
                    game.modifyBalance(builderId, credits);
                    builder.stopDismantling();
                    EventBus.getDefault().post(new RemoveEvent(entityInNextField.getIntValue(), nextIndex, 0));
                    return true;
                }
                // If it's an indestructible wall or mining facility, dismantle it if the rules allow
                if (entityInNextField.isMiningFacility()) {
                    long millis = System.currentTimeMillis();
                    builder.setLastBuildTime(System.currentTimeMillis());
                    builder.startDismantling();
                    while (builder.getLastBuildTime() - millis < built) {
                        if (!builder.isDismantling()) {
                            return false;
                        }
                        System.out.println("Dismantling mining facility...");
                        builder.setLastBuildTime(System.currentTimeMillis());
                    }
                    nextField.clearField();
                    double credits = 300.0;
                    balance.modifyBalance(credits);
                    game.modifyBalance(builderId, credits);
                    stopCreditTask();
                    builder.stopDismantling();
                    EventBus.getDefault().post(new RemoveEvent(entityInNextField.getIntValue(), nextIndex, 0));
                } else if (entityInNextField.isIndestructibleWall()){
                    builder.startDismantling();
                    long millis = System.currentTimeMillis();
                    builder.setLastBuildTime(System.currentTimeMillis());
                    while (builder.getLastBuildTime() - millis < built) {
                        if (!builder.isDismantling()) {
                            return false;
                        }
                        System.out.println("Dismantling indestructible wall...");
                        builder.setLastBuildTime(System.currentTimeMillis());
                    }
                    nextField.clearField();
                    double credits = 150.0;
                    balance.modifyBalance(credits);
                    game.modifyBalance(builderId, credits);
                    builder.stopDismantling();
                    EventBus.getDefault().post(new RemoveEvent(entityInNextField.getIntValue(), nextIndex, 0));
                } else if (entityInNextField.isRoad()){
                    builder.startDismantling();
                    long millis = System.currentTimeMillis();
                    builder.setLastBuildTime(System.currentTimeMillis());
                    while (builder.getLastBuildTime() - millis < built) {
                        if (!builder.isDismantling()) {
                            return false;
                        }
                        System.out.println("Dismantling road...");
                        builder.setLastBuildTime(System.currentTimeMillis());
                    }
                    nextField.clearField();
                    double credits = 40.0;
                    balance.modifyBalance(credits);
                    game.modifyBalance(builderId, credits);
                    builder.stopDismantling();
                    EventBus.getDefault().post(new RemoveEvent(entityInNextField.getIntValue(), nextIndex, 0));
                } else if (entityInNextField.isDeck()){
                    builder.startDismantling();
                    long millis = System.currentTimeMillis();
                    builder.setLastBuildTime(System.currentTimeMillis());
                    while (builder.getLastBuildTime() - millis < built) {
                        if (!builder.isDismantling()) {
                            return false;
                        }
                        System.out.println("Dismantling deck...");
                        builder.setLastBuildTime(System.currentTimeMillis());
                    }
                    nextField.clearField();
                    double credits = 80.0;
                    balance.modifyBalance(credits);
                    game.modifyBalance(builderId, credits);
                    builder.stopDismantling();
                    EventBus.getDefault().post(new RemoveEvent(entityInNextField.getIntValue(), nextIndex, 0));
                } else if (entityInNextField.isBridge()){
                    builder.startDismantling();
                    long millis = System.currentTimeMillis();
                    builder.setLastBuildTime(System.currentTimeMillis());
                    while (builder.getLastBuildTime() - millis < built) {
                        if (!builder.isDismantling()) {
                            return false;
                        }
                        System.out.println("Dismantling bridge...");
                        builder.setLastBuildTime(System.currentTimeMillis());
                    }
                    nextField.clearField();
                    double credits = 120.0;
                    balance.modifyBalance(credits);
                    game.modifyBalance(builderId, credits);
                    builder.stopDismantling();
                    EventBus.getDefault().post(new RemoveEvent(entityInNextField.getIntValue(), nextIndex, 0));
                } else if (entityInNextField.isFactory()){
                    builder.startDismantling();
                    long millis = System.currentTimeMillis();
                    builder.setLastBuildTime(System.currentTimeMillis());
                    while (builder.getLastBuildTime() - millis < built) {
                        if (!builder.isDismantling()) {
                            return false;
                        }
                        System.out.println("Dismantling factory...");
                        builder.setLastBuildTime(System.currentTimeMillis());
                    }
                    nextField.clearField();
                    double credits = 250.0;
                    balance.modifyBalance(credits);
                    game.modifyBalance(builderId, credits);
                    builder.stopDismantling();
                    EventBus.getDefault().post(new RemoveEvent(entityInNextField.getIntValue(), nextIndex, 0));
                }

                return true;
            }
        }

        return true;
    }

    private void startCreditTask(Game game, long facilityId) {
        creditTask = scheduler.scheduleAtFixedRate(() -> {
            if (miningFacilityCount != 0) {
                // Add one credit per second to the owner's balance
                game.modifyBalance(facilityId, miningFacilityCount);
            } else {
                // If conditions are not met, cancel the task
                stopCreditTask();
            }
        }, 1, 1, TimeUnit.SECONDS); // Run every second after an initial 1-second delay

    }

    public void stopCreditTask() {
        // Stop task logic (optional if you track individual tasks)
        if (creditTask != null && !creditTask.isCancelled() && miningFacilityCount == 0) {
            creditTask.cancel(true); // Stop the task
            creditTask = null; // Reset the task reference
        }
        miningFacilityCount--;
    }

    /**
     * Unused, needed to override for Join command
     *
     * @return stub null value
     */
    @Override
    public Long executeJoin() {
        return null;
    }

}
