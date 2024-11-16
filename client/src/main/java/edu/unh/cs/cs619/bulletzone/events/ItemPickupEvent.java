package edu.unh.cs.cs619.bulletzone.events;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

public class ItemPickupEvent extends GameEvent implements Serializable {
    private static final long serialVersionUID = 1L;
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
        int currentValue = board[position / 16][position % 16];

        // If there's a goblin in this cell (check if value > 10000000)
        if (currentValue > 10000000) {
            // Preserve the goblin value
            board[position / 16][position % 16] = currentValue;
        } else {
            // Otherwise just clear the item
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