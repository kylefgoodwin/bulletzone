package edu.unh.cs.cs619.bulletzone.events;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

public class TurnEvent extends GameEvent implements Serializable {
    @JsonProperty
    private int rawServerValue;
    @JsonProperty
    private int position;
    @JsonProperty
    private int direction;

    private static final long serialVersionUID = 1L;

    public TurnEvent() {}

    void applyTo(int [][]board) {
        board[position / 16][position % 16] = rawServerValue;
    }

    @Override
    public String toString() {
        return "Turn " + rawServerValue +
                " to face " + direction +
                super.toString();
    }

}
