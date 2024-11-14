package edu.unh.cs.cs619.bulletzone.model.events;

import com.fasterxml.jackson.annotation.JsonProperty;

public class MoveEvent extends GameEvent {
    @JsonProperty
    private int rawServerValue;

    @JsonProperty
    private int position; // Use position instead of oldPosition

    @JsonProperty
    private int newPosition;

    public MoveEvent() {}

    public MoveEvent(int rawServerValue, int pos, int newPos) {
        this.rawServerValue = rawServerValue;
        this.position = pos;
        this.newPosition = newPos;
    }

    @Override
    public void applyTo(int[][] board) {
        board[position / 16][position % 16] = 0;
        board[newPosition / 16][newPosition % 16] = rawServerValue;
    }

    @Override
    public String toString() {
        return "Move " + rawServerValue +
                " from " + position +
                " to " + newPosition +
                super.toString();
    }
}