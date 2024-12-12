package edu.unh.cs.cs619.bulletzone.events;

import com.fasterxml.jackson.annotation.JsonProperty;

public class HealingEvent extends GameEvent {
    @JsonProperty
    private int rawServerValue;
    @JsonProperty
    private int position;
    @JsonProperty
    private long factoryHealing;

    public HealingEvent() {}

    public long getFactoryHealing() {
        return factoryHealing;
    }

    @Override
    public void applyTo(int[][] board) {
        board[position / 16][position % 16] = rawServerValue;
    }

    @Override
    public String toString() {
        return "Healing " + rawServerValue +
                " at " + position +
                super.toString();
    }
}