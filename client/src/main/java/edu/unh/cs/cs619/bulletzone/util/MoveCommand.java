package edu.unh.cs.cs619.bulletzone.util;

import edu.unh.cs.cs619.bulletzone.PlayerData;
import edu.unh.cs.cs619.bulletzone.TankEventController;

/**
 * Made by Alec Rydeen, Leaf command class
 */
public class MoveCommand extends InputCommand {

    byte direction;
    int currentButtonId;
    int playableType;
    long playableId;

    TankEventController tankEventController;

    public MoveCommand(byte direction, int currentButtonId, int playableType, long playableId,
                       TankEventController tankEventController) {
        this.direction = direction;
        this.currentButtonId = currentButtonId;
        this.playableType = playableType;
        this.playableId = playableId;
        this.tankEventController = tankEventController;
    }

    @Override
    public void operation() {
        tankEventController.turnOrMove(currentButtonId, playableId, playableType, direction);
    }
}
