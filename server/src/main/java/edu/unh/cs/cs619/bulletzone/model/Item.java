package edu.unh.cs.cs619.bulletzone.model;

import java.util.Random;

public class Item extends FieldEntity {
    private static final String TAG = "Item";
    public static final int THINGAMAJIG = 1;
    public static final int ANTI_GRAV = 2;
    public static final int FUSION_REACTOR = 3;
    public static final int DEFLECTOR_SHIELD = 4;
    public static final int REPAIR_KIT = 5;
    private final int type;
    private final long creationTime;

    public Item(int type) {
        this.type = type;
        this.creationTime = System.currentTimeMillis();
    }

    @Override
    public int getIntValue() {
        return 3000 + type;
    }

    public int getType() {
        return type;
    }

    @Override
    public FieldEntity copy() {
        return new Item(type);
    }

    public double getCredits() {
        switch (type) {
            case THINGAMAJIG: return 100 + new Random().nextInt(901);
            case ANTI_GRAV: return 300;
            case FUSION_REACTOR: return 400;
            case DEFLECTOR_SHIELD: return 300;
            case REPAIR_KIT: return 200;
            default: return 0;
        }
    }

    public boolean isAntiGrav() { return type == ANTI_GRAV; }
    public boolean isFusionReactor() { return type == FUSION_REACTOR; }
    public boolean isDeflectorShield() { return type == DEFLECTOR_SHIELD; }
    public boolean isRepairKit() { return type == REPAIR_KIT; }
}