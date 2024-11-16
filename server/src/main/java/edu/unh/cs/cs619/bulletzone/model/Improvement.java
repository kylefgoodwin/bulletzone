package edu.unh.cs.cs619.bulletzone.model;

public class Improvement extends FieldEntity {//just road, use wall class for walls
    int value;

    public Improvement(int value) {
        //902 == pavement
        //903 == deck
        //920 == factory
        //1000 is unbreakable
        //1001-2000 is breakable
        this.value = value;
    }

    @Override
    public FieldEntity copy() {
        return new Improvement(value);
    }

    @Override
    public int getIntValue() {return value;}

    @Override
    public String toString() {
        return "IM";
    }
}
