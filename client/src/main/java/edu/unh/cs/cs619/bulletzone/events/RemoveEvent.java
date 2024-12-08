package edu.unh.cs.cs619.bulletzone.events;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

import edu.unh.cs.cs619.bulletzone.PlayerData;

public class RemoveEvent extends GameEvent  implements Serializable {
    @JsonProperty
    private int rawServerValue;
    @JsonProperty
    private int position;
    @JsonProperty
    private int tankID; // The ID of the user who SHOULD see the tank i.e. do not remove for this ID
    @JsonProperty
    private long soldierRemove;

    private static final long serialVersionUID = 1L;

    public RemoveEvent() {}

    public long getSoldierRemove() {
        return soldierRemove;
    }
    public int getTankID() {
        return tankID;
    }

    @Override
    public void applyTo(int[][] board) {
        // For soldier hides in forrest mechanic:
        // if playerData.getTankID == tankID (id of soldier that is hiding)
        //      then do nothing, don't change the value @ position
        // else
        //      do change the value @ position
        if (PlayerData.getPlayerData().getTankId() != soldierRemove) {
            board[position / 16][position % 16] = 0;
        }
        board[position / 16][position % 16] = 0;
    }

    @Override
    public String toString() {
        return "Remove " + rawServerValue +
                " at " + position +
                super.toString();
    }

}
