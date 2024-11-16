package edu.unh.cs.cs619.bulletzone.model;

public class MiningFacility extends FieldEntity {

    int destructValue, pos;

    public MiningFacility() {
        this.destructValue = 920;
    }

    public MiningFacility(int destructValue, int pos) {
        this.destructValue = destructValue;
        this.pos = pos;
    }

    @Override
    public int getIntValue() {
        return destructValue;
    }

    @Override
    public FieldEntity copy() {
        return new MiningFacility(destructValue, pos);
    }

    public int getPos(){
        return pos;
    }
}
