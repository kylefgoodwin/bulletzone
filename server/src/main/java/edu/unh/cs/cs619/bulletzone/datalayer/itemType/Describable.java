package edu.unh.cs.cs619.bulletzone.datalayer.itemType;

public interface Describable {
    public boolean isContainer();
    public double getProperty(ItemProperty property);

    public double getSize();
    public double getWeight();
    public double getPrice();
}
