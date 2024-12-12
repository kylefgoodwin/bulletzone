package edu.unh.cs.cs619.bulletzone.util;

import edu.unh.cs.cs619.bulletzone.PlayerData;
import edu.unh.cs.cs619.bulletzone.TankEventController;

/**
 * Made by Alec Rydeen, leaf command class
 */
public class FireCommand extends InputCommand{

    long playableId;
    int playableType;

    TankEventController tankEventController;

    public FireCommand(long playableId, int playableType, TankEventController tankEventController) {
        this.playableId = playableId;
        this.playableType = playableType;
        this.tankEventController = tankEventController;
    }

    @Override
    public void operation() {
        tankEventController.fire(playableId, playableType);
    }
}
