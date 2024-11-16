package edu.unh.cs.cs619.bulletzone.util;

import java.io.Serializable;
import java.util.ArrayList;

import edu.unh.cs.cs619.bulletzone.events.GameEvent;

public class ReplayDataFlat implements Serializable {

    private static final long serialVersionUID = 1L;

    private GridWrapper initialGrid;
    private GridWrapper initialTerrainGrid;

    public int[][] initialGridToSet;

    private long initialTimeStamp = -1;

    private long playerTankID = -1;

    private ArrayList<GameEvent> eventHistory;

    public ReplayDataFlat(
            GridWrapper initialGrid, GridWrapper initialTerrainGrid, ArrayList<GameEvent> eventHistory,
            long initialTimeStamp, int[][] initialGridToSet, long playerTankID) {
        this.initialGrid = initialGrid;
        this.initialTerrainGrid = initialTerrainGrid;
        this.initialGridToSet = initialGridToSet;
        this.initialTimeStamp = initialTimeStamp;
        this.eventHistory = eventHistory;
        this.playerTankID = playerTankID;
    }

    public GridWrapper getInitialGrid() {
        return initialGrid;
    }

    public GridWrapper getInitialTerrainGrid() {
        return initialTerrainGrid;
    }

    public ArrayList<GameEvent> getGameEvents() {
        return eventHistory;
    }

    public long getPlayerTankID() { return playerTankID; }

    public long getInitialTimestamp() {
        return initialTimeStamp;
    }
}
