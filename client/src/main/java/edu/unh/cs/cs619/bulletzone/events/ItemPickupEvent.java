package edu.unh.cs.cs619.bulletzone.events;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ItemPickupEvent extends GameEvent {
    @JsonProperty
    private int itemType;
    @JsonProperty
    private double amount;
    @JsonProperty
    private int position;

    public ItemPickupEvent(int itemType, double amount) {
        super();
        this.itemType = itemType;
        this.amount = amount;
    }

    @Override
    void applyTo(int[][] board) {
        if (position >= 0) {
            board[position / 16][position % 16] = 0;
        }
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
}