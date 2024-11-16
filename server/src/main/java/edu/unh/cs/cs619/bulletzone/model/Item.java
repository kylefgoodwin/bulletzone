package edu.unh.cs.cs619.bulletzone.model;

import java.util.Random;

public class Item extends FieldEntity {
    private static final String TAG = "Item";
    private final int type; // 1 = Thingamajig, 2 = AntiGrav, 3 = FusionReactor
    private static final Random random = new Random();

    public Item(int type) {
        this.type = type;
    }

    @Override
    public int getIntValue() {
        return 3000 + type;
    }

    @Override
    public FieldEntity copy() {
        return new Item(type);
    }

    @Override
    public String toString() {
        return "I";
    }

    public int getType() {
        return type;
    }

    public double getCredits() {
        if (type == 1) { // Thingamajig
            return 100 + random.nextInt(901);
        }
        return 0;
    }

    public boolean isAntiGrav() {
        return type == 2;
    }

    public boolean isFusionReactor() {
        return type == 3;
    }

    @Override
    public void hit(int damage) {
        if (parent != null) {
            parent.clearField();
        }
    }
}