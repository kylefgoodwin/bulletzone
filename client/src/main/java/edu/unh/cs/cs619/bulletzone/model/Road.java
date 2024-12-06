package edu.unh.cs.cs619.bulletzone.model;

import edu.unh.cs.cs619.bulletzone.R;

/**
 * Class to define a wall from the rawValue from the server and determine
 * if it should be destructible and what it should look like.
 */
public class Road extends BoardCell{
    protected String cellType;

    public Road(int val, int r, int c) {
        super(val, r, c);

        resourceID = R.drawable.goldmine;
        cellType = "Road";

    }

    public String getCellType() {
        return cellType;
    }
}
