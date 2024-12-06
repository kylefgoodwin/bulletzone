package edu.unh.cs.cs619.bulletzone.model;

public class Road extends FieldEntity {

    int destructValue, pos;

    // Default constructor for an indestructible road
    public Road() {
        this.destructValue = 902; // Default value for road
    }

    // Constructor with parameters
    public Road(int destructValue, int pos) {
        this.destructValue = destructValue;
        this.pos = pos;
    }

    @Override
    public int getIntValue() {
        return destructValue;
    }

    @Override
    public FieldEntity copy() {
        return new Road(destructValue, pos);
    }

    public int getPos() {
        return pos;
    }

}
