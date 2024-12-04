package edu.unh.cs.cs619.bulletzone.util;

import android.util.Log;

import java.util.ArrayList;

import edu.unh.cs.cs619.bulletzone.events.GameEvent;

/**
 * Made by Alec Rydeen
 *
 * Singleton object to store the necessary data to create a replay of the current game
 */
public class ReplayData {

    private static final String TAG = "ReplayData";

    private static ReplayData replayData = null;

    private GridWrapper initialGrid;
    private GridWrapper initialTerrainGrid;

    public int[][] initialGridToSet;

    private long initialTimeStamp = -1;

    private long playerTankID = -1;

    private ArrayList<GameEvent> eventHistory = new ArrayList<>();

    private ReplayData() {}

    public static synchronized ReplayData getReplayData() {
        if (replayData == null) {
            replayData = new ReplayData();
        }
        return replayData;
    }

    /**
     * Formats the current replay singleton to a flat form for storage.
     * @return returns the flat version of the replay
     */
    public ReplayDataFlat turnToFlat() {
        return new ReplayDataFlat(initialGrid, initialTerrainGrid, eventHistory, initialTimeStamp,
                initialGridToSet, playerTankID);
    }

    /**
     * Loads the given flat replay data into the singleton to be used in a replay
     * @param flatData flat data to be loaded
     */
    public void loadReplay(ReplayDataFlat flatData) {
        this.initialGrid = flatData.getInitialGrid();
        this.initialTerrainGrid = flatData.getInitialTerrainGrid();
        this.eventHistory = flatData.getGameEvents();
        this.initialTimeStamp = flatData.getInitialTimestamp();
        this.initialGridToSet = flatData.initialGridToSet;
        this.playerTankID = flatData.getPlayerTankID();
    }

    public void clearReplay() {
        replayData = new ReplayData();
    }

    public long getPlayerTankID() {
        return playerTankID;
    }

    public void setPlayerTankID(long playerTankID) {
        this.playerTankID = playerTankID;
    }

    public void setInitialGrids(GridWrapper initialGrid, GridWrapper initialTerrainGrid) {
        this.initialGrid = initialGrid;
        this.initialTerrainGrid = initialTerrainGrid;
    }

    public GridWrapper getInitialGrid() {
        return initialGrid;
    }

    public GridWrapper getInitialTerrainGrid() {
        return initialTerrainGrid;
    }

    public void addGameEvent(GameEvent event) {
        eventHistory.add(event);
//        Log.d(TAG, "Added Event: " + event.toString());
    }

    public GameEvent getEventAt(int index) {
        if (index < eventHistory.size()) {
            if (eventHistory.get(index) != null) {
                return eventHistory.get(index);
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    public void setInitialTimeStamp(long initialTimestamp) {
        this.initialTimeStamp = initialTimestamp;
    }

    public long getInitialTimestamp() {
        return initialTimeStamp;
    }

    public String toString() {
        StringBuilder returnString  = new StringBuilder();
        int[][] grid = initialGridToSet;
        for (int i = 0; i < grid.length; i++) {
            for (int j = 0; j < grid[i].length; j++) {
                returnString.append(grid[i][j]);
                returnString.append("(").append(i).append(",").append(j).append(")");
                returnString.append(" ");
            }
        }
        return returnString.toString();
    }
}
