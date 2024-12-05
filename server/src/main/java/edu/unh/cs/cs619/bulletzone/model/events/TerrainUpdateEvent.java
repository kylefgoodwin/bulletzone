package edu.unh.cs.cs619.bulletzone.model.events;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeName("terrain")
public class TerrainUpdateEvent extends GameEvent {
    @JsonProperty
    private final boolean hilly;
    @JsonProperty
    private final boolean forest;
    @JsonProperty
    private final boolean rocky;
    @JsonProperty
    private final int playableType;
    @JsonProperty
    private final boolean positionChanged;
    @JsonProperty
    private final int fromPosition;
    @JsonProperty
    private final int toPosition;

    public TerrainUpdateEvent(boolean hilly, boolean forest, boolean rocky, int playableType,
                              int fromPosition, int toPosition) {
        this.hilly = hilly;
        this.forest = forest;
        this.rocky = rocky;
        this.playableType = playableType;
        this.fromPosition = fromPosition;
        this.toPosition = toPosition;
        this.positionChanged = fromPosition != toPosition;
    }

    @Override
    public void applyTo(int[][] board) {
        // No board changes needed for terrain updates
    }

    public boolean isHilly() { return hilly; }
    public boolean isForest() { return forest; }
    public boolean isRocky() { return rocky; }
    public int getPlayableType() { return playableType; }
    public boolean isPositionChanged() { return positionChanged; }
    public int getFromPosition() { return fromPosition; }
    public int getToPosition() { return toPosition; }

    @Override
    public String toString() {
        return "Terrain Update - Hilly: " + hilly +
                " Forest: " + forest +
                " Rocky: " + rocky +
                " Type: " + playableType +
                " From: " + fromPosition +
                " To: " + toPosition;
    }
}