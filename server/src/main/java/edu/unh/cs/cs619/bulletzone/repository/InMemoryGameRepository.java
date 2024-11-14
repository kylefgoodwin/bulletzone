package edu.unh.cs.cs619.bulletzone.repository;

import org.greenrobot.eventbus.EventBus;
import org.javatuples.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicLong;
//import org.java.tuples.Pair; // ???

import edu.unh.cs.cs619.bulletzone.model.Builder;
import edu.unh.cs.cs619.bulletzone.model.Bullet;
import edu.unh.cs.cs619.bulletzone.model.Direction;
import edu.unh.cs.cs619.bulletzone.model.FieldHolder;
import edu.unh.cs.cs619.bulletzone.model.Game;
import edu.unh.cs.cs619.bulletzone.model.IllegalTransitionException;
import edu.unh.cs.cs619.bulletzone.model.Item;
import edu.unh.cs.cs619.bulletzone.model.LimitExceededException;
import edu.unh.cs.cs619.bulletzone.model.Playable;
import edu.unh.cs.cs619.bulletzone.model.Tank;
import edu.unh.cs.cs619.bulletzone.model.TankDoesNotExistException;
import edu.unh.cs.cs619.bulletzone.model.events.SpawnEvent;

import static com.google.common.base.Preconditions.checkNotNull;

@Component
public class InMemoryGameRepository implements GameRepository {

    /**
     * Field dimensions
     */
    private static final int FIELD_DIM = 16;

    /**
     * Bullet step time in milliseconds
     */
    private static final int BULLET_PERIOD = 200;

    /**
     * Bullet's impact effect [life]
     */
    private static final int BULLET_DAMAGE = 1;

    /**
     * Tank's default life [life]
     */
    private final Timer timer = new Timer();
    private final AtomicLong idGenerator = new AtomicLong();
    private final Object monitor = new Object();
    private Game game = null;
    private final int[] bulletDelay = {500, 1000, 1500};
    private final int[] trackActiveBullets = {0, 0, 0, 0, 0, 0};
    private final Timer itemSpawnTimer = new Timer();
    private static final int ITEM_SPAWN_INTERVAL = 15000; // 15 seconds
    private static final Random random = new Random();

    private final FireCommand fireCommand;
    private GameBoardBuilder gameBoardBuilder;

    @Autowired
    public InMemoryGameRepository(Constraints tankConstraintChecker, GameBoardBuilder gameBoardBuilder) {
        this.fireCommand = new FireCommand();
        this.gameBoardBuilder = new GameBoardBuilder();
    }

    @Override
    public Pair<Tank, Builder> join(String ip) {
        synchronized (this.monitor) {
            Tank tank;
            Builder builder;
            if (game == null) {
                this.create();
            }

            if ((tank = game.getTank(ip)) != null && (builder = game.getBuilder(ip)) != null) {
                return Pair.with(tank,builder);
            }

            Long Id = this.idGenerator.getAndIncrement();

            tank = new Tank(Id, Direction.Up, ip);
            builder = new Builder(Id, Direction.Up, ip);

            Random random = new Random();
            int x;
            int y;

            // This may run for forever.. If there is no free space. XXX
            for (; ; ) {
                x = random.nextInt(FIELD_DIM);
                y = random.nextInt(FIELD_DIM);
                FieldHolder fieldElement = game.getHolderGrid().get(x * FIELD_DIM + y);
                if (!fieldElement.isPresent()) {
                    fieldElement.setFieldEntity(tank);
                    tank.setParent(fieldElement);
                    break;
                }
            }

            for (; ; ) {
                x = random.nextInt(FIELD_DIM);
                y = random.nextInt(FIELD_DIM);
                FieldHolder fieldElement = game.getHolderGrid().get(x * FIELD_DIM + y);
                if (!fieldElement.isPresent()) {
                    fieldElement.setFieldEntity(builder);
                    builder.setParent(fieldElement);
                    break;
                }
            }

            game.addTank(ip, tank);
            game.addBuilder(ip, builder);
            return Pair.with(tank,builder);
        }
    }

    @Override
    public Game getGame() {
        synchronized (this.monitor) {
            if (game == null) {
                this.create();
            }
        }
        return game;
    }

    @Override
    public boolean turn(long playableId, int playableType, Direction direction)
            throws TankDoesNotExistException, IllegalTransitionException, LimitExceededException {
        synchronized (this.monitor) {
            checkNotNull(direction);

            Playable playable = null;

            if (playableType == 1) {
                playable = game.getTanks().get(playableId);
                if(playable.gethasSoldier()){
                    playable = game.getSoldiers().get(playableId);
                }
            }

            if (playable == null && playableType == 2) {
                playable = game.getBuilders().get(playableId);
            }

            if (playable == null && playableType == 3) {
                playable = game.getTanks().get(playableId);
                if(playable.gethasSoldier()){
                    playable = game.getSoldiers().get(playableId);
                }
            }

            if (playable == null) {
                throw new TankDoesNotExistException(playableId);
            }
            long millis = System.currentTimeMillis();

            TurnCommand turnCommand = new TurnCommand(playable, game, direction, millis);

            /*try {
                Thread.sleep(500);
            } catch(InterruptedException ex) {
                Thread.currentThread().interrupt();
            }*/
            return turnCommand.execute();
        }
    }

    @Override
    public boolean move(long playableId, int playableType, Direction direction)
            throws TankDoesNotExistException, IllegalTransitionException, LimitExceededException {
        synchronized (this.monitor) {
            // Find tank

            Playable playable = null;

            if (playableType == 1) {
                playable = game.getTanks().get(playableId);
                if(playable.gethasSoldier()){
                    playable = game.getSoldiers().get(playableId);
                }
            }

            if (playable == null && playableType == 2) {
                playable = game.getBuilders().get(playableId);
            }

            if (playable == null && playableType == 3) {
                playable = game.getTanks().get(playableId);
                if(playable.gethasSoldier()){
                    playable = game.getSoldiers().get(playableId);
                }
            }

            if (playable == null) {
                throw new TankDoesNotExistException(playableId);
            }

            long millis = System.currentTimeMillis();
            MoveCommand moveCommand = new MoveCommand(playable, playableType, game, direction, millis);

            return moveCommand.execute();
        }
    }

    @Override
    public boolean fire(long playableId, int playableType, int bulletType)
            throws TankDoesNotExistException {
        synchronized (this.monitor) {

            // Find playable
            Playable playable = null;

            if (playableType == 1) {
                playable = game.getTanks().get(playableId);
                if(playable.gethasSoldier()){
                    playable = game.getSoldiers().get(playableId);
                }
            }

            if (playable == null && playableType == 2) {
                playable = game.getBuilders().get(playableId);
            }

            if (playable == null && playableType == 3) {
                playable = game.getTanks().get(playableId);
                if(playable.gethasSoldier()){
                    playable = game.getSoldiers().get(playableId);
                }
            }

            if (playable == null) {
                throw new TankDoesNotExistException(playableId);
            }
            long millis = System.currentTimeMillis();
            final Playable finalPlayable = playable;

            //Log.i(TAG, "Cannot find user with id: " + tankId);
            Direction direction = playable.getDirection();
            FieldHolder parent = playable.getParent();
            if (!fireCommand.canFire(playable, millis, bulletType, bulletDelay)) {
                return false;
            }
            playable.setNumberOfBullets(playable.getNumberOfBullets() + 1);
            int bulletId = fireCommand.assignBulletId(trackActiveBullets, playable);
            if (bulletId == -1) {
                // No available bullet slots
                return false;
            }
            // Create a new bullet to fire
            final Bullet bullet = new Bullet(playableId, direction, playable.getBulletDamage());
            // Set the same parent for the bullet.
            // This should be only a one way reference.
            bullet.setParent(parent);
            bullet.setBulletId(bulletId);

            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    synchronized (monitor) {
                        System.out.println("Active Bullet: " + finalPlayable.getNumberOfBullets() + "---- Bullet ID: " + bullet.getIntValue());
                        fireCommand.moveBulletAndHandleCollision(game, bullet, finalPlayable, playableType, trackActiveBullets, this);
                    }
                }
            }, 0, BULLET_PERIOD);

            return true;
        }
    }

    @Override
    public boolean build(long playableId, int playableType, String entity)
            throws TankDoesNotExistException {
        synchronized (this.monitor) {
            if (playableType == 2) {
                Playable playable = game.getBuilders().get(playableId);
                if (playable == null) {
                    //Log.i(TAG, "Cannot find user with id: " + tankId);
                    throw new TankDoesNotExistException(playableId);
                }
                BuildCommand buildCommand = new BuildCommand(playableId, game, entity);
                return buildCommand.execute();
            } else {
                System.out.println("Player is not controlling the builder, building blocked.");
                return false;
            }
        }
    }

    @Override
    public void leave(long playableId)
            throws TankDoesNotExistException {
        synchronized (this.monitor) {
            if (!this.game.getTanks().containsKey(playableId)) {
                throw new TankDoesNotExistException(playableId);
            }

            System.out.println("leave() called, ID: " + playableId);

            Tank tank = game.getTanks().get(playableId);
            Builder builder = game.getBuilders().get(playableId);
            FieldHolder tankparent = tank.getParent();
            tankparent.clearField();
            FieldHolder builderparent = builder.getParent();
            builderparent.clearField();
            game.removeTank(playableId);
            game.removeBuilder(playableId);
        }
    }

    public void create() {
        if (game != null) {
            return;
        }
        synchronized (this.monitor) {
            System.out.println("Creating new game and starting item spawner...");
            this.game = new Game();
            createFieldHolderGrid(game);
            gameBoardBuilder.setupGame(game);
            startItemSpawner();
        }
    }

    private void createFieldHolderGrid(Game game) {
        synchronized (this.monitor) {
            game.getHolderGrid().clear();
            game.getItemHolderGrid().clear();
            game.getTerrainHolderGrid().clear();
            for (int i = 0; i < FIELD_DIM * FIELD_DIM; i++) {
                game.getHolderGrid().add(new FieldHolder(i));
                game.getItemHolderGrid().add(new FieldHolder(i));
                game.getTerrainHolderGrid().add(new FieldHolder(i));
            }

            FieldHolder targetHolder;
            FieldHolder rightHolder;
            FieldHolder downHolder;
            FieldHolder leftHolder;
            FieldHolder upHolder;

            // Build connections
            for (int i = 0; i < FIELD_DIM; i++) {
                for (int j = 0; j < FIELD_DIM; j++) {
                    targetHolder = game.getHolderGrid().get(i * FIELD_DIM + j);

                    rightHolder = game.getHolderGrid().get(i * FIELD_DIM
                            + ((j + 1) % FIELD_DIM));
                    downHolder = game.getHolderGrid().get(((i + 1) % FIELD_DIM)
                            * FIELD_DIM + j);

                    targetHolder.addNeighbor(Direction.Right, rightHolder);
                    rightHolder.addNeighbor(Direction.Left, targetHolder);

                    targetHolder.addNeighbor(Direction.Down, downHolder);
                    downHolder.addNeighbor(Direction.Up, targetHolder);
                }
            }
        }
    }

    private void spawnRandomItem() {
        // Add this debug print at the start
        System.out.println("Attempting to spawn random item...");

        for (int attempts = 0; attempts < 10; attempts++) {
            int x = random.nextInt(FIELD_DIM);
            int y = random.nextInt(FIELD_DIM);
            FieldHolder fieldElement = game.getHolderGrid().get(x * FIELD_DIM + y);
            if (!fieldElement.isPresent()) {
                // Weighted random selection
                int itemType = random.nextInt(3) + 1; // 1=Thingamajig, 2=AntiGrav, 3=FusionReactor
                Item item = new Item(itemType);
                fieldElement.setFieldEntity(item);
                item.setParent(fieldElement);

                // Add these debug prints
                System.out.println("Successfully spawned item!");
                System.out.println("Item type: " + itemType);
                System.out.println("Item value: " + item.getIntValue());
                System.out.println("Position: [" + x + "," + y + "]");

                EventBus.getDefault().post(new SpawnEvent(item.getIntValue(), fieldElement.getPosition()));
                break;
            }
        }
    }

    private void startItemSpawner() {
        System.out.println("Starting item spawner timer...");
        itemSpawnTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                synchronized (monitor) {
                    spawnRandomItem();
                }
            }
        }, 5000, 15000); // First spawn after 5 seconds, then every 15 seconds
    }

    @Override
    public boolean ejectSoldier(long playableId) throws TankDoesNotExistException {
        synchronized (this.monitor) {
            Playable playable = game.getTanks().get(playableId);
            if (playable == null) {
                throw new TankDoesNotExistException(playableId);
            }
            if (game.getSoldiers().get(playableId) != null){
                return false;
            }
            long millis = System.currentTimeMillis();
            EjectSoldierCommand ejectsoldier = new EjectSoldierCommand(playableId, game, playable.getDirection(), millis);
            return ejectsoldier.execute();
        }
    }

    @Override
    public boolean ejectPowerUp(long tankId) throws TankDoesNotExistException {
        synchronized (this.monitor) {
            Tank tank = game.getTanks().get(tankId);
            if (tank == null) {
                throw new TankDoesNotExistException(tankId);
            }

            return tank.tryEjectPowerUp(tank.getParent());
        }
    }

    @Override
    public int getLife(long playableId, int playableType) throws TankDoesNotExistException {
        Playable playable;
        if (playableType == 1){
            playable = game.getTanks().get(playableId);
//            System.out.println(playableId);
//            System.out.println(playable.getLife());
        } else if (playableType == 2){
            playable = game.getBuilders().get(playableId);
        } else {
            //code to get soldier (do we want a soldier list too?
            playable = null;
        }
        if (playable == null) {
            //Log.i(TAG, "Cannot find user with id: " + tankId);
            //return false;
            throw new TankDoesNotExistException(playableId);
        }

        return playable.getLife();
    }
}