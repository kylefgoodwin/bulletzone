package edu.unh.cs.cs619.bulletzone.events;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;

import java.io.Serializable;

@JsonTypeName("terrain")
public class TerrainUpdateEvent extends GameEvent implements Serializable {
    private static final long serialVersionUID = 1L;
    @JsonProperty
    private boolean hilly;
    @JsonProperty
    private boolean forest;
    @JsonProperty
    private boolean rocky;
    @JsonProperty
    private boolean water;
    @JsonProperty
    private int playableType;
    @JsonProperty
    private boolean positionChanged;
    @JsonProperty
    private int fromPosition;
    @JsonProperty
    private int toPosition;

    // Default constructor for Jackson
    public TerrainUpdateEvent() {}

    public TerrainUpdateEvent(boolean hilly, boolean forest, boolean rocky, boolean water, int playableType,
                              int fromPosition, int toPosition) {
        this.hilly = hilly;
        this.forest = forest;
        this.rocky = rocky;
        this.water = water;
        this.playableType = playableType;
        this.fromPosition = fromPosition;
        this.toPosition = toPosition;
        this.positionChanged = fromPosition != toPosition;
    }

    @Override
    public void applyTo(int[][] board) {
        // No board changes needed for terrain updates
    }

    // Getters
    public boolean isHilly() { return hilly; }
    public boolean isForest() { return forest; }
    public boolean isRocky() { return rocky; }
    public boolean isWater() { return rocky; }
    public int getPlayableType() { return playableType; }
    public boolean isPositionChanged() { return positionChanged; }
    public int getFromPosition() { return fromPosition; }
    public int getToPosition() { return toPosition; }

    // Setters needed for Jackson deserialization
    public void setHilly(boolean hilly) { this.hilly = hilly; }
    public void setForest(boolean forest) { this.forest = forest; }
    public void setRocky(boolean rocky) { this.rocky = rocky; }
    public void setPlayableType(int playableType) { this.playableType = playableType; }
    public void setPositionChanged(boolean positionChanged) { this.positionChanged = positionChanged; }
    public void setFromPosition(int fromPosition) { this.fromPosition = fromPosition; }
    public void setToPosition(int toPosition) { this.toPosition = toPosition; }
}