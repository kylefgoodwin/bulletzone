//package edu.unh.cs.cs619.bulletzone.repository;
//
//import org.junit.Assert;
//import org.junit.Before;
//import org.junit.Rule;
//import org.junit.Test;
//import org.junit.rules.ExpectedException;
//import org.junit.runner.RunWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.MockitoJUnitRunner;
//import org.mockito.Mockito;
//
//import java.util.ArrayList;
//import java.util.Collections;
//import java.util.List;
//import java.util.Timer;
//import java.util.TimerTask;
//import java.util.HashMap;
//import java.util.Map;
//import org.javatuples.Pair;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.Mockito.*;
//
//
//import edu.unh.cs.cs619.bulletzone.model.Builder;
//import edu.unh.cs.cs619.bulletzone.model.Bullet;
//import edu.unh.cs.cs619.bulletzone.model.Direction;
//import edu.unh.cs.cs619.bulletzone.model.FieldHolder;
//import edu.unh.cs.cs619.bulletzone.model.IllegalTransitionException;
//import edu.unh.cs.cs619.bulletzone.model.LimitExceededException;
//import edu.unh.cs.cs619.bulletzone.model.Playable;
//import edu.unh.cs.cs619.bulletzone.model.Tank;
//import edu.unh.cs.cs619.bulletzone.model.Game;
//import edu.unh.cs.cs619.bulletzone.model.TankDoesNotExistException;
//
//@RunWith(MockitoJUnitRunner.StrictStubs.class)
//public class InMemoryGameRepositoryTest {
//
//    @Rule
//    public ExpectedException thrown = ExpectedException.none();
//    @InjectMocks
//    private InMemoryGameRepository repo;
//    @Mock
//    private MoveCommand moveCommand;
//
//    @Mock
//    private TurnCommand turnCommand;
//
//    @Mock
//    private BuildCommand buildCommand;
//
//    @Mock
//    private FireCommand fireCommand;
//
//    @Mock
//    private DeployCommand deployCommand;
//
//    @Mock
//    private Game mockGame;
//    @Mock
//    private Playable mockPlayable;
//
//    @Mock
//    private Tank mockTank;
//
//    @Mock
//    private Builder mockBuilder;
//
//    @Mock
//    private FieldHolder mockFieldHolder;
//
//    private final long tankId = 1L;
//    private final String tankIp = "192.168.1.1";
//
//    @Before
//    public void setUp() throws Exception {
//        repo = new InMemoryGameRepository(fireCommand, new GameBoardBuilder());
//        mockGame = mock(Game.class);
//
//        // Set up the mock game grid to simulate empty positions
//        ArrayList<FieldHolder> mockHolderGrid = new ArrayList<>(Collections.nCopies(16 * 16, mock(FieldHolder.class)));
//        when(mockGame.getHolderGrid()).thenReturn(mockHolderGrid);
//
//        // Set up mocks for Game methods
//        when(mockGame.getTank(tankIp)).thenReturn(null);
//        when(mockGame.getBuilder(tankIp)).thenReturn(null);
//
//        // Mock behavior of `isPresent` to indicate empty field holders for placement
//        when(mockFieldHolder.isPresent()).thenReturn(false);
//
//        repo.create(); // Initialize the game inside repository
//    }
//
//    @Test
//    public void testJoin() throws Exception {
//        // Execute join operation
//        Pair<Tank, Builder> result = repo.join(tankIp);
//
//        // Verify that the result is not null and both Tank and Builder are returned
//        Assert.assertNotNull(result);
//        Tank tank = result.getValue0();
//        Builder builder = result.getValue1();
//
//        Assert.assertNotNull(tank);
//        Assert.assertNotNull(builder);
//
//        // Verify initial properties of Tank and Builder
//        Assert.assertEquals(tankIp, tank.getIp());
//        Assert.assertEquals(Direction.Up, tank.getDirection());
//        Assert.assertEquals(Direction.Up, builder.getDirection());
//        Assert.assertNotNull(tank.getParent());
//        Assert.assertNotNull(builder.getParent());
//
//        // Verify that addTank and addBuilder are called on the mockGame
//        verify(mockGame).addTank(tankIp, tank);
//        verify(mockGame).addBuilder(tankIp, builder);
//    }
//
//
//    @Test
//    public void turn_TankTimedInteraction_TurnSucceeds() throws IllegalTransitionException, TankDoesNotExistException, LimitExceededException {
//        Pair<Tank, Builder> result = repo.join(tankIp);
//        mockPlayable = result.getValue0();
//        mockPlayable.setAllowedMoveInterval(0);
//        mockPlayable.setLastMoveTime(System.currentTimeMillis());
//
//        Assert.assertEquals(Direction.Up, mockPlayable.getDirection());
//
//        boolean turned = repo.turn(mockPlayable.getId(), 1, Direction.fromByte((byte) 2));
//        Assert.assertEquals(Direction.Right, mockPlayable.getDirection());
//
//        assertTrue(turned, "Tank should be able to turn immediately");
//    }
//
//    @Test
//    public void turn_BuilderTimedInteraction_TurnSucceeds() throws IllegalTransitionException, TankDoesNotExistException, LimitExceededException {
//        Pair<Tank, Builder> result = repo.join(tankIp);
//        mockPlayable = result.getValue1();
//        mockPlayable.setAllowedTurnInterval(300);
//        mockPlayable.setLastTurnTime(System.currentTimeMillis());
//
//        Assert.assertEquals(Direction.Up, mockPlayable.getDirection());
//
//        boolean turned = repo.turn(mockPlayable.getId(), 2, Direction.fromByte((byte) 2));
//        Assert.assertEquals(Direction.Right, mockPlayable.getDirection());
//
//        assertTrue(turned, "Tank should be able to turn immediately");
//    }
//
//    @Test
//    public void turn_BuilderTimedInteraction_TurnFails() throws IllegalTransitionException, TankDoesNotExistException, LimitExceededException {
//        Pair<Tank, Builder> result = repo.join(tankIp);
//        mockPlayable = result.getValue1();
//        mockPlayable.setAllowedTurnInterval(300);
//        mockPlayable.setLastTurnTime(System.currentTimeMillis() + 300);
//
//        Assert.assertEquals(Direction.Up, mockPlayable.getDirection());
//
//        boolean turned = repo.turn(mockPlayable.getId(), 2, Direction.fromByte((byte) 2));
//
//        assertFalse(turned, "Tank should be able to turn immediately");
//    }
//
//    @Test
//    public void move_TankTimedInteraction_MoveSucceeds() throws IllegalTransitionException, TankDoesNotExistException, LimitExceededException {
//        Pair<Tank, Builder> result = repo.join(tankIp);
//        mockPlayable = result.getValue0();
//        mockPlayable.setAllowedMoveInterval(500);
//        mockPlayable.setLastMoveTime(System.currentTimeMillis());
//
//        Assert.assertEquals(Direction.Up, mockPlayable.getDirection());
//
//        boolean moved = repo.move(mockPlayable.getId(), 1, Direction.fromByte((byte) 4));
//        Assert.assertEquals(Direction.Down, mockPlayable.getDirection());
//
//        assertTrue(moved, "Tank should be able to turn immediately");
//    }
//
//    @Test
//    public void move_TankTimedInteraction_MoveFails() throws IllegalTransitionException, TankDoesNotExistException, LimitExceededException {
//        Pair<Tank, Builder> result = repo.join(tankIp);
//        mockPlayable = result.getValue0();
//        mockPlayable.setAllowedMoveInterval(500);
//        mockPlayable.setLastMoveTime(System.currentTimeMillis() + 500);
//
//        Assert.assertEquals(Direction.Up, mockPlayable.getDirection());
//
//        boolean moved = repo.move(mockPlayable.getId(), 1, Direction.fromByte((byte) 4));
//
//        assertFalse(moved, "Tank should be able to turn immediately");
//    }
//
//    @Test
//    public void move_BuilderTimedInteraction_MoveSucceeds() throws IllegalTransitionException, TankDoesNotExistException, LimitExceededException {
//        Pair<Tank, Builder> result = repo.join(tankIp);
//        mockPlayable = result.getValue1();
//        mockPlayable.setAllowedMoveInterval(1000);
//        mockPlayable.setLastMoveTime(System.currentTimeMillis());
//
//        Assert.assertEquals(Direction.Up, mockPlayable.getDirection());
//
//        boolean moved = repo.move(mockPlayable.getId(), 2, Direction.fromByte((byte) 4));
//        Assert.assertEquals(Direction.Down, mockPlayable.getDirection());
//
//        assertTrue(moved, "Tank should be able to turn immediately");
//    }
//
//    @Test
//    public void move_BuilderTimedInteraction_MoveFails() throws IllegalTransitionException, TankDoesNotExistException, LimitExceededException {
//        Tank result = repo.join(tankIp);
//        mockPlayable = mockBuilder;
//        Builder builder = mockGame.getBuilders().get(tankId);
//        mockPlayable.setAllowedMoveInterval(1000);
//        mockPlayable.setLastMoveTime(System.currentTimeMillis() + 1000);
//
//        Assert.assertEquals(Direction.Up, mockPlayable.getDirection());
//
//        boolean moved = repo.move(builder, 2, Direction.fromByte((byte) 4));
//        Assert.assertEquals(Direction.Down, mockPlayable.getDirection());
//
//        assertFalse(moved, "Tank should be able to turn immediately");
//    }
//
//    @Test
//    public void move_SoldierTimedInteraction_MoveSucceeds() throws IllegalTransitionException, TankDoesNotExistException, LimitExceededException {
//        Pair<Tank, Builder> result = repo.join(tankIp);
//        mockPlayable = result.getValue0();
//        boolean deployed = repo.deploy(mockPlayable.getId(), 1, Direction.fromByte((byte) 0));
//        Assert.assertEquals(Direction.Up, mockPlayable.getDirection());
//        Assert.assertEquals(mockPlayable.getPlayableType(), 3);
//
//        assertTrue(deployed, "Tank should be able to turn immediately");
//        mockPlayable.setAllowedMoveInterval(1000);
//        mockPlayable.setLastMoveTime(System.currentTimeMillis());
//
//        Assert.assertEquals(Direction.Up, mockPlayable.getDirection());
//
//        boolean moved = repo.move(mockPlayable.getId(), 3, Direction.fromByte((byte) 4));
//        Assert.assertEquals(Direction.Down, mockPlayable.getDirection());
//
//        assertTrue(moved, "Tank should be able to turn immediately");
//    }
//
//    @Test
//    public void move_SoldierTimedInteraction_MoveFails() throws IllegalTransitionException, TankDoesNotExistException, LimitExceededException {
//        Pair<Tank, Builder> result = repo.join(tankIp);
//        mockPlayable = result.getValue0();
//        boolean deployed = repo.deploy(mockPlayable.getId(), 1, Direction.fromByte((byte) 0));
//        Assert.assertEquals(Direction.Up, mockPlayable.getDirection());
//        Assert.assertEquals(mockPlayable.getPlayableType(), 3);
//
//        assertTrue(deployed, "Tank should be able to turn immediately");
//        mockPlayable.setAllowedMoveInterval(1000);
//        mockPlayable.setLastMoveTime(System.currentTimeMillis() + 1000);
//
//        Assert.assertEquals(Direction.Up, mockPlayable.getDirection());
//
//        boolean moved = repo.move(mockPlayable.getId(), 3, Direction.fromByte((byte) 4));
//        Assert.assertEquals(Direction.Down, mockPlayable.getDirection());
//
//        assertFalse(moved, "Tank should be able to turn immediately");
//    }
//
//    @Test
//    public void fire_TankTimedInteraction_FireSucceeds() throws IllegalTransitionException, TankDoesNotExistException, LimitExceededException {
//        Pair<Tank, Builder> result = repo.join(tankIp);
//        mockPlayable = result.getValue0();
//        mockPlayable.setAllowedFireInterval(1500);
//        mockPlayable.setLastFireTime(System.currentTimeMillis());
//
//        Assert.assertEquals(Direction.Up, mockPlayable.getDirection());
//
//        boolean fired = repo.fire(mockPlayable.getId(), 1, 2);
//
//        assertTrue(fired, "Tank should be able to fire immediately");
//    }
//
//    @Test
//    public void fire_TankTimedInteraction_FireFails() throws IllegalTransitionException, TankDoesNotExistException, LimitExceededException {
//        Pair<Tank, Builder> result = repo.join(tankIp);
//        mockPlayable = result.getValue0();
//        mockPlayable.setAllowedFireInterval(1500);
//        mockPlayable.setLastFireTime(System.currentTimeMillis() + 1500);
//
//        Assert.assertEquals(Direction.Up, mockPlayable.getDirection());
//
//        boolean fired = repo.fire(mockPlayable.getId(), 1, 2);
//
//        assertFalse(fired, "Tank should be able to fire immediately");
//    }
//
//    @Test
//    public void fire_BuilderTimedInteraction_FireSucceeds() throws IllegalTransitionException, TankDoesNotExistException, LimitExceededException {
//        Pair<Tank, Builder> result = repo.join(tankIp);
//        mockPlayable = result.getValue1();
//        mockPlayable.setAllowedFireInterval(1000);
//        mockPlayable.setLastFireTime(System.currentTimeMillis());
//
//        Assert.assertEquals(Direction.Up, mockPlayable.getDirection());
//
//        boolean fired = repo.fire(mockPlayable.getId(), 2, 1);
//
//        assertTrue(fired, "Tank should be able to fire immediately");
//    }
//
//    @Test
//    public void fire_BuilderTimedInteraction_FireFails() throws IllegalTransitionException, TankDoesNotExistException, LimitExceededException {
//        Pair<Tank, Builder> result = repo.join(tankIp);
//        mockPlayable = result.getValue1();
//        mockPlayable.setAllowedFireInterval(1000);
//        mockPlayable.setLastFireTime(System.currentTimeMillis() + 1000);
//
//        Assert.assertEquals(Direction.Up, mockPlayable.getDirection());
//
//        boolean fired = repo.fire(mockPlayable.getId(), 2, 1);
//
//        assertFalse(fired, "Tank should be able to fire immediately");
//    }
//
//    @Test
//    public void fire_SoldierTimedInteraction_FireSucceeds() throws IllegalTransitionException, TankDoesNotExistException, LimitExceededException {
//        Pair<Tank, Builder> result = repo.join(tankIp);
//        mockPlayable = result.getValue0();
//        boolean deployed = repo.deploy(mockPlayable.getId(), 1, Direction.fromByte((byte) 0));
//        Assert.assertEquals(Direction.Up, mockPlayable.getDirection());
//        Assert.assertEquals(mockPlayable.getPlayableType(), 3);
//
//        assertTrue(deployed, "Tank should be able to turn immediately");
//        mockPlayable.setAllowedFireInterval(250);
//        mockPlayable.setLastFireTime(System.currentTimeMillis());
//
//        Assert.assertEquals(Direction.Up, mockPlayable.getDirection());
//
//        boolean fired = repo.fire(mockPlayable.getId(), 3, 0);
//        Assert.assertEquals(Direction.Down, mockPlayable.getDirection());
//
//        assertTrue(fired, "Tank should be able to turn immediately");
//    }
//
//    @Test
//    public void fire_SoldierTimedInteraction_FireFails() throws IllegalTransitionException, TankDoesNotExistException, LimitExceededException {
//        Pair<Tank, Builder> result = repo.join(tankIp);
//        mockPlayable = result.getValue0();
//        boolean deployed = repo.deploy(mockPlayable.getId(), 1, Direction.fromByte((byte) 0));
//        Assert.assertEquals(Direction.Up, mockPlayable.getDirection());
//        Assert.assertEquals(mockPlayable.getPlayableType(), 3);
//
//        assertTrue(deployed, "Tank should be able to turn immediately");
//        mockPlayable.setAllowedFireInterval(250);
//        mockPlayable.setLastFireTime(System.currentTimeMillis() + 250);
//
//        Assert.assertEquals(Direction.Up, mockPlayable.getDirection());
//
//        boolean fired = repo.fire(mockPlayable.getId(), 3, 0);
//        Assert.assertEquals(Direction.Down, mockPlayable.getDirection());
//
//        assertFalse(fired, "Tank should be able to turn immediately");
//    }
///*
//    @Test
//    public void turn_VehicleFacingUpTurnLeft_TurnSucceeds() throws IllegalTransitionException, TankDoesNotExistException, LimitExceededException {
//        Tank tank = repo.join("turningVehicle");
//
//        Assert.assertEquals(Direction.Up, tank.getDirection());
//
//        repo.turn(tank.getId(), Direction.fromByte((byte) 6));
//
//        Assert.assertEquals(Direction.Left, tank.getDirection());
//
//        thrown.expect(TankDoesNotExistException.class);
//        thrown.expectMessage("Tank '1000' does not exist");
//        repo.turn(1000, Direction.Left);
//    }
//
//    @Test
//    public void turn_VehicleFacingDownTurnLeft_TurnSucceeds() throws IllegalTransitionException, LimitExceededException, TankDoesNotExistException {
//        Tank tank = repo.join("turningVehicle");
//
//        Assert.assertEquals(Direction.Up, tank.getDirection());
//        tank.setDirection(Direction.Down);
//        Assert.assertEquals(Direction.Down, tank.getDirection());
//
//        repo.turn(tank.getId(), Direction.fromByte((byte) 6));
//        Assert.assertEquals(Direction.Left, tank.getDirection());
//
//        thrown.expect(TankDoesNotExistException.class);
//        thrown.expectMessage("Tank '1000' does not exist");
//        repo.turn(1000, Direction.Up);
//    }
//
//    @Test
//    public void turn_VehicleFacingDownTurnRight_TurnSucceeds() throws IllegalTransitionException, LimitExceededException, TankDoesNotExistException {
//        Tank tank = repo.join("turningVehicle");
//        Assert.assertEquals(Direction.Up, tank.getDirection());
//        tank.setDirection(Direction.Down);
//        Assert.assertEquals(Direction.Down, tank.getDirection());
//
//        repo.turn(tank.getId(), Direction.fromByte((byte) 2));
//        Assert.assertEquals(Direction.Right, tank.getDirection());
//
//        thrown.expect(TankDoesNotExistException.class);
//        thrown.expectMessage("Tank '1000' does not exist");
//        repo.turn(1000, Direction.Up);
//    }
//
//    @Test
//    public void turn_VehicleCanTurnAfterTimePasses_TurnFails() throws TankDoesNotExistException, IllegalTransitionException, LimitExceededException {
//        // Mock the current time to simulate time-dependent behavior
//        Tank tank = repo.join("turningVehicle");
//        tank.setLastMoveTime(System.currentTimeMillis());
//        // First turn attempt without enough time passing (simulate last move time)
//        Assert.assertEquals(Direction.Up, tank.getDirection());
//        tank.setDirection(Direction.Down);
//        Assert.assertEquals(Direction.Down, tank.getDirection());
//        constraints.canTurn(tank.getId(), mockGame, Direction.fromByte((byte) 2), mockMillis + tank.getLastMoveTime());
//
//        boolean turnAttempt1 = repo.turn(tank.getId(), Direction.fromByte((byte) 2));
//        Assert.assertTrue("First turn", turnAttempt1);
//        // Simulate time passage - advance the mock time by 500 milliseconds
//        Assert.assertEquals(Direction.Right, tank.getDirection());
//        // Second turn attempt after enough time has passed
//        constraints.canTurn(tank.getId(), mockGame, Direction.fromByte((byte) 0), mockMillis + tank.getLastMoveTime());
//
//        boolean turnAttempt2 = repo.turn(tank.getId(), Direction.fromByte((byte) 0));
//        Assert.assertTrue("Tank should be able to turn after enough time has passed", turnAttempt2);
//
//        // Verify that the direction has changed to Left
//        Assert.assertEquals(Direction.Up, tank.getDirection());
//    }
//
//
//    @Test
//    public void move_VehicleFacingUpMoveForward_MoveSucceeds() throws IllegalTransitionException, TankDoesNotExistException, LimitExceededException {
//        Tank tank = repo.join("movingVehicle");
//
//        int tankPos = tank.getParent().getPosition();
//
//        tank.setDirection(Direction.Up);
//        Assert.assertEquals(Direction.Up, tank.getDirection());
//
//        boolean move = repo.move(tank.getId(), Direction.fromByte((byte)0));
//        Assert.assertTrue("Backward movement should succeed", move);
//
//        Assert.assertNotEquals(tankPos, tank.getParent().getPosition());
//        Assert.assertEquals(Direction.Up, tank.getDirection());
//    }
//
//    @Test
//    public void move_VehicleFacingDownMoveForward_MoveSucceeds() throws TankDoesNotExistException, IllegalTransitionException, LimitExceededException {
//        Tank tank = repo.join("movingVehicle");
//
//        int tankPos = tank.getParent().getPosition();
//
//        tank.setDirection(Direction.Down);
//        Assert.assertEquals(Direction.Down, tank.getDirection());
//
//        boolean move = repo.move(tank.getId(), Direction.fromByte((byte)4));
//        Assert.assertTrue("Forward movement should succeed", move);
//
//        Assert.assertNotEquals(tankPos, tank.getParent().getPosition());
//        Assert.assertEquals(Direction.Down, tank.getDirection());
//    }
//
//    @Test
//    public void move_VehicleFacingRightMoveForward_MoveSucceeds() throws TankDoesNotExistException, IllegalTransitionException, LimitExceededException {
//        Tank tank = repo.join("movingVehicle");
//
//        int tankPos = tank.getParent().getPosition();
//
//        tank.setDirection(Direction.Right);
//        Assert.assertEquals(Direction.Right, tank.getDirection());
//
//        boolean move = repo.move(tank.getId(), Direction.fromByte((byte)2));
//        Assert.assertTrue("Forward movement should succeed", move);
//
//        Assert.assertNotEquals(tankPos, tank.getParent().getPosition());
//        Assert.assertEquals(Direction.Right, tank.getDirection());
//    }
//
//    @Test
//    public void move_VehicleFacingDownMoveBackwards_MoveSucceeds() throws TankDoesNotExistException, IllegalTransitionException, LimitExceededException {
//        Tank tank = repo.join("movingVehicle");
//
//        int tankPos = tank.getParent().getPosition();
//
//        tank.setDirection(Direction.Down);
//        Assert.assertEquals(Direction.Down, tank.getDirection());
//
//        boolean move = repo.move(tank.getId(), Direction.fromByte((byte)0));
//        Assert.assertTrue("Backward movement should succeed", move);
//
//        Assert.assertNotEquals(tankPos, tank.getParent().getPosition());
//        Assert.assertEquals(Direction.Up, tank.getDirection());
//    }
//
//    @Test
//    public void move_VehicleFacingRightMoveBackward_MoveSucceeds() throws TankDoesNotExistException, IllegalTransitionException, LimitExceededException {
//        Tank tank = repo.join("movingVehicle");
//
//        assert tank != null;
//        int tankPos = tank.getParent().getPosition();
//
//        tank.setDirection(Direction.Right);
//        Assert.assertEquals(Direction.Right, tank.getDirection());
//
//        // Moving backward from Right facing direction (byte 6 is Left direction)
//        boolean move = repo.move(tank.getId(), Direction.fromByte((byte) 6));
//        Assert.assertTrue("Backward movement should succeed", move);
//
//        // Position should have changed
//        Assert.assertNotEquals(tankPos, tank.getParent().getPosition());
//
//        // When moving backward into a new direction, tank faces that direction
//        // Moving backward from Right means we're going Left, so tank should face Left
//        Assert.assertEquals(Direction.Left, tank.getDirection());
//    }
//
//    @Test
//    public void move_VehicleFacingLeftMoveForward_MoveSucceeds() throws TankDoesNotExistException, IllegalTransitionException, LimitExceededException {
//        Tank tank = repo.join("movingVehicle");
//
//        int tankPos = tank.getParent().getPosition();
//
//        tank.setDirection(Direction.Left);
//        Assert.assertEquals(Direction.Left, tank.getDirection());
//
//        boolean move = repo.move(tank.getId(), Direction.fromByte((byte) 6));
//        Assert.assertTrue("Forward movement should succeed", move);
//
//        Assert.assertNotEquals(tankPos, tank.getParent().getPosition());
//        Assert.assertEquals(Direction.Left, tank.getDirection());
//    }
//
//    @Test
//    public void move_VehicleFacingLeftMoveBackward_MoveSucceeds() throws TankDoesNotExistException, IllegalTransitionException, LimitExceededException {
//        Tank tank = repo.join("movingVehicle");
//
//        int tankPos = tank.getParent().getPosition();
//
//        tank.setDirection(Direction.Left);
//        Assert.assertEquals(Direction.Left, tank.getDirection());
//
//        boolean move = repo.move(tank.getId(), Direction.fromByte((byte) 2));
//        Assert.assertTrue("Backward movement should succeed", move);
//
//        Assert.assertNotEquals(tankPos, tank.getParent().getPosition());
//        Assert.assertEquals(Direction.Right, tank.getDirection());
//    }
//
//    @Test
//    public void move_VehicleCanMoveAfterTimePasses_MoveSuccess() throws TankDoesNotExistException, IllegalTransitionException, LimitExceededException {
//        // Mock the current time to simulate time-dependent behavior
//        Tank tank = repo.join("movingVehicle");
//
//        // Set initial state
//        tank.setLastMoveTime(System.currentTimeMillis() - 1000); // Ensure enough time has passed
//        tank.setDirection(Direction.Up);
//        Assert.assertEquals(Direction.Up, tank.getDirection());
//
//        // First move attempt
//        FieldHolder currentPos = tank.getParent();
//        boolean moveAttempt1 = repo.move(tank.getId(), Direction.Up);
//        Assert.assertTrue("First move should succeed", moveAttempt1);
//        Assert.assertNotEquals("Tank position should change", currentPos, tank.getParent());
//
//        // Set up for second move
//        tank.setLastMoveTime(System.currentTimeMillis() - 1000); // Ensure enough time has passed
//        currentPos = tank.getParent();
//
//        // Second move attempt
//        boolean moveAttempt2 = repo.move(tank.getId(), Direction.Down);
//        Assert.assertTrue("Tank should be able to move after enough time has passed", moveAttempt2);
//        Assert.assertNotEquals("Tank position should change", currentPos, tank.getParent());
//    }
//
//    @Test
//    public void testFire() throws Exception {
//
//    }
//
//    @Test
//    public void fire_VehicleCallsFire_SendsBullet() throws LimitExceededException, TankDoesNotExistException {
//        Tank tank = repo.join("movingVehicle");
//
//        if (repo.fire(tank.getId(), 1)) {
//            Assert.assertTrue(true);
//        }
//
//        Assert.assertEquals(Direction.Up, tank.getDirection());
//    }
//
//    @Test
//    public void fire_VehicleCanFireAfterTimePasses_FireSuccess() throws TankDoesNotExistException {
//        // Mock the current time to simulate time-dependent behavior
//        Tank tank = repo.join("turningVehicle");
//        tank.setLastFireTime(System.currentTimeMillis());
//        // First turn attempt without enough time passing (simulate last move time)
//        Assert.assertEquals(Direction.Up, tank.getDirection());
//        tank.setDirection(Direction.Up);
//        Assert.assertEquals(Direction.Up, tank.getDirection());
//        constraints.canFire(tank, mockMillis + tank.getLastFireTime(), 1, bulletDelay);
//
//        boolean fireAttempt1 = repo.fire(tank.getId(), 1);
//        Assert.assertTrue("Tank should be able to turn", fireAttempt1);
//        // Simulate time passage - advance the mock time by 500 milliseconds
//        Assert.assertEquals(Direction.Up, tank.getDirection());
//        // Second turn attempt after enough time has passed
//        constraints.canFire(tank, mockMillis + tank.getLastFireTime(), 1, bulletDelay);
//
//        boolean fireAttempt2 = repo.fire(tank.getId(), 1);
//        Assert.assertFalse("Tank should be able to turn after enough time has passed", fireAttempt2);
//
//        // Verify that the direction has changed to Left
////        Assert.assertEquals(Direction.Down, tank.getDirection());
//    }
//
//    @Test
//    public void testLeave() throws Exception {
//
//    }*/
//}