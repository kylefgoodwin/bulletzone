package edu.unh.cs.cs619.bulletzone.events;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

public class MoveEvent extends GameEvent implements Serializable {
    @JsonProperty
    private int rawServerValue;

    @JsonProperty
    private int oldPosition; // Must match server field name

    @JsonProperty
    private int newPosition;

    private static final long serialVersionUID = 1L;

    public MoveEvent() {}

    @Override
    void applyTo(int[][] board) {
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
}