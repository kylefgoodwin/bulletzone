package edu.unh.cs.cs619.bulletzone.model;

public class Factory extends Playable {

    int destructValue = 930;
    int pos;

    // Constructor for an indestructible road
    public Factory(long id, Direction direction, String ip) {
        super(id, direction, ip); // 930 represents the value for a factory
        life = 50; // Builders start with 80 life points
        playableType = 5;
    }

    @Override
    public boolean handleTerrainConstraints(Terrain terrain, long millis) {
        if (terrain.isHilly() && millis < (getLastMoveTime() + (getAllowedMoveInterval() * 1.5))) {
            return false;
        } else if (terrain.isForest() && millis < (getLastMoveTime() + (getAllowedMoveInterval() * 2))) {
            return false;
        }
        return true;
    }

    @Override
    public boolean handleImprovements(Improvement improvement, long millis) {
        if (improvement.isRoad() && millis < (getLastMoveTime() + (getAllowedMoveInterval() / 2))) {
            return false;
        } else if (improvement.isBridge() && millis < (getLastMoveTime() + getAllowedMoveInterval())) {
            return false;
        } else if (improvement.isDeck() && millis < (getLastMoveTime() + getAllowedMoveInterval())) {
            return false;
        }
        return true;
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
