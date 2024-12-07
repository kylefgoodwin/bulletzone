package edu.unh.cs.cs619.bulletzone.model;

public class Bridge extends Improvement {

    int destructValue = 903;
    int pos;

    // Constructor for an indestructible bridge
    public Bridge() {
        super(903);
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
