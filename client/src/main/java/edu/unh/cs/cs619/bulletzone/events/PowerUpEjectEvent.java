package edu.unh.cs.cs619.bulletzone.events;

public class PowerUpEjectEvent {
    private final int powerUpType;

    public PowerUpEjectEvent(int powerUpType) {
        this.powerUpType = powerUpType;
    }

    public int getPowerUpType() {
        return powerUpType;
    }
}