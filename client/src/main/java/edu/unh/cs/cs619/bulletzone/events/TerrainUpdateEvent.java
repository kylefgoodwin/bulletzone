package edu.unh.cs.cs619.bulletzone.events;

import android.util.Log;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;

import java.io.Serializable;

@JsonTypeName("terrain")
public class TerrainUpdateEvent extends GameEvent implements Serializable {
    private static final String TAG = "TerrainUpdateEvent";
    private static final long serialVersionUID = 1L;

    @JsonProperty("type")
    private int playableType;

    @JsonProperty("hilly")
    private boolean hilly;

    @JsonProperty("forest")
    private boolean forest;

    @JsonProperty("rocky")
    private boolean rocky;

    public TerrainUpdateEvent() {} // Required empty constructor

    public TerrainUpdateEvent(int playableType, boolean hilly, boolean forest, boolean rocky) {
        super();
        this.playableType = playableType;
        this.hilly = hilly;
        this.forest = forest;
        this.rocky = rocky;
        System.out.println("Created TerrainUpdateEvent with type: " + playableType); // Using System.out instead of Log
    }

    @Override
    public void applyTo(int[][] board) {
        // No board updates needed for terrain effects
    }

    // Getters
    public int getPlayableType() { return playableType; }
    public boolean isHilly() { return hilly; }
    public boolean isForest() { return forest; }
    public boolean isRocky() { return rocky; }
}