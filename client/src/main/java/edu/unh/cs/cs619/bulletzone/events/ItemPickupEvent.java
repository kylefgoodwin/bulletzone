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
        if (position >= 0 && position < board.length * board[0].length) {
            int row = position / board[0].length;
            int col = position % board[0].length;
            if (board[row][col] >= 3001 && board[row][col] <= 3005) {
                board[row][col] = 0;  // Clear the item
            }
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