package edu.unh.cs.cs619.bulletzone.model;

import org.greenrobot.eventbus.EventBus;

import edu.unh.cs.cs619.bulletzone.model.events.HitEvent;

public class Wall extends FieldEntity {
    int destructValue, pos;
    private int life = 40;

    public Wall(){
        this.destructValue = 1000;
    }

    public Wall(int destructValue, int pos){
        this.destructValue = destructValue;
        this.pos = pos;
    }

    public int getLife() {
        return life;
    }

    public void setLife(int life) {
        this.life = life;
    }

    public void hit(int damage) {
        life -= damage;
//        if (life <= 0) {
//            //handle game over scenario
//        }
//        System.out.println("Tank id: " + id + " Tank Life: " + life);
//        EventBus.getDefault().post(new HitEvent((int) id, 1));
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