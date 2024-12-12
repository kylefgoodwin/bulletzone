/**
 * Class created by Kyle Goodwin
 * Class to define a terrain from the rawValue from the server and decide what image
 * and type to give it
 */
package edu.unh.cs.cs619.bulletzone.model;

import edu.unh.cs.cs619.bulletzone.R;

public class Terrain extends BoardCell {
    protected String cellType;
    public final int meadow = 4000;
    public final int rocky = 4001;
    public final int hilly = 4002;
    public final int forest = 4003;
    public final int water = 4004;

    public Terrain(int val, int r, int c) {
        super(val, r, c);

        if (val == rocky){
            resourceID = R.drawable.rocky;
            cellType = "Rocky";
        } else if (val == hilly){
            resourceID = R.drawable.hilly;
            cellType = "Hilly";
        } else if (val == forest){
            resourceID = R.drawable.forest;
            cellType = "Forest";
        } else if (val == water) {
            resourceID = R.drawable.water;
            cellType = "Water";
        } else {
            resourceID = R.drawable.blank;
            cellType = "Meadow";
        }
    }

    public String getCellType() {
        return cellType;
    }
}