package edu.unh.cs.cs619.bulletzone.events;

import com.fasterxml.jackson.annotation.JsonProperty;

public class HitEvent extends GameEvent{
    @JsonProperty
    private long playableId;
    @JsonProperty
    private long playableType;

    public HitEvent() {}

    @Override
    void applyTo(int[][] board) {

    }

    public long getPlayableId() {
        return playableId;
    }

    public long getPlayableType() {
        return playableType;
    }
}
