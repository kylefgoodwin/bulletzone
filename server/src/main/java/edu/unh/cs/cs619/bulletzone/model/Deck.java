package edu.unh.cs.cs619.bulletzone.model;

public class Deck extends Improvement {

    int destructValue = 904;
    int pos;

    // Constructor for an indestructible deck
    public Deck() {
        super(904); // 902 represents the value for a deck
    }

    @Override
    public FieldEntity copy() {
        return new Deck();
    }

    @Override
    public int getIntValue() {
        return destructValue;
    }

    public int getPos() {
        return pos;
    }

}
