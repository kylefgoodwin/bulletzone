package edu.unh.cs.cs619.bulletzone.model.events;

import com.fasterxml.jackson.annotation.JsonProperty;

public class RemoveEvent extends GameEvent {
    @JsonProperty
    private int rawServerValue;
    @JsonProperty
    private int position;

    public RemoveEvent() {}

    public RemoveEvent(int rawServerValue, int pos) {
        this.rawServerValue = rawServerValue;
        this.position = pos;
    }

    @Override
    public void applyTo(int[][] board) {
        board[position / 16][position % 16] = 0;
    }

    @Override
    public String toString() {
        return "Remove " + rawServerValue +
                " at " + position +
                super.toString();
    }

}