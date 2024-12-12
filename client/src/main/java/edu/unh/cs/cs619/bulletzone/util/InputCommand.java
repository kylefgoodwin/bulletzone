package edu.unh.cs.cs619.bulletzone.util;

import java.util.ArrayList;

/**
 * Made by Alec Rydeen Abstract component class
 */
public abstract class InputCommand {

    private  ArrayList<InputCommand> children;

    protected InputCommand() {}

    public void operation() {}
}
