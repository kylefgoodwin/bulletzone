package edu.unh.cs.cs619.bulletzone.model;

public class Deck extends FieldEntity {

    int destructValue, pos;

    // Default constructor for an indestructible deck
    public Deck() {
        this.destructValue = 904; // Default value for deck
    }

    // Constructor with parameters
    public Deck(int destructValue, int pos) {
        this.destructValue = destructValue;
        this.pos = pos;
    }

    @Override
    public int getIntValue() {
        return destructValue;
    }

    @Override
    public FieldEntity copy() {
        return new Deck(destructValue, pos);
    }

    public int getPos() {
        return pos;
    }

}
