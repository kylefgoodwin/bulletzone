package edu.unh.cs.cs619.bulletzone.model;

public abstract class FieldEntity {
    //protected static final EventBus eventBus = new EventBus();
    protected FieldHolder parent;

    /**
     * Serializes the current {@link edu.unh.cs.cs619.bulletzone.model.FieldEntity} instance.
     *
     * @return Integer representation of the current {@link edu.unh.cs.cs619.bulletzone.model.FieldEntity}
     */
    public abstract int getIntValue();

    public FieldHolder getParent() {
        return parent;
    }

    public void setParent(FieldHolder parent) {
        this.parent = parent;
    }

    public int getPosition() { return parent.getPosition(); }

    public abstract FieldEntity copy();

    public void hit(int damage) {
    }

    public boolean isWall(){
        return getIntValue() >= 1000 && getIntValue() <= 2000;
    }

    public boolean isTankItem(){
        return getIntValue() >= 2000000 && getIntValue() < 3000000;
    }

    public boolean isTerrain(){
        return getIntValue() >= 4000 && getIntValue() <= 4004;
    }

    public boolean isItem() {
        int value = getIntValue();
        return value >= 3001 && value <= 3005;  // Updated range to include all power-ups
    }

    public boolean isPlayable(){
        return getIntValue() >= 10000000;
    }

    public boolean isMiningFacility(){return getIntValue() == 920;}

    public boolean isImprovement(){return getIntValue() == 902 || getIntValue() == 903 || getIntValue() == 920;}

    public boolean isRoad(){return getIntValue() == 902;}

    public boolean isBridge(){return getIntValue() == 903;}

    public boolean isDeck(){return getIntValue() == 904;}

    public boolean isFactory(){return getIntValue() == 920;}

    public boolean isWater(){
        int value = getIntValue();
        return value == 4004;}

    public boolean isIndestructibleWall(){return getIntValue() == Integer.MAX_VALUE;}
}

    /*public static final void registerEventBusListener(Object listener) {
        checkNotNull(listener);
        eventBus.register(listener);
    }

    public static final void unregisterEventBusListener(Object listener) {
        checkNotNull(listener);
        eventBus.unregister(listener);
    }*/

