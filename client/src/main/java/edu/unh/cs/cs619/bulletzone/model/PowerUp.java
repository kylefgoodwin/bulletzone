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
    public final int deflectorshield = 3004;
    public final int repairkit = 3005;

    public PowerUp(int val, int r, int c) {
        super(val, r, c);
        if (val == thingamajig) {
            resourceID = R.drawable.thingamajig_icon;
            cellType = "Thingamajig";
        } else if (val == antigrav) {
            resourceID = R.drawable.anti_grav_icon;
            cellType = "AntiGrav";
        } else if (val == fusionreactor) {
            resourceID = R.drawable.fusion_reactor_icon;
            cellType = "FusionReactor";
        } else if (val == deflectorshield) {
            resourceID = R.drawable.shield_powerup;
            cellType = "DeflectorShield";
        } else if (val == repairkit) {
            resourceID = R.drawable.repair_kit;
            cellType = "RepairKit";
        }
    }

    public String getCellType() {
        return cellType;
    }
}