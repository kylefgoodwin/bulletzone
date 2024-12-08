package edu.unh.cs.cs619.bulletzone.model;

public class Factory extends Improvement {

    int destructValue = 930;
    int pos;

    // Constructor for an indestructible road
    public Factory() {
        super(930); // 930 represents the value for a factory
    }

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
