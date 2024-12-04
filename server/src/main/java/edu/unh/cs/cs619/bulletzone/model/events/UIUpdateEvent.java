package edu.unh.cs.cs619.bulletzone.model.events;

import com.fasterxml.jackson.annotation.JsonProperty;

public class UIUpdateEvent extends GameEvent {

    @JsonProperty
    private boolean canMoveUp;

    @JsonProperty
    private boolean canMoveDown;

    @JsonProperty
    private boolean canMoveLeft;

    @JsonProperty
    private boolean canMoveRight;

    public UIUpdateEvent() {}

    public UIUpdateEvent(boolean canMoveUp, boolean canMoveDown, boolean canMoveLeft, boolean canMoveRight) {
        this.canMoveUp = canMoveUp;
        this.canMoveDown = canMoveDown;
        this.canMoveLeft = canMoveLeft;
        this.canMoveRight = canMoveRight;
    }

    @Override
    public void applyTo(int[][] board) {}

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
