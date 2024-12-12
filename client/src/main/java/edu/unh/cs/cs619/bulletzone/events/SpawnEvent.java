package edu.unh.cs.cs619.bulletzone.events;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

public class SpawnEvent extends GameEvent implements Serializable {
    @JsonProperty
    private int rawServerValue;
    @JsonProperty
    private int position;

    private static final long serialVersionUID = 1L;

    public SpawnEvent() {}

    void applyTo(int [][]board) {
        board[position / 16][position % 16] = rawServerValue;
    }

    @Override
    public String toString() {
        return "Spawn " + rawServerValue +
                " at " + position +
                super.toString();
    }
}