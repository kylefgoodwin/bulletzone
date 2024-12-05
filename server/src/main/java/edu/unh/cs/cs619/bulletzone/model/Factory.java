package edu.unh.cs.cs619.bulletzone.model;

public class Factory extends FieldEntity {

    int destructValue, pos;

    public Factory() {
        this.destructValue = 920;
    }

    public Factory(int destructValue, int pos) {
        this.destructValue = destructValue;
        this.pos = pos;
    }

    @Override
    public int getIntValue() {
        return destructValue;
    }

    @Override
    public FieldEntity copy() {
        return new Factory(destructValue, pos);
    }

    public int getPos(){
        return pos;
    }
}
