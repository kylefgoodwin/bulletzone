package edu.unh.cs.cs619.bulletzone.repository;

import edu.unh.cs.cs619.bulletzone.model.Direction;
import edu.unh.cs.cs619.bulletzone.model.FieldHolder;
import edu.unh.cs.cs619.bulletzone.model.Game;
import edu.unh.cs.cs619.bulletzone.model.Soldier;
import edu.unh.cs.cs619.bulletzone.model.Tank;
import edu.unh.cs.cs619.bulletzone.model.TankDoesNotExistException;

public class EjectCommand implements Command {
    Game game;
    long tankId;
    Direction direction;
    long millis;
    private static final int FIELD_DIM = 16;

    /**
     * Constructor for EjectCommand called each time
     * eject() is called in InGameMemoryRepository
     *
     * @param tankId       tank to eject power-up
     */
    public EjectCommand(long tankId, Game game, Direction direction, long currentTimeMillis) {
        this.tankId = tankId;
        this.game = game;
        this.direction = direction;
        this.millis = currentTimeMillis;
    }

    /**
     * execute command for eject
     *
     * @return true if eject is successful
     * @throws TankDoesNotExistException if tank doesn't exist
     */
    @Override
    public boolean execute() throws TankDoesNotExistException {
        Tank tank = game.getTanks().get(tankId);
        if (tank == null) {
            throw new TankDoesNotExistException(tankId);
        }

        FieldHolder currentField = tank.getParent();
        Direction direction = Direction.Up;
        if (tank.getDirection() == Direction.Up) {
            if (!(currentField.getNeighbor(Direction.Down).isPresent())) {
                direction = Direction.Down;
            } else if (!(currentField.getNeighbor(Direction.Left).isPresent())) {
                direction = Direction.Left;
            } else if (!(currentField.getNeighbor(Direction.Right).isPresent())) {
                direction = Direction.Right;
            } else if ((currentField.getNeighbor(Direction.Up).isPresent())) {
                return false;
            }
        } else if (tank.getDirection() == Direction.Right) {
            if (!(currentField.getNeighbor(Direction.Left).isPresent())) {
                direction = Direction.Left;
            } else if (!(currentField.getNeighbor(Direction.Down).isPresent())) {
                direction = Direction.Down;
            } else if (!(currentField.getNeighbor(Direction.Right).isPresent())) {
                direction = Direction.Right;
            } else {
                return false;
            }
        } else if (tank.getDirection() == Direction.Left) {
            if (!(currentField.getNeighbor(Direction.Right).isPresent())) {
                direction = Direction.Right;
            } else if (!(currentField.getNeighbor(Direction.Down).isPresent())) {
                direction = Direction.Down;
            }else if (!(currentField.getNeighbor(Direction.Left).isPresent())) {
                direction = Direction.Left;
            } else {
                return false;
            }
        } else {//down
            if (!(currentField.getNeighbor(Direction.Up).isPresent())) {
                direction = Direction.Up;
            } else if (!(currentField.getNeighbor(Direction.Right).isPresent())) {
                direction = Direction.Right;
            } else if (!(currentField.getNeighbor(Direction.Left).isPresent())) {
                direction = Direction.Left;
            } else if ((currentField.getNeighbor(Direction.Down).isPresent())) {
                direction = Direction.Down;
            } else {
                return false;
            }
        }

        boolean isVisible = currentField.isPresent()
                && (currentField.getEntity() == tank);

        FieldHolder nextField = currentField.getNeighbor(direction);

        // Check if the destination field is empty
        if (!nextField.isPresent()) {
            // If the next field is empty then eject soldier
            int fieldIndex = currentField.getPosition();
            int row = fieldIndex / FIELD_DIM;
            int col = fieldIndex % FIELD_DIM;

            // Check if the tank is at the gameboard edges and trying to move out of bounds
            boolean isAtLeftEdge = (col == 0) && direction == Direction.Left;
            boolean isAtRightEdge = (col == FIELD_DIM - 1) && direction == Direction.Right;
            boolean isAtTopEdge = (row == 0) && direction == Direction.Up;
            boolean isAtBottomEdge = (row == FIELD_DIM - 1) && direction == Direction.Down;

            if (isAtLeftEdge || isAtRightEdge || isAtTopEdge || isAtBottomEdge) {
                System.out.println("Next field is out of bounds, eject blocked.");
                return false;
            }

            // Check if the tank is visible on the field (just to prevent weird cases)
            if (!isVisible) {
                System.out.println("You have already been eliminated.");
                return false;
            }
            // Create and eject the soldier
            Soldier soldier = new Soldier(tankId, direction, tank.getIp());

            // Place the soldier on the grid
            int oldPos = tank.getPosition();
            nextField.setFieldEntity(soldier);
            soldier.setParent(nextField);
            int newPos = soldier.getPosition();

            // ////////////Insert Event logic///////////////

            return true;
        }
        return false;
    }

    @Override
    public Long executeJoin() {
        return null;
    }
}
