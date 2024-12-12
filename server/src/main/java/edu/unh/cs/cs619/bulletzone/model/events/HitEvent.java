package edu.unh.cs.cs619.bulletzone.model.events;

import com.fasterxml.jackson.annotation.JsonProperty;

public class HitEvent extends GameEvent {
    @JsonProperty
    private int playableId;

    @JsonProperty
    private int playableType;

    @JsonProperty
    private int shieldHealth;

    @JsonProperty
    private int damage;

    public HitEvent() {} // Required empty constructor for Jackson

    public HitEvent(int playableId, int playableType, int shieldHealth, int damage) {
        this.playableId = playableId;
        this.playableType = playableType;
        this.shieldHealth = shieldHealth;
        this.damage = damage;
    }

    @Override
    public void applyTo(int[][] board) {
        // No board update needed for hit events
    }

    @Override
    public String toString() {
        return "Player's : " + playableId + playableType + " Hit with damage " + damage +
                " (Shield: " + shieldHealth + ") " + super.toString();
    }

    // Getters
    public int getPlayableId() { return playableId; }
    public int getPlayableType() { return playableType; }
    public int getShieldHealth() { return shieldHealth; }
    public int getDamage() { return damage; }
}