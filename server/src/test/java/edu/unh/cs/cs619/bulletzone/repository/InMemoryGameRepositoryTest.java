package edu.unh.cs.cs619.bulletzone.repository;

import static org.mockito.Mockito.mock;

import edu.unh.cs.cs619.bulletzone.model.Direction;
import edu.unh.cs.cs619.bulletzone.model.FieldHolder;
import edu.unh.cs.cs619.bulletzone.model.Game;
import edu.unh.cs.cs619.bulletzone.model.Tank;
import edu.unh.cs.cs619.bulletzone.model.Wall;
import org.javatuples.Pair;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.HashMap;
import java.util.Map;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class InMemoryGameRepositoryTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();
    @InjectMocks
    InMemoryGameRepository repo;
    private InMemoryGameRepository gameRepository;
    @Mock
    private Constraints constraints;

    @Mock
    private Game mockGame;
    @Mock
    private Tank mockTank;

    private final long mockMillis = 500;
    private final int[] bulletDelay = {500, 1000, 1500};
    private final long tankId = 1L;
    private final String tankIp = "192.168.1.1";

    @Before
    public void setUp() throws Exception {
        constraints = mock(Constraints.class);
        gameRepository = new InMemoryGameRepository(constraints, new GameBoardBuilder());
        mockGame = mock(Game.class);
        mockTank = mock(Tank.class);
        Map<Long, Tank> tanks = new HashMap<>();
        tanks.put(tankId, mockTank);
        gameRepository.create();

        // Set up indestructible walls around a cell at (5,5) for the spawn test
        Game game = gameRepository.getGame();
        int gridSize = 16; // Assuming FIELD_DIM = 16

        int[] indestructibleWallPositions = {
                4 * gridSize + 5, // Above (5,5)
                5 * gridSize + 4, // Left of (5,5)
                5 * gridSize + 6, // Right of (5,5)
                6 * gridSize + 5  // Below (5,5)
        };

        for (int pos : indestructibleWallPositions) {
            FieldHolder wallHolder = game.getHolderGrid().get(pos);
            wallHolder.setTerrainEntityHolder(new Wall(0, pos)); // Use setTerrainEntityHolder
        }
    }

    @Test
    public void testJoin() throws Exception {
        Tank tank = repo.join("").getValue0();
        Assert.assertNotNull(tank);
        Assert.assertTrue(tank.getId() >= 0);
        Assert.assertNotNull(tank.getDirection());
        Assert.assertTrue(tank.getDirection() == Direction.Up);
        Assert.assertNotNull(tank.getParent());
    }

    @Test
    public void testSpawnAvoidsSurroundedByIndestructibleWalls() {
        // Attempt to join a new player to the game
        Pair<Tank, ?> playerPair = gameRepository.join("127.0.0.1");
        Tank tank = playerPair.getValue0();

        // Retrieve the spawn position
        FieldHolder spawnHolder = tank.getParent();

        // Check that at least one neighboring cell is open
        boolean hasOpenNeighbor = false;
        for (Direction direction : Direction.values()) {
            FieldHolder neighbor = spawnHolder.getNeighbor(direction);
            if (neighbor != null && (!neighbor.isTerrainPresent() ||
                    !(neighbor.getTerrainEntityHolder() instanceof Wall) ||
                    ((Wall) neighbor.getTerrainEntityHolder()).getIntValue() != 0)) {
                hasOpenNeighbor = true;
                break;
            }
        }

        // Assert that the spawn location is not fully surrounded by indestructible walls
        Assert.assertTrue("Player spawned in a location surrounded by indestructible walls", hasOpenNeighbor);
    }

    // Additional tests from your existing code can go here
}