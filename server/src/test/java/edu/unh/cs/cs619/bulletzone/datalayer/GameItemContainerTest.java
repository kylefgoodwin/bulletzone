package edu.unh.cs.cs619.bulletzone.datalayer;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

import org.junit.BeforeClass;
import org.junit.Test;

import edu.unh.cs.cs619.bulletzone.datalayer.item.GameItem;
import edu.unh.cs.cs619.bulletzone.datalayer.item.GameItemContainer;
import edu.unh.cs.cs619.bulletzone.datalayer.itemType.ItemProperty;

public class GameItemContainerTest {

    static BulletZoneData db;

    @BeforeClass
    static public void setup() {
        db = new BulletZoneData();
        db.rebuildData();
    }

    @Test
    public void getProperty_capacityWhenContainsContainers_returnsOverallCapactiy() {
        double totalCapacity = 0.0;
        final ItemProperty Capacity = db.properties.Capacity;
        GameItemContainer garage = db.items.createContainer(db.types.GarageBay);
        totalCapacity += garage.getProperty(Capacity);
        for (int i = 0; i < 3; i++)
        {
            GameItemContainer tank = db.items.createContainer(db.types.TankFrame);
            totalCapacity += tank.getProperty(Capacity);
            db.items.addItemToContainer(tank, garage);
        }
        assertThat(garage.getProperty(db.properties.Capacity), is(totalCapacity));
    }

    @Test
    public void getLocalProperty_capacityWhenContainsContainers_returnsOnlyContainerCapactiy() {
        final ItemProperty Capacity = db.properties.Capacity;
        GameItemContainer garage = db.items.createContainer(db.types.GarageBay);
        double garageCapacity = garage.getProperty(Capacity);
        for (int i = 0; i < 3; i++)
        {
            GameItemContainer tank = db.items.createContainer(db.types.TankFrame);
            db.items.addItemToContainer(tank, garage);
        }
        assertThat(garage.getLocalProperty(db.properties.Capacity), is(garageCapacity));
    }

    @Test
    public void getProperty_SizeWhenContainsContainers_returnsAggregateSize() {
        final ItemProperty Size = db.properties.Size;
        GameItemContainer tank = db.items.createContainer(db.types.TankFrame);
        double tankSize = tank.getSize();
        for (int i = 0; i < 3; i++)
        {
            GameItemContainer expand = db.items.createContainer(db.types.VehicleExpansionFrame);
            db.items.addItemToContainer(expand, tank);
            tankSize += expand.getSize();
            GameItem weapon = db.items.create(db.types.TankCannon);
            db.items.addItemToContainer(weapon, expand);
            tankSize += weapon.getSize();
        }
        assertThat(tank.getProperty(db.properties.Size), is(tankSize));
    }

    @Test
    public void getProperty_WeightModifierWhenContainsGravAssist_returnsAggregateWeightModifier() {
        final ItemProperty prop = db.properties.WeightModifier;
        GameItemContainer tank = db.items.createContainer(db.types.TankFrame);
        double propVal = tank.getProperty(prop);
        for (int i = 0; i < 3; i++)
        {
            GameItem add = db.items.create(db.types.GravAssist);
            db.items.addItemToContainer(add, tank);
            propVal *= add.getProperty(prop);
        }
        assertThat(tank.getProperty(prop), is(propVal));
    }

    @Test
    public void getProperty_ArmorWhenContainsDeflectorShield_returnsAggregateArmor() {
        final ItemProperty prop = db.properties.Armor;
        GameItemContainer tank = db.items.createContainer(db.types.TankFrame);
        double propVal = tank.getProperty(prop);
        for (int i = 0; i < 3; i++)
        {
            GameItem add = db.items.create(db.types.DeflectorShield);
            db.items.addItemToContainer(add, tank);
            propVal += add.getProperty(prop);
        }
        assertThat(tank.getProperty(prop), is(propVal));
    }

    @Test
    public void getProperty_InstanceLimitWhenContainsFusionGenerator_returnsAggregateInstanceLimit() {
        final ItemProperty prop = db.properties.InstanceLimit;
        GameItemContainer tank = db.items.createContainer(db.types.TankFrame);
        GameItem cannon = db.items.create(db.types.TankCannon);
        db.items.addItemToContainer(cannon, tank);
        double propVal = tank.getProperty(prop);
        for (int i = 0; i < 3; i++)
        {
            GameItem add = db.items.create(db.types.FusionGenerator);
            db.items.addItemToContainer(add, tank);
            propVal += add.getProperty(prop);
        }
        assertThat(tank.getProperty(prop), is(propVal / 4.0));
    }

    @Test
    public void getParent_OnItemWithoutParentOrWithRemovedParent_returnsNull() {
        GameItemContainer tank = db.items.createContainer(db.types.TankFrame);
        GameItem cannon = db.items.create(db.types.TankCannon);
        assertNull(cannon.getParent());
        db.items.addItemToContainer(cannon, tank);
        db.items.removeItemFromContainer(cannon, tank);
        assertNull(cannon.getParent());
        assertNull(tank.getParent());
    }

    @Test
    public void getParent_OnItemWithParentThatChagnes_returnsCorrectParent() {
        GameItemContainer tank1 = db.items.createContainer(db.types.TankFrame);
        GameItemContainer tank2 = db.items.createContainer(db.types.TankFrame);
        GameItem cannon = db.items.create(db.types.TankCannon);
        db.items.addItemToContainer(cannon, tank1);
        assertThat(cannon.getParent(), is(tank1));
        db.items.removeItemFromContainer(cannon, tank1);
        db.items.addItemToContainer(cannon, tank2);
        assertThat(cannon.getParent(), is(tank2));
    }
}