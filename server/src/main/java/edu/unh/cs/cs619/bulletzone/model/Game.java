package edu.unh.cs.cs619.bulletzone.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.juli.logging.Log;
import org.greenrobot.eventbus.EventBus;

import edu.unh.cs.cs619.bulletzone.datalayer.account.BankAccount;
import edu.unh.cs.cs619.bulletzone.model.events.SpawnEvent;

public final class Game {
    private static final int FIELD_DIM = 16;
    private final long id;
    private final ArrayList<FieldHolder> holderGrid = new ArrayList<>();
    private final ArrayList<FieldHolder> itemHolderGrid = new ArrayList<>();
    private final ArrayList<FieldHolder> terrainHolderGrid = new ArrayList<>();

    private final ConcurrentMap<String, Long> playersIP = new ConcurrentHashMap<>(); // Maps IP to Player ID
    private final ConcurrentMap<Long, Playable[]> playables = new ConcurrentHashMap<>(); // Stores playables by player ID

    private final ConcurrentMap<Long, Double> playerCredits = new ConcurrentHashMap<>();
    private final ConcurrentMap<Long, BankAccount> playerAccounts = new ConcurrentHashMap<>();

    private boolean isSoldierEjected = false;

    public Game() {
        this.id = 0;
    }

    @JsonIgnore
    public long getId() {
        return id;
    }

    public BankAccount getBankAccount(long playerId) {
        return playerAccounts.computeIfAbsent(playerId, BankAccount::new);
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

    public void setSoldierEjected(boolean isEjected) {
        this.isSoldierEjected = isEjected;
    }

    public boolean getSolderEjected() {
        return this.isSoldierEjected;
    }

    // Method to add credits to a player's bank account
    public void modifyBalance(long playerId, double amount) {
        BankAccount account = getBankAccount(playerId);
        account.modifyBalance(amount);
    }

    public double getCredits(long playerId) {
        return playerCredits.getOrDefault(playerId, 0.0);
    }

    // Method to get the playable (tank, builder, or soldier) by player ID and type
    public Playable getPlayable(long playerId, int playableType) {
        Playable[] playablesArray = playables.get(playerId);
        if (playablesArray != null && playableType >= 1 && playableType < playablesArray.length) {
            return playablesArray[playableType];
        }
        return null; // Return null if not found
    }

    // Method to get a playable by IP
    public Playable getPlayable(String ip, int playableType) {
        Long playerId = playersIP.get(ip);
        if (playerId != null) {
            return getPlayable(playerId, playableType);
        }
        return null;
    }


    public ConcurrentMap<Long, Playable[]> getPlayables() {
        return playables;
    }

    public void addPlayable(long playerId, Playable playable, String ip) {
        synchronized (playables) {
            Playable[] playablesArray = playables.computeIfAbsent(playerId, k -> new Playable[4]);
            int type = playable.getPlayableType();
            if (type >= 1 && type < playablesArray.length) {
                playablesArray[type] = playable;
            }

            // Update the playables map with the new array of playables
            playables.put(playerId, playablesArray);

            // Update the playersIP map with the player ID under the corresponding IP
            playersIP.put(ip, playerId);
        }
    }

    // Remove a playable by player ID and type
    public void removePlayable(long playerId, int playableType) {
        synchronized (playables) {
            Playable[] playablesArray = playables.get(playerId);
            if (playablesArray != null && playableType >= 1 && playableType < playablesArray.length) {
                playablesArray[playableType] = null; // Remove the playable
                playables.put(playerId, playablesArray);
            }
        }
    }

    public void addTank(String ip, Tank tank) {
        synchronized (playables){
            addPlayable(tank.getId(), tank, ip);
            playerCredits.put(tank.getId(), 1000.0); // Initialize credits for new tank
            playerAccounts.putIfAbsent(tank.getId(), new BankAccount(tank.getId()));
            EventBus.getDefault().post(new SpawnEvent(tank.getIntValue(), tank.getPosition()));
        }
    }

    public Tank getTank(long tankId) {
        Playable[] playablesArray = playables.get(tankId);
        return (Tank) (playablesArray != null ? playablesArray[1] : null);
    }

    public Tank getTank(String ip) {
        Long playerId = playersIP.get(ip);
        return playerId != null ? getTank(playerId) : null;
    }

    public Map<Long, Tank> getTanks() {
        Map<Long, Tank> allTanks = new HashMap<>();
        // Iterate over each player ID in the playables map
        for (Long playerId : playables.keySet()) {
            Playable[] playablesArray = playables.get(playerId);
            if (playablesArray != null) {
                // Check if the playable at index 1 is a Tank
                Playable playable = playablesArray[1];
                if (playable != null && playable.getPlayableType() == 1) {
                    allTanks.put(playerId, (Tank) playable); // Add the player ID and the tank to the map
                }
            }
        }
        return allTanks; // Return the map of player IDs to tanks
    }

    public void removeTank(long tankId){
        removePlayable(tankId, 1); // Remove tank (type 1)
    }

    public void addBuilder(String ip, Builder builder) {
        synchronized (playables) {
            addPlayable(builder.getId(), builder, ip);
            playerCredits.put(builder.getId(), 1000.0); // Initialize credits for new tank
            playerAccounts.putIfAbsent(builder.getId(), new BankAccount(builder.getId()));
            EventBus.getDefault().post(new SpawnEvent(builder.getIntValue(), builder.getPosition()));
        }
        EventBus.getDefault().post(new SpawnEvent(builder.getIntValue(), builder.getPosition()));
    }

    public Builder getBuilder(long builderId) {
        Playable[] playablesArray = playables.get(builderId);
        return (Builder) (playablesArray != null ? playablesArray[2] : null);
    }

    public Builder getBuilder(String ip){
        Long playerId = playersIP.get(ip);
        return playerId != null ? getBuilder(playerId) : null;
    }

    public Map<Long, Builder> getBuilders() {
        Map<Long, Builder> allBuilders = new HashMap<>();

        // Iterate over each player ID in the playables map
        for (Long playerId : playables.keySet()) {
            Playable[] playablesArray = playables.get(playerId);
            if (playablesArray != null) {
                // Check if the playable at index 2 is a Builder
                Playable playable = playablesArray[2];
                if (playable != null && playable.getPlayableType() == 2) {
                    allBuilders.put(playerId, (Builder) playable); // Add the player ID and the builder to the map
                }
            }
        }
        return allBuilders; // Return the map of player IDs to builders
    }

    public void removeBuilder(long builderId) {
        removePlayable(builderId, 2); // Remove builder (type 2)
    }

    public void addSoldier(String ip, Soldier soldier) {
        synchronized (playables) {
            addPlayable(soldier.getId(), soldier, ip);
            playerCredits.put(soldier.getId(), 1000.0); // Initialize credits for new soldier
            playerAccounts.putIfAbsent(soldier.getId(), new BankAccount(soldier.getId()));
        }
        EventBus.getDefault().post(new SpawnEvent(soldier.getIntValue(), soldier.getPosition()));
    }

    public Soldier getSoldier(long soldierId) {
        Playable[] playablesArray = playables.get(soldierId);
        return (Soldier) (playablesArray != null ? playablesArray[3] : null);
    }

    public Soldier getSoldier(String ip) {
        Long playerId = playersIP.get(ip);
        return playerId != null ? getSoldier(playerId) : null;
    }

    public Map<Long, Soldier> getSoldiers() {
        Map<Long, Soldier> allSoldiers = new HashMap<>();

        // Iterate over each player ID in the playables map
        for (Long playerId : playables.keySet()) {
            Playable[] playablesArray = playables.get(playerId);
            if (playablesArray != null) {
                // Check if the playable at index 3 is a Soldier
                Playable playable = playablesArray[3];
                if (playable != null && playable.getPlayableType() == 3) {
                    allSoldiers.put(playerId, (Soldier) playable); // Add the player ID and the soldier to the map
                }
            }
        }

        return allSoldiers; // Return the map of player IDs to soldiers
    }

    public void removeSoldier(long soldierId) {
        removePlayable(soldierId, 3); // Remove soldier (type 3)
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