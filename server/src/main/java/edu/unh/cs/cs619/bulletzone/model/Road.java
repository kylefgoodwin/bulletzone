package edu.unh.cs.cs619.bulletzone.model;

public class Road extends Improvement {

    int destructValue = 902;
    int pos;

    // Constructor for an indestructible road
    public Road() {
        super(902); // 902 represents the value for a road
    }

    // Override copy method to create a new instance of Road
    @Override
    public FieldEntity copy() {
        return new Road();
    }

    @Override
    public int getIntValue() {
        return destructValue;
    }

    public int getPos() {
        return pos;
    }

}
