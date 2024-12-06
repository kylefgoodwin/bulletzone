package edu.unh.cs.cs619.bulletzone.model.events;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeName("powerUpEject")
public class PowerUpEjectEvent extends GameEvent {
    @JsonProperty
    private final int powerUpType;

    public PowerUpEjectEvent(int powerUpType) {
        this.powerUpType = powerUpType;
    }

    public int getPowerUpType() {
        return powerUpType;
    }

    @Override
    public void applyTo(int[][] board) {
        // No board changes needed for power-up eject events
    }

    @Override
    public String toString() {
        return "PowerUpEject " + powerUpType + super.toString();
    }
}