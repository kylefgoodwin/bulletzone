package edu.unh.cs.cs619.bulletzone.model;

public enum PowerUpType {
    ANTI_GRAV(2, 300),
    FUSION_REACTOR(3, 400),
    DEFLECTOR_SHIELD(4, 300),
    REPAIR_KIT(5, 200);

    private final int id;
    private final int creditValue;

    PowerUpType(int id, int creditValue) {
        this.id = id;
        this.creditValue = creditValue;
    }

    public int getId() {
        return id;
    }

    public int getCreditValue() {
        return creditValue;
    }

    public static PowerUpType fromId(int id) {
        for (PowerUpType type : values()) {
            if (type.getId() == id) {
                return type;
            }
        }
        return null;
    }
}