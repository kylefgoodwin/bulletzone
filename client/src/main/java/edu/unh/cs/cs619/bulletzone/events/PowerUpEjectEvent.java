package edu.unh.cs.cs619.bulletzone.events;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import java.io.Serializable;

@JsonTypeName("powerUpEject")
public class PowerUpEjectEvent extends GameEvent implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty
    private int powerUpType;

    public PowerUpEjectEvent() {
        // Default constructor for Jackson
    }

    public PowerUpEjectEvent(int powerUpType) {
        this.powerUpType = powerUpType;
    }

    public int getPowerUpType() {
        return powerUpType;
    }

    public void setPowerUpType(int powerUpType) {
        this.powerUpType = powerUpType;
    }

    @Override
    public void applyTo(int[][] board) {
        // No board changes needed for power-up eject events
    }
}