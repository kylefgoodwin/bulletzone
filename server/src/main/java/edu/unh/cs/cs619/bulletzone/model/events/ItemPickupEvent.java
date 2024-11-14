package edu.unh.cs.cs619.bulletzone.model.events;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ItemPickupEvent extends GameEvent {
    @JsonProperty
    private int itemType;
    @JsonProperty
    private double amount;
    @JsonProperty
    private int position;

    public ItemPickupEvent() {} // Required for Jackson

    public ItemPickupEvent(int itemType, double amount, int position) {
        this.itemType = itemType;
        this.amount = amount;
        this.position = position;
    }

    @Override
    public void applyTo(int[][] board) {
        board[position / 16][position % 16] = 0;
    }

    public int getItemType() {
        return itemType;
    }

    public double getAmount() {
        return amount;
    }

    public int getPosition() {
        return position;
    }

    @Override
    public String toString() {
        return "ItemPickup " + itemType +
                " amount=" + amount +
                " at " + position +
                super.toString();
    }
}