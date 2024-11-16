package edu.unh.cs.cs619.bulletzone.model;

import edu.unh.cs.cs619.bulletzone.R;

/**
 * Template class for all simulation board, board cell.
 * Holds information for the Simboard about the type of cell, its position, and its rawValue from the server
 */
public class BoardCell {
    protected int resourceID; /// The resource ID for the image to display
    protected int rawValue; /// The value as represented on the server
    protected int row, col; /// The location of this cell on the grid

    public BoardCell(int val, int r, int c) {
        rawValue = val;
        row = r;
        col = c;
        resourceID = R.drawable.blank;
    }

    public Integer getResourceID() {
        return resourceID;
    }

    public int getRotation() { return 0; }

    public int getRawValue() { return rawValue; }

    public String getCellType() {
        return "Empty";
    }

    public String getCellInfo() {
        String baseInfo = "Location: (" + this.col + ", " + this.row + ")";
        if (rawValue >= 3000 && rawValue <= 3003 || rawValue >= 4000 && rawValue <= 4003) {
            return baseInfo + " - " + getCellType();
        }
        return baseInfo;
    }
}