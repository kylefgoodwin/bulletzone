package edu.unh.cs.cs619.bulletzone.repository;

import java.util.Objects;

import edu.unh.cs.cs619.bulletzone.model.Builder;
import edu.unh.cs.cs619.bulletzone.model.Direction;
import edu.unh.cs.cs619.bulletzone.model.FieldEntity;
import edu.unh.cs.cs619.bulletzone.model.FieldHolder;
import edu.unh.cs.cs619.bulletzone.model.Game;
import edu.unh.cs.cs619.bulletzone.model.MiningFacility;
import edu.unh.cs.cs619.bulletzone.model.TankDoesNotExistException;
import edu.unh.cs.cs619.bulletzone.model.Wall;

public class BuildCommand implements Command {
    Game game;
    long builderId;
    String entity;
    private static final int FIELD_DIM = 16;

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
                // //////////INSERT EVENT LOGIC HERE ///////////////
                game.getHolderGrid().get(nextIndex).setFieldEntity(new Wall(1500, nextIndex));
                return true;
            } else if (Objects.equals(entity, "indestructibleWall")) {
                // //////////INSERT EVENT LOGIC HERE ///////////////
                game.getHolderGrid().get(nextIndex).setFieldEntity(new Wall());
                return true;
            } else if (Objects.equals(entity, "miningFacility")) {
                // //////////INSERT EVENT LOGIC HERE ///////////////
                game.getHolderGrid().get(nextIndex).setFieldEntity(new MiningFacility(920, nextIndex));
                return true;
            }
        } else {
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
            FieldEntity entityInNextField = nextField.getEntity();

            if (entityInNextField instanceof Wall || entityInNextField instanceof MiningFacility) {
                if (entityInNextField instanceof Wall) {
                    System.out.println("Dismantling wall...");
                    nextField.setFieldEntity(null);
                    return true;
                }
                // If it's an indestructible wall or mining facility, dismantle it if the rules allow
                System.out.println("Dismantling mining facility...");
                nextField.setFieldEntity(null);
                return true;
            }
        }

        return true;
    }

    /**
     * stub for join command
     * @return null value now
     */
    @Override
    public Long executeJoin() {
        return null;
    }

}
