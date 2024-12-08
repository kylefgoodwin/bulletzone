package edu.unh.cs.cs619.bulletzone.model;

import edu.unh.cs.cs619.bulletzone.R;

/**
 * Class to define a wall from the rawValue from the server and determine
 * if it should be destructible and what it should look like.
 */
public class Factory extends BoardCell{
    protected String cellType;

    public Factory(int val, int r, int c) {
        super(val, r, c);

        resourceID = R.drawable.factory;
        cellType = "Factory";

    }

    public String getCellType() {
        return cellType;
    }
}
