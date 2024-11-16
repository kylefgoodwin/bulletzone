package edu.unh.cs.cs619.bulletzone.model;

import edu.unh.cs.cs619.bulletzone.R;

/**
 * Class to define a wall from the rawValue from the server and determine
 * if it should be destructible and what it should look like.
 */
public class Wall extends BoardCell{
    protected String cellType;
    public final int indestructibleWallType = 1000;

    public Wall(int val, int r, int c) {
        super(val, r, c);

        if (val == indestructibleWallType) {
            resourceID = R.drawable.trans_indestwall;
            cellType = "IndestructibleWall";
        } else {
            resourceID = R.drawable.trans_destwall;
            cellType = "DestructibleWall";
        }
    }

    public String getCellType() {
        return cellType;
    }
}
