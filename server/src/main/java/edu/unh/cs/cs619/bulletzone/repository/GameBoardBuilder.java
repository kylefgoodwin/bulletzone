package edu.unh.cs.cs619.bulletzone.repository;

import org.springframework.stereotype.Component;

import java.awt.Point;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.unh.cs.cs619.bulletzone.datalayer.terrain.TerrainType;
import edu.unh.cs.cs619.bulletzone.model.FieldEntity;
import edu.unh.cs.cs619.bulletzone.model.Game;
import edu.unh.cs.cs619.bulletzone.model.Terrain;
import edu.unh.cs.cs619.bulletzone.model.Wall;

@Component()
public class GameBoardBuilder {
    private int width;
    private int height;
    private boolean wrapHorizontal;
    private boolean wrapVertical;
    private Game game = null;
    private TerrainType baseTerrain;
    private TerrainType[][] customTerrain;
    private final Map<Integer, FieldEntity> entities = new HashMap<>();

    public GameBoardBuilder setDimensions(int width, int height) {
        this.width = width;
        this.height = height;
        return this;
    }

    public void setupGame(Game game) {
        attachGame(game);
        addWalls(List.of(1, 2, 3, 17, 34, 35, 51, 5, 21, 37, 53, 7, 23, 39, 8, 40, 9, 25, 41, 57, 73));
        addWalls(List.of(33, 49, 65, 66, 67, 69, 71, 72), 1500);
//        addTerrain(118, 2);
//        addTerrain(121, 3);
        addTerrains(List.of(115, 116, 117, 118, 130, 131, 132, 133), 1);
        addTerrains(List.of(205, 206, 207, 208, 209, 220, 222, 223, 224), 3);
        addTerrains(List.of(182, 183, 184, 187, 188, 189, 199, 200, 201, 202), 2);
        addTerrains(List.of(76, 77, 78, 60, 61, 62, 44, 45, 46), 3);
        addTerrains(List.of(153, 154, 155, 156, 157, 158, 105, 106, 107, 108, 109, 110, 137, 138, 139, 140, 141, 142, 126, 125, 124, 123, 122, 121), 4);
        build();
    }
    public GameBoardBuilder attachGame(Game game) {
        this.game = game;
        return this;
    }

    // Enables horizontal wrapping
    public GameBoardBuilder wrapHorizontal() {
        this.wrapHorizontal = true;
        return this;
    }

    // Enables vertical wrapping
    public GameBoardBuilder wrapVertical() {
        this.wrapVertical = true;
        return this;
    }

    public GameBoardBuilder withBaseTerrain(TerrainType t) {
        this.baseTerrain = t;
        return this;
    }


    public GameBoardBuilder withTerrain(int x, int y, TerrainType t) {
        customTerrain[x][y] = t;
        return this;
    }

    public GameBoardBuilder addWall(int position) {
        entities.put(position, new Wall());
        return this;
    }

    public GameBoardBuilder addWall(int position, int health) {
        entities.put(position, new Wall(health, position));
        return this;
    }

    public GameBoardBuilder addTerrain(int position, int terrainType) {
        entities.put(position, new Terrain(terrainType));
        return this;
    }

    public GameBoardBuilder addWalls(List<Integer> positions) {
        positions.forEach(position -> entities.put(position, new Wall()));
        return this;
    }

    public GameBoardBuilder addWalls(List<Integer> positions, int health) {
        positions.forEach(position -> entities.put(position, new Wall(health, position)));
        return this;
    }

    public GameBoardBuilder addTerrains(List<Integer> positions, int terrainType) {
        positions.forEach(position -> entities.put(position, new Terrain(terrainType)));
        return this;
    }

    public void build(){
        if (game == null){
            throw new IllegalStateException("Add game before creating board");
        }

        for (int i = 0; i < width * height; i++) {
                game.getHolderGrid().get(i).setTerrainEntityHolder(new Terrain());
        }

        entities.forEach((position, entity) -> {
            if (entity.isWall()) {
                game.getHolderGrid().get(position).setFieldEntity(entity);
            } else if (entity.isTerrain()) {
                game.getHolderGrid().get(position).setTerrainEntityHolder(entity);
            }
        });
    }
}

