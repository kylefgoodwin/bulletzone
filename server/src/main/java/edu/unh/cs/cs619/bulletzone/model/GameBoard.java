package edu.unh.cs.cs619.bulletzone.model;

import java.util.ArrayList;

public class GameBoard {

    private int rows;
    private int columns;
    private int position;
    private final Object monitor = new Object();
    private final ArrayList<FieldHolder> holderGrid;

    public GameBoard(int rows, int columns) {
        this.rows = rows;
        this.columns = columns;
        holderGrid = new ArrayList<>();
    }

    public void add(FieldHolder cell) {

    }

    public FieldHolder get(int x, int y) {
        return null;
    }

    public void setEntity(int x, int y, FieldEntity f) {

    }

    public ArrayList<FieldHolder> getHolderGrid() {
        return holderGrid;
    }

    public void createFieldHolderGrid(Game game) {
        synchronized (this.monitor) {
            game.getHolderGrid().clear();
            for (int i = 0; i < rows * columns; i++) {
                game.getHolderGrid().add(new FieldHolder(i));
            }

            FieldHolder targetHolder;
            FieldHolder rightHolder;
            FieldHolder downHolder;

            // Build connections
            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < columns; j++) {
                    targetHolder = game.getHolderGrid().get(i * rows + j);
                    rightHolder = game.getHolderGrid().get(i * rows
                            + ((j + 1) % columns));
                    downHolder = game.getHolderGrid().get(((i + 1) % rows)
                            * columns + j);

                    targetHolder.addNeighbor(Direction.Right, rightHolder);
                    rightHolder.addNeighbor(Direction.Left, targetHolder);

                    targetHolder.addNeighbor(Direction.Down, downHolder);
                    downHolder.addNeighbor(Direction.Up, targetHolder);
                }
            }
        }
    }

}
