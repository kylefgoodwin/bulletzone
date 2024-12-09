package edu.unh.cs.cs619.bulletzone.model.events;

import com.fasterxml.jackson.annotation.JsonProperty;

public class RemoveEvent extends GameEvent {
    @JsonProperty
    private int rawServerValue;
    @JsonProperty
    private int position;

    @JsonProperty
    private int tankID;

    @JsonProperty
    private long soldierRemove = 0;

    public RemoveEvent() {}

    public RemoveEvent(int rawServerValue, int pos, long soldierRemove) {
        this.rawServerValue = rawServerValue;
        this.position = pos;
        this.soldierRemove = soldierRemove;
    }

    @Override
    public void applyTo(int[][] board) {
        board[position / 16][position % 16] = 0;
    }

    @Override
    public String toString() {
        return "Remove " + rawServerValue +
                " at " + position +
                super.toString();
    }

    public void setTankID(int tankID) {
        this.tankID = tankID;
    }

    public long getSoldierRemove() {
        return soldierRemove;
    }
    public int getTankID() {
        return tankID;
    }

}