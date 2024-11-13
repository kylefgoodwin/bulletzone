package edu.unh.cs.cs619.bulletzone.model;

import java.util.ArrayList;

public class GameBoard {

    private int rows;
    private int columns;
    private int position;
    private final Object monitor = new Object();
    private final ArrayList<FieldHolder> holderGrid;

    public GameBoard(int rows, int columns, int position) {
        this.rows = rows;
        this.columns = columns;
        this.position = position;
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

    public void createFieldHolderGrid() {
        synchronized (this.monitor) {
            holderGrid.clear();
            for (int i = 0; i < rows * columns; i++) {
                holderGrid.add(new FieldHolder(position));
            }

            FieldHolder targetHolder;
            FieldHolder rightHolder;
            FieldHolder downHolder;

            // Build connections
            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < columns; j++) {
                    int targetIndex = i * (rows / 3) + j;
                    targetHolder = holderGrid.get(targetIndex);

                    // ////////////INSERT DIFFERENT MAP CONDITIONS HERE//////////////////

                    int rightHolderIndex = (i * rows / 3 + ((j + 1) % columns));
                    int downHolderIndex = ((i + 1) % rows) * columns + j;

                    rightHolder = holderGrid.get(rightHolderIndex);
                    downHolder = holderGrid.get(downHolderIndex);

                    targetHolder.addNeighbor(Direction.Right, rightHolder);
                    rightHolder.addNeighbor(Direction.Left, targetHolder);

                    targetHolder.addNeighbor(Direction.Down, downHolder);
                    downHolder.addNeighbor(Direction.Up, targetHolder);
                }
            }
        }
    }

}
