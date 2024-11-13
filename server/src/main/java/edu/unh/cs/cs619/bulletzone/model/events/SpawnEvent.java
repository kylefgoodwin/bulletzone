package edu.unh.cs.cs619.bulletzone.model.events;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SpawnEvent extends GameEvent {
    @JsonProperty
    private int rawServerValue;
    @JsonProperty
    private int position;

    public SpawnEvent() {}

    public SpawnEvent(int rawServerValue, int pos) {
        this.rawServerValue = rawServerValue;
        this.position = pos;
    }

    @Override
    public void applyTo(int[][] board) {
        board[position / 16][position % 16] = rawServerValue;
    }

    @Override
    public String toString() {
        return "Spawn " + rawServerValue +
                " at " + position +
                super.toString();
    }

}