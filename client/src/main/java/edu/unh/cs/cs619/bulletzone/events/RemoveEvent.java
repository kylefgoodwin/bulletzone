package edu.unh.cs.cs619.bulletzone.events;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

import edu.unh.cs.cs619.bulletzone.PlayerData;

public class RemoveEvent extends GameEvent implements Serializable {
    @JsonProperty
    private int rawServerValue;
    @JsonProperty
    private int position;
    @JsonProperty
    private int tankID; // The ID of the user who SHOULD see the tank i.e. do not remove for this ID

    private static final long serialVersionUID = 1L;

    public RemoveEvent() {}

    @Override
    public void applyTo(int[][] board) {
        if (PlayerData.getPlayerData().getTankId() != tankID) {
            board[position / 16][position % 16] = 0;
        }
    }

    // Add these getters
    public int getTankID() {
        return tankID;
    }

    public int getRawServerValue() {
        return rawServerValue;
    }

    public int getPosition() {
        return position;
    }

    @Override
    public String toString() {
        return "Remove " + rawServerValue +
                " at " + position +
                super.toString();
    }
}