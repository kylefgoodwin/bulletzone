package edu.unh.cs.cs619.bulletzone.events;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

public class PowerUpEjectEvent extends GameEvent implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty
    private int powerUpType;

    public PowerUpEjectEvent(int powerUpType) {
        this.powerUpType = powerUpType;
    }

    public int getPowerUpType() {
        return powerUpType;
    }

    @Override
    void applyTo(int[][] board) {
        // No board changes needed for power-up eject events
    }
}