package edu.unh.cs.cs619.bulletzone.model.events;

import com.fasterxml.jackson.annotation.JsonProperty;

public class MoveEvent extends GameEvent {
    @JsonProperty
    private int rawServerValue;

    @JsonProperty
    private int oldPosition; // Use oldPosition instead of oldPosition

    @JsonProperty
    private int newPosition;

    @JsonProperty
    private int tankID; // If this is set, it is the tank to show the move to, everyone else should see nothing

    public MoveEvent() {}

    public MoveEvent(int rawServerValue, int pos, int newPos) {
        this.rawServerValue = rawServerValue;
        this.oldPosition = pos;
        this.newPosition = newPos;
    }

    @Override
    public void applyTo(int[][] board) {
        board[oldPosition / 16][oldPosition % 16] = 0;
        board[newPosition / 16][newPosition % 16] = rawServerValue;
    }

    @Override
    public String toString() {
        return "Move " + rawServerValue +
                " from " + oldPosition +
                " to " + newPosition +
                super.toString();
    }

    public void setTankID(int tankID) {
        this.tankID = tankID;
    }
}