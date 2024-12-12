package edu.unh.cs.cs619.bulletzone.events;

import android.util.Log;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

import edu.unh.cs.cs619.bulletzone.PlayerData;

public class MoveEvent extends GameEvent implements Serializable {
    @JsonProperty
    private int rawServerValue;

    @JsonProperty
    private int oldPosition; // Must match server field name

    @JsonProperty
    private int newPosition;

    @JsonProperty
    private int tankID;

    private static final long serialVersionUID = 1L;

    public MoveEvent() {}

    public MoveEvent(int rawServerValue, int pos, int newPos) {
        super();
        this.rawServerValue = rawServerValue;
        this.oldPosition = pos;
        this.newPosition = newPos;
        this.tankID = -1;
    }

    @Override
    void applyTo(int[][] board) {
//        Log.d("Move Event", "MOVING");
        board[oldPosition / 16][oldPosition % 16] = 0;
        board[newPosition / 16][newPosition % 16] = rawServerValue;
    }

    @Override
    public String toString() {
        return "Move " + rawServerValue +
                " from " + oldPosition +
                " to " + newPosition +
                super.toString();
    }

    public void setTankID(int tankID) {
        this.tankID = tankID;
    }

    public int getTankID() {
        return this.tankID;
    }
}