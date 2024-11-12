package edu.unh.cs.cs619.bulletzone.model.events;

import com.fasterxml.jackson.annotation.JsonProperty;

public class HitEvent extends GameEvent{
    @JsonProperty
    private int playableId;

    @JsonProperty
    private int playableType;

    public HitEvent() {}

    public HitEvent(int playableId, int playableType) {
        this.playableId = playableId;
        this.playableType = playableType;
    }

    @Override
    public String toString() {
        return "Player's : " + playableId +  playableType + " Hit " +
                super.toString();
    }
}
