package edu.unh.cs.cs619.bulletzone.util;

import java.util.ArrayList;

import edu.unh.cs.cs619.bulletzone.PlayerData;

/**
 * Made by Alec Rydeen, Composite command class
 */
public class CompositeCommand extends InputCommand {

    private ArrayList<InputCommand> children = new ArrayList<>();

    PlayerData playerData = PlayerData.getPlayerData();

    public CompositeCommand(ArrayList<InputCommand> children) {
        this.children.addAll(children);
    }

    public CompositeCommand clone() {
        return new CompositeCommand(children);
    }

    @Override
    public void operation() {
        new Thread(() -> {
            try {
                for (InputCommand child : children) {
                    child.operation();
                    Thread.sleep(playerData.getMoveInterval());
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        for (InputCommand child : children) {
            if (child instanceof MoveCommand) {
                if (((MoveCommand) child).direction == 0) {
                    stringBuilder.append("U");
                } else if (((MoveCommand) child).direction == 2) {
                    stringBuilder.append("R");
                } else if (((MoveCommand) child).direction == 4) {
                    stringBuilder.append("D");
                } else if (((MoveCommand) child).direction == 6) {
                    stringBuilder.append("L");
                }
            } else if (child instanceof FireCommand) {
                stringBuilder.append("F");
            } else if (child instanceof CompositeCommand) {
                stringBuilder.append(child.toString());
            }
        }
        return stringBuilder.toString();
    }
}
