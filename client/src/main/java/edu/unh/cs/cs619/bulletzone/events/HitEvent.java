package edu.unh.cs.cs619.bulletzone.events;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

public class HitEvent extends GameEvent implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty
    private int playableId;

    @JsonProperty
    private int playableType;

    @JsonProperty
    private int shieldHealth;

    @JsonProperty
    private int damage;

    public HitEvent() {} // Required empty constructor for deserialization

    public HitEvent(int playableId, int playableType, int shieldHealth, int damage) {
        this.playableId = playableId;
        this.playableType = playableType;
        this.shieldHealth = shieldHealth;
        this.damage = damage;
    }

    @Override
    public void applyTo(int[][] board) {
        // No board changes needed for hit events
    }

    @Override
    public String toString() {
        return "Hit Event - Player: " + playableId +
                " Type: " + playableType +
                " Damage: " + damage +
                " Shield: " + shieldHealth +
                " " + super.toString();
    }

    // Getters
    public int getPlayableId() { return playableId; }
    public int getPlayableType() { return playableType; }
    public int getShieldHealth() { return shieldHealth; }
    public int getDamage() { return damage; }
}