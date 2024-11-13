package edu.unh.cs.cs619.bulletzone.repository;

import org.springframework.stereotype.Component;

import java.awt.Point;
import java.util.HashMap;

import edu.unh.cs.cs619.bulletzone.datalayer.terrain.TerrainType;
import edu.unh.cs.cs619.bulletzone.model.Game;
//import edu.unh.cs.cs619.bulletzone.model.GameBoard;
import edu.unh.cs.cs619.bulletzone.model.Terrain;
import edu.unh.cs.cs619.bulletzone.model.Wall;
import jdk.jfr.Category;

@Component()
public class GameBoardBuilder {
    private int width;
    private int height;
    private boolean wrapHorizontal;
    private boolean wrapVertical;
    private Game game = null;
    private TerrainType baseTerrain;
    private TerrainType[][] customTerrain;
//    private GameBoard gameBoard;

    public GameBoardBuilder setDimensions(int width, int height) {
        this.width = width;
        this.height = height;
//        gameBoard = new GameBoard(width, height);
        customTerrain = new TerrainType[width][height];
        return this;
    }

    public void setupGame(Game game) {

        // Placing walls on the game board, can be extracted to another method for cleaner code.
        game.getHolderGrid().get(1).setFieldEntity(new Wall());
        game.getHolderGrid().get(2).setFieldEntity(new Wall());
        game.getHolderGrid().get(3).setFieldEntity(new Wall());

        game.getHolderGrid().get(17).setFieldEntity(new Wall());
        game.getHolderGrid().get(33).setFieldEntity(new Wall(1500, 33));
        game.getHolderGrid().get(49).setFieldEntity(new Wall(1500, 49));
        game.getHolderGrid().get(65).setFieldEntity(new Wall(1500, 65));

        game.getHolderGrid().get(34).setFieldEntity(new Wall());
        game.getHolderGrid().get(66).setFieldEntity(new Wall(1500, 66));

        game.getHolderGrid().get(35).setFieldEntity(new Wall());
        game.getHolderGrid().get(51).setFieldEntity(new Wall());
        game.getHolderGrid().get(67).setFieldEntity(new Wall(1500, 67));

        game.getHolderGrid().get(5).setFieldEntity(new Wall());
        game.getHolderGrid().get(21).setFieldEntity(new Wall());
        game.getHolderGrid().get(37).setFieldEntity(new Wall());
        game.getHolderGrid().get(53).setFieldEntity(new Wall());
        game.getHolderGrid().get(69).setFieldEntity(new Wall(1500, 69));

        game.getHolderGrid().get(7).setFieldEntity(new Wall());
        game.getHolderGrid().get(23).setFieldEntity(new Wall());
        game.getHolderGrid().get(39).setFieldEntity(new Wall());
        game.getHolderGrid().get(71).setFieldEntity(new Wall(1500, 71));

        game.getHolderGrid().get(8).setFieldEntity(new Wall());
        game.getHolderGrid().get(40).setFieldEntity(new Wall());
        game.getHolderGrid().get(72).setFieldEntity(new Wall(1500, 72));

        game.getHolderGrid().get(9).setFieldEntity(new Wall());
        game.getHolderGrid().get(25).setFieldEntity(new Wall());
        game.getHolderGrid().get(41).setFieldEntity(new Wall());
        game.getHolderGrid().get(57).setFieldEntity(new Wall());
        game.getHolderGrid().get(73).setFieldEntity(new Wall());

        //Terrain test cells
        game.getHolderGrid().get(115).setTerrainEntityHolder(new Terrain(4001, 115));
        game.getHolderGrid().get(118).setTerrainEntityHolder(new Terrain(4002, 118));
        game.getHolderGrid().get(121).setTerrainEntityHolder(new Terrain(4003, 121));
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

//    public GameBoard getBoard() {
//        GameBoard board = new GameBoard(width, height, wrapHorizontal, wrapVertical);
//        for (int x = 0; x < width; x++) {
//            for (int y = 0; y < height; y++) {
//                TerrainType terrain = customTerrain[x][y] != null ? customTerrain[x][y] : baseTerrain;
//                board.setTerrain(x, y, terrain);
//            }
//        }
//        return board;
//    }
}

