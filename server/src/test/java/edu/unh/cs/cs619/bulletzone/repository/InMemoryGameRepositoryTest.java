package edu.unh.cs.cs619.bulletzone.repository;

import org.apache.commons.lang3.tuple.Triple;
import org.javatuples.Triplet;
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

@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class InMemoryGameRepositoryTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();
    @InjectMocks
    private InMemoryGameRepository repo;


    private Game game; // Mock the game context
    private String tankIp;

    @Before
    public void setUp() throws Exception {
        game = mock(Game.class);
        tankIp = "192.168.1.1";
    }

    @Test
    public void testJoin() throws Exception {
        // Execute join operation
        Pair<Tank, Builder> result = repo.join(tankIp);

        // Verify that the result is not null and both Tank and Builder are returned
        Assert.assertNotNull(result);
        Tank tank = result.getValue0();
        Builder builder = result.getValue1();

        Assert.assertNotNull(tank);
        Assert.assertNotNull(builder);

        // Verify initial properties of Tank and Builder
        Assert.assertEquals(tankIp, tank.getIp());
        Assert.assertEquals(Direction.Up, tank.getDirection());
        Assert.assertEquals(Direction.Up, builder.getDirection());
        Assert.assertNotNull(tank.getParent());
        Assert.assertNotNull(builder.getParent());

        // Verify that addTank and addBuilder are called on the mockGame
//        verify(mockGame).addTank(tankIp, tank);
//        verify(mockGame).addBuilder(tankIp, builder);
    }


    @Test
    public void turn_TankTimedInteraction_TurnSucceeds() throws IllegalTransitionException, TankDoesNotExistException, LimitExceededException {
        Tank tank = new Tank(1L, Direction.Up, tankIp);
        tank.setAllowedTurnInterval(0);
        tank.setLastTurnTime(System.currentTimeMillis());

        FieldHolder currentField = mock(FieldHolder.class);
        FieldHolder nextField = mock(FieldHolder.class);

        tank.setParent(currentField);

        when(currentField.getNeighbor(Direction.Left)).thenReturn(nextField);

        Assert.assertEquals(Direction.Up, tank.getDirection());

        // Act
        TurnCommand turnCommand = new TurnCommand(tank, game, Direction.Left, System.currentTimeMillis());
        boolean result = turnCommand.execute();

        // Assert
        Assert.assertTrue(result);

        Assert.assertEquals(Direction.Left, tank.getDirection());

    }

    @Test
    public void turn_BuilderTimedInteraction_TurnSucceeds() throws IllegalTransitionException, TankDoesNotExistException, LimitExceededException {
        Builder builder = new Builder(1L, Direction.Up, tankIp);
        builder.setAllowedTurnInterval(300);
        builder.setLastTurnTime(System.currentTimeMillis());

        FieldHolder currentField = mock(FieldHolder.class);
        FieldHolder nextField = mock(FieldHolder.class);

        builder.setParent(currentField);

        when(currentField.getNeighbor(Direction.Left)).thenReturn(nextField);

        Assert.assertEquals(Direction.Up, builder.getDirection());

        // Act
        TurnCommand turnCommand = new TurnCommand(builder, game, Direction.Left, System.currentTimeMillis());
        boolean result = turnCommand.execute();

        // Assert
        Assert.assertTrue(result);
        Assert.assertEquals(Direction.Left, builder.getDirection());
    }

    @Test
    public void turn_BuilderTimedInteraction_TurnFails() throws IllegalTransitionException, TankDoesNotExistException, LimitExceededException {
        Builder builder = new Builder(1L, Direction.Up, tankIp);
        builder.setAllowedTurnInterval(300);
        builder.setLastTurnTime(System.currentTimeMillis() + 300);

        FieldHolder currentField = mock(FieldHolder.class);
        FieldHolder nextField = mock(FieldHolder.class);

        builder.setParent(currentField);

//        when(currentField.getNeighbor(Direction.Left)).thenReturn(nextField);

        Assert.assertEquals(Direction.Up, builder.getDirection());

        // Act
        TurnCommand turnCommand = new TurnCommand(builder, game, Direction.Left, System.currentTimeMillis());
        boolean result = turnCommand.execute();

        // Assert
        Assert.assertFalse(result);
        Assert.assertEquals(Direction.Up, builder.getDirection());
    }

    @Test
    public void turn_SoldierTimedInteraction_TurnSucceeds() throws IllegalTransitionException, TankDoesNotExistException, LimitExceededException {
        Soldier soldier = new Soldier(1L, Direction.Up, tankIp);
        soldier.setAllowedTurnInterval(0);
        soldier.setLastTurnTime(System.currentTimeMillis());

        FieldHolder currentField = mock(FieldHolder.class);
        FieldHolder nextField = mock(FieldHolder.class);

        soldier.setParent(currentField);

        when(currentField.getNeighbor(Direction.Left)).thenReturn(nextField);

        Assert.assertEquals(Direction.Up, soldier.getDirection());

        // Act
        TurnCommand turnCommand = new TurnCommand(soldier, game, Direction.Left, System.currentTimeMillis());
        boolean result = turnCommand.execute();

        // Assert
        Assert.assertTrue(result);

        Assert.assertEquals(Direction.Left, soldier.getDirection());
    }

    @Test
    public void move_TankTimedInteraction_MoveSucceeds() throws IllegalTransitionException, TankDoesNotExistException, LimitExceededException {
        Tank tank = new Tank(1L, Direction.Up, tankIp);
        tank.setAllowedMoveInterval(500);
        tank.setLastMoveTime(System.currentTimeMillis());

        FieldHolder currentField = mock(FieldHolder.class);
        FieldHolder nextField = mock(FieldHolder.class);

        tank.setParent(currentField);

        when(currentField.getNeighbor(Direction.Down)).thenReturn(nextField);

        Assert.assertEquals(Direction.Up, tank.getDirection());

        // Act
        MoveCommand moveCommand = new MoveCommand(tank, 0, game, Direction.Down, System.currentTimeMillis());
        boolean result = moveCommand.execute();

        // Assert
        Assert.assertTrue(result);
        verify(currentField).clearField();
        verify(nextField).setFieldEntity(tank);

    }

    @Test
    public void move_TankTimedInteraction_MoveFails() throws IllegalTransitionException, TankDoesNotExistException, LimitExceededException {
        Tank tank = new Tank(1L, Direction.Up, tankIp);
        tank.setAllowedMoveInterval(500);
        tank.setLastMoveTime(System.currentTimeMillis() + 500);

        FieldHolder currentField = mock(FieldHolder.class);
        FieldHolder nextField = mock(FieldHolder.class);

        tank.setParent(currentField);

//        when(currentField.getNeighbor(Direction.Down)).thenReturn(nextField);

        Assert.assertEquals(Direction.Up, tank.getDirection());

        // Act
        MoveCommand moveCommand = new MoveCommand(tank, 0, game, Direction.Down, System.currentTimeMillis());
        boolean result = moveCommand.execute();

        // Assert
        Assert.assertFalse(result);
        verify(currentField, never()).clearField();
        verify(nextField, never()).setFieldEntity(tank);

    }

    @Test
    public void move_BuilderTimedInteraction_MoveSucceeds() throws IllegalTransitionException, TankDoesNotExistException, LimitExceededException {
        Builder builder = new Builder(1L, Direction.Up, tankIp);
        builder.setAllowedMoveInterval(1000);
        builder.setLastMoveTime(System.currentTimeMillis());

        FieldHolder currentField = mock(FieldHolder.class);
        FieldHolder nextField = mock(FieldHolder.class);

        builder.setParent(currentField);

        when(currentField.getNeighbor(Direction.Down)).thenReturn(nextField);

        Assert.assertEquals(Direction.Up, builder.getDirection());

        // Act
        MoveCommand moveCommand = new MoveCommand(builder, 1, game, Direction.Down, System.currentTimeMillis());
        boolean result = moveCommand.execute();

        // Assert
        Assert.assertTrue(result);
        verify(currentField).clearField();
        verify(nextField).setFieldEntity(builder);

    }

    @Test
    public void move_BuilderTimedInteraction_MoveFails() throws IllegalTransitionException, TankDoesNotExistException, LimitExceededException {
        Builder builder = new Builder(1L, Direction.Up, tankIp);
        builder.setAllowedMoveInterval(1000);
        builder.setLastMoveTime(System.currentTimeMillis() + 1000);

        FieldHolder currentField = mock(FieldHolder.class);
        FieldHolder nextField = mock(FieldHolder.class);

        builder.setParent(currentField);


        Assert.assertEquals(Direction.Up, builder.getDirection());

        // Act
        MoveCommand moveCommand = new MoveCommand(builder, 1, game, Direction.Down, System.currentTimeMillis());
        boolean result = moveCommand.execute();

        // Assert
        Assert.assertFalse(result);
        verify(currentField, never()).clearField();
        verify(nextField, never()).setFieldEntity(builder);

    }

    @Test
    public void move_SoldierTimedInteraction_MoveSucceeds() throws IllegalTransitionException, TankDoesNotExistException, LimitExceededException {
        Soldier soldier = new Soldier(1L, Direction.Up, tankIp);
        soldier.setAllowedMoveInterval(1000);
        soldier.setLastMoveTime(System.currentTimeMillis());

        FieldHolder currentField = mock(FieldHolder.class);
        FieldHolder nextField = mock(FieldHolder.class);

        soldier.setParent(currentField);

        when(currentField.getNeighbor(Direction.Down)).thenReturn(nextField);

        Assert.assertEquals(Direction.Up, soldier.getDirection());

        // Act
        MoveCommand moveCommand = new MoveCommand(soldier, 2, game, Direction.Down, System.currentTimeMillis());
        boolean result = moveCommand.execute();

        // Assert
        Assert.assertTrue(result);
        verify(currentField).clearField();
        verify(nextField).setFieldEntity(soldier);

    }

    @Test
    public void move_SoldierTimedInteraction_MoveFails() throws IllegalTransitionException, TankDoesNotExistException, LimitExceededException {
        Soldier soldier = new Soldier(1L, Direction.Up, tankIp);
        soldier.setAllowedMoveInterval(1000);
        soldier.setLastMoveTime(System.currentTimeMillis() + 1000);

        FieldHolder currentField = mock(FieldHolder.class);
        FieldHolder nextField = mock(FieldHolder.class);

        soldier.setParent(currentField);

        Assert.assertEquals(Direction.Up, soldier.getDirection());

        // Act
        MoveCommand moveCommand = new MoveCommand(soldier, 2, game, Direction.Down, System.currentTimeMillis());
        boolean result = moveCommand.execute();

        // Assert
        Assert.assertFalse(result);
        verify(currentField, never()).clearField();
        verify(nextField, never()).setFieldEntity(soldier);

    }

    @Test
    public void fire_TankTimedInteraction_FireSucceeds() throws IllegalTransitionException, TankDoesNotExistException, LimitExceededException {
        Tank tank = new Tank(1L, Direction.Up, tankIp);
        tank.setAllowedFireInterval(1500);
        tank.setLastFireTime(System.currentTimeMillis());

        FieldHolder currentField = mock(FieldHolder.class);
        FieldHolder nextField = mock(FieldHolder.class);

        tank.setParent(currentField);

        Assert.assertEquals(Direction.Up, tank.getDirection());

        // Act
        FireCommand fireCommand = new FireCommand();
        boolean result1 = fireCommand.canFire(tank, System.currentTimeMillis(), 2);

        // Assert
        Assert.assertTrue(result1);
    }

    @Test
    public void fire_TankTimedInteraction_FireFails() throws IllegalTransitionException, TankDoesNotExistException, LimitExceededException {
        Tank tank = new Tank(1L, Direction.Up, tankIp);
        tank.setAllowedFireInterval(1500);
        tank.setLastFireTime(System.currentTimeMillis() + 1500);

        FieldHolder currentField = mock(FieldHolder.class);

        tank.setParent(currentField);

        Assert.assertEquals(Direction.Up, tank.getDirection());

        // Act
        FireCommand fireCommand = new FireCommand();
        boolean result = fireCommand.canFire(tank, System.currentTimeMillis(), 2);

        // Assert
        Assert.assertFalse(result);
    }

    @Test
    public void fire_BuilderTimedInteraction_FireSucceeds() throws IllegalTransitionException, TankDoesNotExistException, LimitExceededException {
        Builder builder = new Builder(1L, Direction.Up, tankIp);
        builder.setAllowedFireInterval(1000);
        builder.setLastFireTime(System.currentTimeMillis());

        FieldHolder currentField = mock(FieldHolder.class);

        builder.setParent(currentField);


        Assert.assertEquals(Direction.Up, builder.getDirection());

        // Act
        FireCommand fireCommand = new FireCommand();
        boolean result = fireCommand.canFire(builder, System.currentTimeMillis(), 1);

        // Assert
        Assert.assertTrue(result);
    }

    @Test
    public void fire_BuilderTimedInteraction_FireFails() throws IllegalTransitionException, TankDoesNotExistException, LimitExceededException {
        Builder builder = new Builder(1L, Direction.Up, tankIp);
        builder.setAllowedFireInterval(1000);
        builder.setLastFireTime(System.currentTimeMillis() + 1000);

        FieldHolder currentField = mock(FieldHolder.class);

        builder.setParent(currentField);

        Assert.assertEquals(Direction.Up, builder.getDirection());

        // Act
        FireCommand fireCommand = new FireCommand();
        boolean result = fireCommand.canFire(builder, System.currentTimeMillis(), 1);

        // Assert
        Assert.assertFalse(result);
    }

    @Test
    public void fire_SoldierTimedInteraction_FireSucceeds() throws IllegalTransitionException, TankDoesNotExistException, LimitExceededException {
        Soldier soldier = new Soldier(1L, Direction.Up, tankIp);
        soldier.setAllowedFireInterval(250);
        soldier.setLastFireTime(System.currentTimeMillis());

        FieldHolder currentField = mock(FieldHolder.class);

        soldier.setParent(currentField);

        Assert.assertEquals(Direction.Up, soldier.getDirection());

        // Act
        FireCommand fireCommand = new FireCommand();
        boolean result = fireCommand.canFire(soldier, System.currentTimeMillis(), 0);

        // Assert
        Assert.assertTrue(result);
    }

    @Test
    public void fire_SoldierTimedInteraction_FireFails() throws IllegalTransitionException, TankDoesNotExistException, LimitExceededException {
        Soldier soldier = new Soldier(1L, Direction.Up, tankIp);
        soldier.setAllowedFireInterval(250);
        soldier.setLastFireTime(System.currentTimeMillis() + 250);

        FieldHolder currentField = mock(FieldHolder.class);

        soldier.setParent(currentField);

        Assert.assertEquals(Direction.Up, soldier.getDirection());

        // Act
        FireCommand fireCommand = new FireCommand();
        boolean result = fireCommand.canFire(soldier, System.currentTimeMillis(), 0);

        // Assert
        Assert.assertFalse(result);
    }
/*
    @Test
    public void turn_VehicleFacingUpTurnLeft_TurnSucceeds() throws IllegalTransitionException, TankDoesNotExistException, LimitExceededException {
        Tank tank = repo.join("turningVehicle");

        Assert.assertEquals(Direction.Up, tank.getDirection());

        repo.turn(tank.getId(), Direction.fromByte((byte) 6));

        Assert.assertEquals(Direction.Left, tank.getDirection());

        thrown.expect(TankDoesNotExistException.class);
        thrown.expectMessage("Tank '1000' does not exist");
        repo.turn(1000, Direction.Left);
    }

    @Test
    public void turn_VehicleFacingDownTurnLeft_TurnSucceeds() throws IllegalTransitionException, LimitExceededException, TankDoesNotExistException {
        Tank tank = repo.join("turningVehicle");

        Assert.assertEquals(Direction.Up, tank.getDirection());
        tank.setDirection(Direction.Down);
        Assert.assertEquals(Direction.Down, tank.getDirection());

        repo.turn(tank.getId(), Direction.fromByte((byte) 6));
        Assert.assertEquals(Direction.Left, tank.getDirection());

        thrown.expect(TankDoesNotExistException.class);
        thrown.expectMessage("Tank '1000' does not exist");
        repo.turn(1000, Direction.Up);
    }

    @Test
    public void turn_VehicleFacingDownTurnRight_TurnSucceeds() throws IllegalTransitionException, LimitExceededException, TankDoesNotExistException {
        Tank tank = repo.join("turningVehicle");
        Assert.assertEquals(Direction.Up, tank.getDirection());
        tank.setDirection(Direction.Down);
        Assert.assertEquals(Direction.Down, tank.getDirection());

        repo.turn(tank.getId(), Direction.fromByte((byte) 2));
        Assert.assertEquals(Direction.Right, tank.getDirection());

        thrown.expect(TankDoesNotExistException.class);
        thrown.expectMessage("Tank '1000' does not exist");
        repo.turn(1000, Direction.Up);
    }

    @Test
    public void turn_VehicleCanTurnAfterTimePasses_TurnFails() throws TankDoesNotExistException, IllegalTransitionException, LimitExceededException {
        // Mock the current time to simulate time-dependent behavior
        Tank tank = repo.join("turningVehicle");
        tank.setLastMoveTime(System.currentTimeMillis());
        // First turn attempt without enough time passing (simulate last move time)
        Assert.assertEquals(Direction.Up, tank.getDirection());
        tank.setDirection(Direction.Down);
        Assert.assertEquals(Direction.Down, tank.getDirection());
        constraints.canTurn(tank.getId(), mockGame, Direction.fromByte((byte) 2), mockMillis + tank.getLastMoveTime());

        boolean turnAttempt1 = repo.turn(tank.getId(), Direction.fromByte((byte) 2));
        Assert.assertTrue("First turn", turnAttempt1);
        // Simulate time passage - advance the mock time by 500 milliseconds
        Assert.assertEquals(Direction.Right, tank.getDirection());
        // Second turn attempt after enough time has passed
        constraints.canTurn(tank.getId(), mockGame, Direction.fromByte((byte) 0), mockMillis + tank.getLastMoveTime());

        boolean turnAttempt2 = repo.turn(tank.getId(), Direction.fromByte((byte) 0));
        Assert.assertTrue("Tank should be able to turn after enough time has passed", turnAttempt2);

        // Verify that the direction has changed to Left
        Assert.assertEquals(Direction.Up, tank.getDirection());
    }


    @Test
    public void move_VehicleFacingUpMoveForward_MoveSucceeds() throws IllegalTransitionException, TankDoesNotExistException, LimitExceededException {
        Tank tank = repo.join("movingVehicle");

        int tankPos = tank.getParent().getPosition();

        tank.setDirection(Direction.Up);
        Assert.assertEquals(Direction.Up, tank.getDirection());

        boolean move = repo.move(tank.getId(), Direction.fromByte((byte)0));
        Assert.assertTrue("Backward movement should succeed", move);

        Assert.assertNotEquals(tankPos, tank.getParent().getPosition());
        Assert.assertEquals(Direction.Up, tank.getDirection());
    }

    @Test
    public void move_VehicleFacingDownMoveForward_MoveSucceeds() throws TankDoesNotExistException, IllegalTransitionException, LimitExceededException {
        Tank tank = repo.join("movingVehicle");

        int tankPos = tank.getParent().getPosition();

        tank.setDirection(Direction.Down);
        Assert.assertEquals(Direction.Down, tank.getDirection());

        boolean move = repo.move(tank.getId(), Direction.fromByte((byte)4));
        Assert.assertTrue("Forward movement should succeed", move);

        Assert.assertNotEquals(tankPos, tank.getParent().getPosition());
        Assert.assertEquals(Direction.Down, tank.getDirection());
    }

    @Test
    public void move_VehicleFacingRightMoveForward_MoveSucceeds() throws TankDoesNotExistException, IllegalTransitionException, LimitExceededException {
        Tank tank = repo.join("movingVehicle");

        int tankPos = tank.getParent().getPosition();

        tank.setDirection(Direction.Right);
        Assert.assertEquals(Direction.Right, tank.getDirection());

        boolean move = repo.move(tank.getId(), Direction.fromByte((byte)2));
        Assert.assertTrue("Forward movement should succeed", move);

        Assert.assertNotEquals(tankPos, tank.getParent().getPosition());
        Assert.assertEquals(Direction.Right, tank.getDirection());
    }

    @Test
    public void move_VehicleFacingDownMoveBackwards_MoveSucceeds() throws TankDoesNotExistException, IllegalTransitionException, LimitExceededException {
        Tank tank = repo.join("movingVehicle");

        int tankPos = tank.getParent().getPosition();

        tank.setDirection(Direction.Down);
        Assert.assertEquals(Direction.Down, tank.getDirection());

        boolean move = repo.move(tank.getId(), Direction.fromByte((byte)0));
        Assert.assertTrue("Backward movement should succeed", move);

        Assert.assertNotEquals(tankPos, tank.getParent().getPosition());
        Assert.assertEquals(Direction.Up, tank.getDirection());
    }

    @Test
    public void move_VehicleFacingRightMoveBackward_MoveSucceeds() throws TankDoesNotExistException, IllegalTransitionException, LimitExceededException {
        Tank tank = repo.join("movingVehicle");

        assert tank != null;
        int tankPos = tank.getParent().getPosition();

        tank.setDirection(Direction.Right);
        Assert.assertEquals(Direction.Right, tank.getDirection());

        // Moving backward from Right facing direction (byte 6 is Left direction)
        boolean move = repo.move(tank.getId(), Direction.fromByte((byte) 6));
        Assert.assertTrue("Backward movement should succeed", move);

        // Position should have changed
        Assert.assertNotEquals(tankPos, tank.getParent().getPosition());

        // When moving backward into a new direction, tank faces that direction
        // Moving backward from Right means we're going Left, so tank should face Left
        Assert.assertEquals(Direction.Left, tank.getDirection());
    }

    @Test
    public void move_VehicleFacingLeftMoveForward_MoveSucceeds() throws TankDoesNotExistException, IllegalTransitionException, LimitExceededException {
        Tank tank = repo.join("movingVehicle");

        int tankPos = tank.getParent().getPosition();

        tank.setDirection(Direction.Left);
        Assert.assertEquals(Direction.Left, tank.getDirection());

        boolean move = repo.move(tank.getId(), Direction.fromByte((byte) 6));
        Assert.assertTrue("Forward movement should succeed", move);

        Assert.assertNotEquals(tankPos, tank.getParent().getPosition());
        Assert.assertEquals(Direction.Left, tank.getDirection());
    }

    @Test
    public void move_VehicleFacingLeftMoveBackward_MoveSucceeds() throws TankDoesNotExistException, IllegalTransitionException, LimitExceededException {
        Tank tank = repo.join("movingVehicle");

        int tankPos = tank.getParent().getPosition();

        tank.setDirection(Direction.Left);
        Assert.assertEquals(Direction.Left, tank.getDirection());

        boolean move = repo.move(tank.getId(), Direction.fromByte((byte) 2));
        Assert.assertTrue("Backward movement should succeed", move);

        Assert.assertNotEquals(tankPos, tank.getParent().getPosition());
        Assert.assertEquals(Direction.Right, tank.getDirection());
    }

    @Test
    public void move_VehicleCanMoveAfterTimePasses_MoveSuccess() throws TankDoesNotExistException, IllegalTransitionException, LimitExceededException {
        // Mock the current time to simulate time-dependent behavior
        Tank tank = repo.join("movingVehicle");

        // Set initial state
        tank.setLastMoveTime(System.currentTimeMillis() - 1000); // Ensure enough time has passed
        tank.setDirection(Direction.Up);
        Assert.assertEquals(Direction.Up, tank.getDirection());

        // First move attempt
        FieldHolder currentPos = tank.getParent();
        boolean moveAttempt1 = repo.move(tank.getId(), Direction.Up);
        Assert.assertTrue("First move should succeed", moveAttempt1);
        Assert.assertNotEquals("Tank position should change", currentPos, tank.getParent());

        // Set up for second move
        tank.setLastMoveTime(System.currentTimeMillis() - 1000); // Ensure enough time has passed
        currentPos = tank.getParent();

        // Second move attempt
        boolean moveAttempt2 = repo.move(tank.getId(), Direction.Down);
        Assert.assertTrue("Tank should be able to move after enough time has passed", moveAttempt2);
        Assert.assertNotEquals("Tank position should change", currentPos, tank.getParent());
    }

    @Test
    public void testFire() throws Exception {

    }

    @Test
    public void fire_VehicleCallsFire_SendsBullet() throws LimitExceededException, TankDoesNotExistException {
        Tank tank = repo.join("movingVehicle");

        if (repo.fire(tank.getId(), 1)) {
            Assert.assertTrue(true);
        }

        Assert.assertEquals(Direction.Up, tank.getDirection());
    }

    @Test
    public void fire_VehicleCanFireAfterTimePasses_FireSuccess() throws TankDoesNotExistException {
        // Mock the current time to simulate time-dependent behavior
        Tank tank = repo.join("turningVehicle");
        tank.setLastFireTime(System.currentTimeMillis());
        // First turn attempt without enough time passing (simulate last move time)
        Assert.assertEquals(Direction.Up, tank.getDirection());
        tank.setDirection(Direction.Up);
        Assert.assertEquals(Direction.Up, tank.getDirection());
        constraints.canFire(tank, mockMillis + tank.getLastFireTime(), 1, bulletDelay);

        boolean fireAttempt1 = repo.fire(tank.getId(), 1);
        Assert.assertTrue("Tank should be able to turn", fireAttempt1);
        // Simulate time passage - advance the mock time by 500 milliseconds
        Assert.assertEquals(Direction.Up, tank.getDirection());
        // Second turn attempt after enough time has passed
        constraints.canFire(tank, mockMillis + tank.getLastFireTime(), 1, bulletDelay);

        boolean fireAttempt2 = repo.fire(tank.getId(), 1);
        Assert.assertFalse("Tank should be able to turn after enough time has passed", fireAttempt2);

        // Verify that the direction has changed to Left
//        Assert.assertEquals(Direction.Down, tank.getDirection());
    }

    @Test
    public void testLeave() throws Exception {

    }*/
}
