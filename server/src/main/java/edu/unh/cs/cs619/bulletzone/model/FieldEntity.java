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
        return getIntValue() >= 4000 && getIntValue() <= 4003;
    }

    public boolean isItem() {
        int value = getIntValue();
        return value >= 3001 && value <= 3005;  // Updated range to include all power-ups
    }

    public boolean isPlayable(){
        return getIntValue() >= 10000000;
    }

    public boolean isMiningFacility(){return getIntValue() == 920;}

    public boolean isIndestructibleWall(){return getIntValue() == 1000;}
}

    /*public static final void registerEventBusListener(Object listener) {
        checkNotNull(listener);
        eventBus.register(listener);
    }

    public static final void unregisterEventBusListener(Object listener) {
        checkNotNull(listener);
        eventBus.unregister(listener);
    }*/

