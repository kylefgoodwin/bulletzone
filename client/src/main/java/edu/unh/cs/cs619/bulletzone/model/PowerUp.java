/**
 * Class created by Kyle Goodwin
 * Class to define a terrain from the rawValue from the server and decide what image
 * and type to give it
 */
package edu.unh.cs.cs619.bulletzone.model;

import edu.unh.cs.cs619.bulletzone.R;

public class PowerUp extends BoardCell {
    protected String cellType;
    public final int thingamajig = 3001;
    public final int antigrav = 3002;
    public final int fusionreactor = 3003;

    public PowerUp(int val, int r, int c) {
        super(val, r, c);
        if (val == thingamajig) {
            resourceID = R.drawable.thingamajig_icon;
            cellType = "Thingamajig";
        } else if (val == antigrav){
            resourceID = R.drawable.anti_grav_icon;
            cellType = "AntiGrav";
        } else {
            resourceID = R.drawable.fusion_reactor_icon;
            cellType = "FusionReactor";
        }
    }

    public String getCellType() {
        return cellType;
    }
}
