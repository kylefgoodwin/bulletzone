package edu.unh.cs.cs619.bulletzone.events;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

public class UIUpdateEvent extends GameEvent implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty
    private boolean canMoveUp;

    @JsonProperty
    private boolean canMoveDown;

    @JsonProperty
    private boolean canMoveLeft;

    @JsonProperty
    private boolean canMoveRight;

    public UIUpdateEvent() {}

    @Override
    void applyTo(int [][]board) {}

    public boolean getCanMoveDown() {
        return canMoveDown;
    }

    public boolean getCanMoveUp() {
        return canMoveUp;
    }

    public boolean getCanMoveLeft() {
        return canMoveLeft;
    }

    public boolean getCanMoveRight() {
        return canMoveRight;
    }

    @Override
    public String toString() {
        return "Move Forward: " + canMoveUp + "Move Backward: " + canMoveDown +
                "Move Left: " + canMoveLeft + "Move Right: " + canMoveRight + super.toString();
    }
}
