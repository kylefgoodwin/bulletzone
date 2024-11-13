package edu.unh.cs.cs619.bulletzone.events;

import com.fasterxml.jackson.annotation.JsonProperty;

public class MoveEvent extends GameEvent {
    @JsonProperty
    private int rawServerValue;

    @JsonProperty
    private int position; // Must match server field name

    @JsonProperty
    private int newPosition;

    public MoveEvent() {}

    @Override
    void applyTo(int[][] board) {
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