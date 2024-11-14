package edu.unh.cs.cs619.bulletzone.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import edu.unh.cs.cs619.bulletzone.model.events.SpawnEvent;

public final class Game {
    private static final int FIELD_DIM = 16;
    private final long id;
    private final ArrayList<FieldHolder> holderGrid = new ArrayList<>();
    private final ArrayList<FieldHolder> itemHolderGrid = new ArrayList<>();
    private final ArrayList<FieldHolder> terrainHolderGrid = new ArrayList<>();

    private final ConcurrentMap<Long, Tank> tanks = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Long> playersIP = new ConcurrentHashMap<>();
    private final ConcurrentMap<Long, Builder> builders = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Long> playersIPBuilders = new ConcurrentHashMap<>();
    private final ConcurrentMap<Long, Soldier> soldiers = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Long> playersIPSoldiers = new ConcurrentHashMap<>();
    private final ConcurrentMap<Long, Double> playerCredits = new ConcurrentHashMap<>();

    public Game() {
        this.id = 0;
    }

    @JsonIgnore
    public long getId() {
        return id;
    }

    @JsonIgnore
    public ArrayList<FieldHolder> getHolderGrid() {
        return holderGrid;
    }

    @JsonIgnore
    public ArrayList<FieldHolder> getItemHolderGrid() {
        return itemHolderGrid;
    }

    @JsonIgnore
    public ArrayList<FieldHolder> getTerrainHolderGrid() {
        return terrainHolderGrid;
    }

    public void addTank(String ip, Tank tank) {
        synchronized (tanks) {
            tanks.put(tank.getId(), tank);
            playersIP.put(ip, tank.getId());
            playerCredits.put(tank.getId(), 0.0); // Initialize credits for new tank
        }
        EventBus.getDefault().post(new SpawnEvent(tank.getIntValue(), tank.getPosition()));
    }

    public void addSoldier(String ip, Soldier soldier) {
        synchronized (soldiers) {
            soldiers.put(soldier.getId(), soldier);
            playersIPSoldiers.put(ip, soldier.getId());
        }
    }

    public void addCredits(long tankId, double amount) {
        playerCredits.compute(tankId, (key, oldValue) ->
                (oldValue == null ? 0 : oldValue) + amount);
    }

    public double getCredits(long tankId) {
        return playerCredits.getOrDefault(tankId, 0.0);
    }

    public Tank getTank(Long tankId) {
        return tanks.get(tankId);
    }

    public ConcurrentMap<Long, Tank> getTanks() {
        return tanks;
    }

    public Soldier getSoldier(long soldierId) {
        return soldiers.get(soldierId);
    }

    public Soldier getSoldier(String ip) {
        if (playersIPSoldiers.containsKey(ip)) {
            return soldiers.get(playersIPSoldiers.get(ip));
        }
        return null;
    }

    public ConcurrentMap<Long, Soldier> getSoldiers() {
        return soldiers;
    }

    public void removeSoldier(long soldierId) {
        synchronized (soldiers) {
            Soldier soldier = soldiers.remove(soldierId);
            if (soldier != null) {
                playersIPSoldiers.remove(soldier.getIp());
            }
        }
    }

    public List<Optional<FieldEntity>> getGrid() {
        synchronized (holderGrid) {
            List<Optional<FieldEntity>> entities = new ArrayList<>();

            FieldEntity entity;
            for (FieldHolder holder : holderGrid) {
                if (holder.isPresent()) {
                    entity = holder.getEntity();
                    entity = entity.copy();
                    entities.add(Optional.of(entity));
                } else {
                    entities.add(Optional.empty());
                }
            }
            return entities;
        }
    }

    public Tank getTank(String ip){
        if (playersIP.containsKey(ip)){
            return tanks.get(playersIP.get(ip));
        }
        return null;
    }

    public void removeTank(long tankId){
        synchronized (tanks) {
            Tank t = tanks.remove(tankId);
            if (t != null) {
                playersIP.remove(t.getIp());
                playerCredits.remove(tankId);
            }
        }
    }

    public void addBuilder(String ip, Builder builder) {
        synchronized (builders) {
            builders.put(builder.getId(), builder);
            playersIPBuilders.put(ip, builder.getId());
        }
        EventBus.getDefault().post(new SpawnEvent(builder.getIntValue(), builder.getPosition()));
    }

    public void removeBuilder(long builderId){
        synchronized (builders) {
            Builder b = builders.remove(builderId);
            if (b != null) {
                playersIPBuilders.remove(b.getIp());
            }
        }
    }

    public Builder getBuilder(long builderId) {
        return builders.get(builderId);
    }

    public Builder getBuilder(String ip){
        if (playersIPBuilders.containsKey(ip)){
            return builders.get(playersIPBuilders.get(ip));
        }
        return null;
    }

    public ConcurrentMap<Long, Builder> getBuilders() {
        return builders;
    }

    /**
     * Converts the 3 FieldHolder Grids into 1 2D int[][].
     * For each cell in the int array, there are 3 values that can be iterated through in the second value
     * The in each "tuple" its goes (playerData, itemData, terrainData)
     * @return
     */
    public int[][] getGrid2D() {
        int[][] grid = new int[FIELD_DIM][FIELD_DIM];

        synchronized (holderGrid) {
            FieldHolder holder;
            for (int i = 0; i < FIELD_DIM; i++) {
                for (int j = 0; j < FIELD_DIM; j++) {
                    holder = holderGrid.get(i * FIELD_DIM + j);
                    if (holder.isPresent()) {
                        grid[i][j] = holder.getEntity().getIntValue();
                    } else {
                        grid[i][j] = 0;
                    }
                }
            }
        }

        return grid;

    }

    public int[][] getItemGrid2D() {
        int[][] grid = new int[FIELD_DIM][FIELD_DIM];

        synchronized (itemHolderGrid) {
            FieldHolder holder;
            for (int i = 0; i < FIELD_DIM; i++) {
                for (int j = 0; j < FIELD_DIM; j++) {
                    holder = holderGrid.get(i * FIELD_DIM + j);
                    if (holder.isPresent()) {
                        grid[i][j] = holder.getEntity().getIntValue();
                    } else {
                        grid[i][j] = 0;
                    }
                }
            }
        }

        return grid;
    }

    public int[][] getTerrainGrid2D() {
        int[][] grid = new int[FIELD_DIM][FIELD_DIM];

        synchronized (holderGrid) {
            FieldHolder holder;
            for (int i = 0; i < FIELD_DIM; i++) {
                for (int j = 0; j < FIELD_DIM; j++) {
                    holder = holderGrid.get(i * FIELD_DIM + j);
                    if (holder.isTerrainPresent()) {
                        grid[i][j] = holder.getTerrainEntityHolder().getIntValue();
                    } else {
                        grid[i][j] = 0;
                    }
                }
            }
        }

        return grid;
    }
}