package edu.unh.cs.cs619.bulletzone.model;

public class Wall extends FieldEntity {
    int destructValue, pos;

    public Wall(){
        this.destructValue = 1000;
    }

    public Wall(int destructValue, int pos){
        this.destructValue = destructValue;
        this.pos = pos;
    }

    @Override
    public FieldEntity copy() {
        return new Wall();
    }

    @Override
    public int getIntValue() {
        return destructValue;
    }

    @Override
    public String toString() {
        return "W";
    }

    public int getPos(){
        return pos;
    }
}