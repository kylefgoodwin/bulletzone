package edu.unh.cs.cs619.bulletzone.model;

public class Improvement extends FieldEntity {//just road, use wall class for walls
    int value;

    public Improvement(int value) {
        //902 == road
        //903 == bridge
        //904 == deck
        //930 == factory
        //920 == mining facility
        //MAX is unbreakable
        //1001-2000 is breakable
        this.value = value;
    }

    @Override
    public FieldEntity copy() {
        return new Improvement(value);
    }

    public boolean isMiningFacility(){return getIntValue() == 920;}

    public boolean isImprovement(){return getIntValue() == 902 || getIntValue() == 903 || getIntValue() == 904 || getIntValue() == 920 || getIntValue() == 930 || getIntValue() == Integer.MAX_VALUE || (getIntValue() >= 1000 && getIntValue() <= 2000);}

    public boolean isRoad(){return getIntValue() == 902;}

    public boolean isBridge(){return getIntValue() == 903;}

    public boolean isDeck(){return getIntValue() == 904;}

    public boolean isFactory(){return getIntValue() == 930;}

    public boolean isIndestructibleWall(){return getIntValue() == Integer.MAX_VALUE;}

    public boolean isWall(){
        return getIntValue() >= 1000 && getIntValue() <= 2000;
    }

    @Override
    public int getIntValue() {return value;}

    @Override
    public String toString() {
        return "IM";
    }
}
