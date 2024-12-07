package edu.unh.cs.cs619.bulletzone.model;

public class Bridge extends FieldEntity {

    int destructValue, pos;

    // Default constructor for an indestructible bridge
    public Bridge() {
        this.destructValue = 903; // Default value for bridge
    }

    // Constructor with parameters
    public Bridge(int destructValue, int pos) {
        this.destructValue = destructValue;
        this.pos = pos;
    }

    @Override
    public int getIntValue() {
        return destructValue;
    }

    @Override
    public FieldEntity copy() {
        return new Bridge(destructValue, pos);
    }

    public int getPos() {
        return pos;
    }

}
