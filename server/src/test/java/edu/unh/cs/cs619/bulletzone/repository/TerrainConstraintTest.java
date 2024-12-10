package edu.unh.cs.cs619.bulletzone.repository;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.HashMap;
import java.util.Map;
import org.javatuples.Pair;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


import edu.unh.cs.cs619.bulletzone.model.Builder;
import edu.unh.cs.cs619.bulletzone.model.Bullet;
import edu.unh.cs.cs619.bulletzone.model.Direction;
import edu.unh.cs.cs619.bulletzone.model.FieldHolder;
import edu.unh.cs.cs619.bulletzone.model.IllegalTransitionException;
import edu.unh.cs.cs619.bulletzone.model.LimitExceededException;
import edu.unh.cs.cs619.bulletzone.model.Playable;
import edu.unh.cs.cs619.bulletzone.model.Ship;
import edu.unh.cs.cs619.bulletzone.model.Soldier;
import edu.unh.cs.cs619.bulletzone.model.Tank;
import edu.unh.cs.cs619.bulletzone.model.Game;
import edu.unh.cs.cs619.bulletzone.model.TankDoesNotExistException;
import edu.unh.cs.cs619.bulletzone.model.Terrain;

public class TerrainConstraintTest {
    private Game game; // Mock the game context
    private String tankIp; // Represent the tank IP as a string

    @Before
    public void setup() {
        // Initialize mocks and test data
        game = mock(Game.class);
        tankIp = "192.168.1.1"; // Example IP, use a value suitable for your implementation
    }

    @Test
    public void testMoveTankToHillyTerrain_ConstraintApplied() throws Exception {
        // Arrange
        Tank tank = new Tank(1L, Direction.Up, tankIp);
        tank.setAllowedMoveInterval(1000); // 1 second delay between moves
        tank.setLastMoveTime(System.currentTimeMillis());

        FieldHolder currentField = mock(FieldHolder.class);
        FieldHolder nextField = mock(FieldHolder.class);
        Terrain hillyTerrain = mock(Terrain.class);

        tank.setParent(currentField);

        // Mock behaviors
        when(currentField.getNeighbor(Direction.Up)).thenReturn(nextField);
        when(nextField.isTerrainPresent()).thenReturn(true);
        when(nextField.getTerrainEntityHolder()).thenReturn(hillyTerrain);
        when(hillyTerrain.isHilly()).thenReturn(true);

        // Act
        MoveCommand moveCommand = new MoveCommand(tank, 1, game, Direction.Up, System.currentTimeMillis());
        boolean result = moveCommand.execute();

        // Assert
        Assert.assertFalse(result); // Movement should fail due to the hilly terrain constraint
        verify(currentField, never()).clearField();
        verify(nextField, never()).setFieldEntity(tank);
    }

    @Test
    public void testMoveTankToForestTerrain_ConstraintApplied() throws Exception {
        // Arrange
        Tank tank = new Tank(1L, Direction.Up, tankIp);
        tank.setAllowedMoveInterval(1000); // 1 second delay between moves
        tank.setLastMoveTime(System.currentTimeMillis());

        FieldHolder currentField = mock(FieldHolder.class);
        FieldHolder nextField = mock(FieldHolder.class);
        Terrain forestTerrain = mock(Terrain.class);

        tank.setParent(currentField);

        // Mock behaviors
        when(currentField.getNeighbor(Direction.Up)).thenReturn(nextField);
        when(nextField.isTerrainPresent()).thenReturn(true);
        when(nextField.getTerrainEntityHolder()).thenReturn(forestTerrain);
        when(forestTerrain.isForest()).thenReturn(true);

        // Act
        MoveCommand moveCommand = new MoveCommand(tank, 1, game, Direction.Up, System.currentTimeMillis());
        boolean result = moveCommand.execute();

        // Assert
        Assert.assertFalse(result); // Movement should fail due to the forest terrain constraint
        verify(currentField, never()).clearField();
        verify(nextField, never()).setFieldEntity(tank);
    }

    @Test
    public void testMoveBuilderToRockyTerrain_ConstraintApplied() throws Exception {
        // Arrange
        Builder builder = new Builder(1L, Direction.Up, tankIp);
        builder.setAllowedMoveInterval(1000); // 1 second delay between moves
        builder.setLastMoveTime(System.currentTimeMillis());

        FieldHolder currentField = mock(FieldHolder.class);
        FieldHolder nextField = mock(FieldHolder.class);
        Terrain rockyTerrain = mock(Terrain.class);

        builder.setParent(currentField);

        // Mock behaviors
        when(currentField.getNeighbor(Direction.Up)).thenReturn(nextField);
        when(nextField.isTerrainPresent()).thenReturn(true);
        when(nextField.getTerrainEntityHolder()).thenReturn(rockyTerrain);
        when(rockyTerrain.isRocky()).thenReturn(true);

        // Act
        MoveCommand moveCommand = new MoveCommand(builder, 2, game, Direction.Up, System.currentTimeMillis());
        boolean result = moveCommand.execute();

        // Assert
        Assert.assertFalse(result); // Movement should fail due to the rocky terrain constraint
        verify(currentField, never()).clearField();
        verify(nextField, never()).setFieldEntity(builder);
    }

    @Test
    public void testMoveSoldierToForestTerrain_Success() throws Exception {
        // Arrange
        Soldier soldier = new Soldier(1L, Direction.Up, tankIp);
        soldier.setAllowedMoveInterval(1000); // 1 second delay between moves
        soldier.setLastMoveTime(System.currentTimeMillis());

        FieldHolder currentField = mock(FieldHolder.class);
        FieldHolder nextField = mock(FieldHolder.class);
        Terrain forestTerrain = mock(Terrain.class);
        Game game = mock(Game.class); // Mock the game object

        soldier.setParent(currentField);

        // Mock behaviors
        when(currentField.getNeighbor(Direction.Up)).thenReturn(nextField);
        when(nextField.isTerrainPresent()).thenReturn(true);
        when(nextField.getTerrainEntityHolder()).thenReturn(forestTerrain);
        when(forestTerrain.isForest()).thenReturn(true);

        // Debugging to verify the mock values
        System.out.println("Mocked currentField neighbor: " + currentField.getNeighbor(Direction.Up));
        System.out.println("Mocked nextField terrain: " + nextField.getTerrainEntityHolder());
        System.out.println("Forest terrain is forest: " + forestTerrain.isForest()); // Ensure this is true

        // Act
        MoveCommand moveCommand = new MoveCommand(soldier, 3, game, Direction.Up, System.currentTimeMillis());
        boolean result = moveCommand.execute();

        // Debugging the result
        System.out.println("Result of execute(): " + result);

        // Assert
        Assert.assertTrue("The soldier should have moved successfully.", result); // Soldier should succeed in moving to the forest terrain
        verify(currentField).clearField();
        verify(nextField).setFieldEntity(soldier);

        // Further checks
        // Add additional assertions to check if the expected actions were performed.
    }

    @Test
    public void testMoveTankToWaterTerrain_Fails() throws Exception {
        // Arrange
        Tank tank = new Tank(1L, Direction.Up, tankIp);
        tank.setAllowedMoveInterval(1000); // 1 second delay between moves
        tank.setLastMoveTime(System.currentTimeMillis());

        FieldHolder currentField = mock(FieldHolder.class);
        FieldHolder nextField = mock(FieldHolder.class);
        Terrain waterTerrain = mock(Terrain.class);

        tank.setParent(currentField);

        // Mock behaviors
        when(currentField.getNeighbor(Direction.Up)).thenReturn(nextField);
        when(nextField.isTerrainPresent()).thenReturn(true);
        when(nextField.getTerrainEntityHolder()).thenReturn(waterTerrain);
        when(waterTerrain.isWater()).thenReturn(true);

        // Act
        MoveCommand moveCommand = new MoveCommand(tank, 0, game, Direction.Up, System.currentTimeMillis() + 1001);
        boolean result = moveCommand.execute();

        // Assert
        Assert.assertFalse(result); // Movement should fail due to water terrain
        verify(currentField, never()).clearField();
        verify(nextField, never()).setFieldEntity(tank);
    }

    @Test
    public void testMoveBuilderToWaterTerrain_Fails() throws Exception {
        // Arrange
        Builder builder = new Builder(1L, Direction.Up, tankIp);
        builder.setAllowedMoveInterval(1000); // 1 second delay between moves
        builder.setLastMoveTime(System.currentTimeMillis());

        FieldHolder currentField = mock(FieldHolder.class);
        FieldHolder nextField = mock(FieldHolder.class);
        Terrain waterTerrain = mock(Terrain.class);

        builder.setParent(currentField);

        // Mock behaviors
        when(currentField.getNeighbor(Direction.Up)).thenReturn(nextField);
        when(nextField.isTerrainPresent()).thenReturn(true);
        when(nextField.getTerrainEntityHolder()).thenReturn(waterTerrain);
        when(waterTerrain.isWater()).thenReturn(true);

        // Act
        MoveCommand moveCommand = new MoveCommand(builder, 1, game, Direction.Up, System.currentTimeMillis() + 1001);
        boolean result = moveCommand.execute();

        // Assert
        Assert.assertFalse(result); // Movement should fail due to water terrain
        verify(currentField, never()).clearField();
        verify(nextField, never()).setFieldEntity(builder);
    }

    @Test
    public void testMoveSoldierToWaterTerrain_Fails() throws Exception {
        // Arrange
        Soldier soldier = new Soldier(1L, Direction.Up, tankIp);
        soldier.setAllowedMoveInterval(1000); // 1 second delay between moves
        soldier.setLastMoveTime(System.currentTimeMillis());

        FieldHolder currentField = mock(FieldHolder.class);
        FieldHolder nextField = mock(FieldHolder.class);
        Terrain waterTerrain = mock(Terrain.class);
        Game game = mock(Game.class); // Mock the game object

        soldier.setParent(currentField);

        // Mock behaviors
        when(currentField.getNeighbor(Direction.Up)).thenReturn(nextField);
        when(nextField.isTerrainPresent()).thenReturn(true);
        when(nextField.getTerrainEntityHolder()).thenReturn(waterTerrain);
        when(waterTerrain.isWater()).thenReturn(true);

        // Debugging to verify the mock values
        System.out.println("Mocked currentField neighbor: " + currentField.getNeighbor(Direction.Up));
        System.out.println("Mocked nextField terrain: " + nextField.getTerrainEntityHolder());
        System.out.println("Water terrain is water: " + waterTerrain.isWater()); // Ensure this is true

        // Act
        MoveCommand moveCommand = new MoveCommand(soldier, 2, game, Direction.Up, System.currentTimeMillis() + 1001);
        boolean result = moveCommand.execute();

        // Debugging the result
        System.out.println("Result of execute(): " + result);

        // Assert
        Assert.assertFalse(result); // Movement should fail due to water terrain
        verify(currentField, never()).clearField();
        verify(nextField, never()).setFieldEntity(soldier);

        // Further checks
        // Add additional assertions to check if the expected actions were performed.
    }

    @Test
    public void testMoveShipToWaterTerrain_Success() throws Exception {
        // Arrange
        Ship ship = new Ship(1L, Direction.Up, tankIp);
        ship.setAllowedMoveInterval(1000); // 1 second delay between moves
        ship.setLastMoveTime(System.currentTimeMillis());

        FieldHolder currentField = mock(FieldHolder.class);
        FieldHolder nextField = mock(FieldHolder.class);
        Terrain waterTerrain = mock(Terrain.class);

        ship.setParent(currentField);

        // Mock behaviors
        when(currentField.getNeighbor(Direction.Up)).thenReturn(nextField);
        when(nextField.isTerrainPresent()).thenReturn(true);
        when(nextField.getTerrainEntityHolder()).thenReturn(waterTerrain);
        when(waterTerrain.isWater()).thenReturn(true);

        // Act
        MoveCommand moveCommand = new MoveCommand(ship, 3, game, Direction.Up, System.currentTimeMillis() + 1001);
        boolean result = moveCommand.execute();

        // Debugging the result
        System.out.println("Result of execute(): " + result);

        // Assert
        Assert.assertTrue("The ship should have moved successfully.", result); // Ship should succeed in moving to the water terrain
        verify(currentField).clearField();
        verify(nextField).setFieldEntity(ship);
    }

    @Test
    public void testMoveShipToMeadowTerrain_Fails() throws Exception {
        // Arrange
        Ship ship = new Ship(1L, Direction.Up, tankIp);
        ship.setAllowedMoveInterval(1000); // 1 second delay between moves
        ship.setLastMoveTime(System.currentTimeMillis());

        FieldHolder currentField = mock(FieldHolder.class);
        FieldHolder nextField = mock(FieldHolder.class);
        Terrain meadowTerrain = mock(Terrain.class);

        ship.setParent(currentField);

        // Mock behaviors
        when(currentField.getNeighbor(Direction.Up)).thenReturn(nextField);
        when(nextField.isTerrainPresent()).thenReturn(true);
        when(nextField.getTerrainEntityHolder()).thenReturn(meadowTerrain);
        when(meadowTerrain.isMeadow()).thenReturn(true);

        // Act
        MoveCommand moveCommand = new MoveCommand(ship, 3, game, Direction.Up, System.currentTimeMillis() + 1001);
        boolean result = moveCommand.execute();

        // Debugging the result
        System.out.println("Result of execute(): " + result);

        // Assert
        Assert.assertFalse(result); // Movement should fail due to meadow terrain
        verify(currentField, never()).clearField();
        verify(nextField, never()).setFieldEntity(ship);
    }

}
