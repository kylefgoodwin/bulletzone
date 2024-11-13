package edu.unh.cs.cs619.bulletzone.util;

import android.util.Log;

import java.util.Arrays;

/**
 * Created by simon on 10/1/14.
 */
public class GridWrapper {
    private int[][] grid;

    private long timeStamp;

    public GridWrapper() {
    }

    public GridWrapper(int[][] grid) {
        this.grid = grid;
    }

    public int[][] getGrid() {
//        Log.d("GridWrapper", Arrays.toString(grid));
        return this.grid;
    }

    public void setGrid(int[][] grid) {
        this.grid = grid;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }
}
