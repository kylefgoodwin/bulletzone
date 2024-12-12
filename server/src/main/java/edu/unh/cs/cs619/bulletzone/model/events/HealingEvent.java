package edu.unh.cs.cs619.bulletzone.model.events;

import com.fasterxml.jackson.annotation.JsonProperty;

public class HealingEvent extends GameEvent {
    @JsonProperty
    private int rawServerValue;
    @JsonProperty
    private int position;
    @JsonProperty
    private long factoryHealing;

    public HealingEvent() {}

    public HealingEvent(int rawServerValue, int pos, long factoryHealing) {
        this.position = pos;
        this.rawServerValue = rawServerValue;
        this.factoryHealing = factoryHealing;
    }

    @Override
    public void applyTo(int[][] board) {
        board[position / 16][position % 16] = rawServerValue;
    }

    public long getFactoryHealing() {
        return factoryHealing;
    }

    @Override
    public String toString() {
        return "Healing " + rawServerValue +
                " at " + position +
                super.toString();
    }
}