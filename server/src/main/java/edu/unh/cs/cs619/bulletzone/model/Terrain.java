/**
 * Class created by Kyle Goodwin
 * Defines terrain and gives its derived methods from FieldEntity
 */
package edu.unh.cs.cs619.bulletzone.model;

public class Terrain extends FieldEntity {
    int value;
    private final int type;

    public Terrain(){
        this.type = 0;
        this.value = 4000;
    }

    public Terrain(int type){
        this.type = type;
        this.value = 4000 + type;
    }

    public boolean isMeadow(){
        return type == 0;
    }

    public boolean isRocky(){
        return type == 1;
    }

    public boolean isHilly(){
        return type == 2;
    }

    public boolean isForest(){
        return type == 3;
    }


    @Override
    public FieldEntity copy() {
        return new Terrain();
    }

    @Override
    public int getIntValue() {
        return 4000 + type;
    }

    @Override
    public String toString() {
        return "T";
    }

}
