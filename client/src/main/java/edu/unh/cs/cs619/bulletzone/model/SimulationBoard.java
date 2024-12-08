package edu.unh.cs.cs619.bulletzone.model;

import edu.unh.cs.cs619.bulletzone.events.GameEventProcessor;

/**
 * SimulationBoard Class.
 * Holds Cell information about all cells in board at a time which is known from updates in GridAdapter
 * from event updates to board int array.
 * Sam Harris 10/19/2024.
 */
public class SimulationBoard extends GameEventProcessor {
    private int numRows, numCols;
    private BoardCellBlock[] cells;
    private BoardCell badCell = new BoardCell(0, -1, -1);
    private BoardCellBlock badCellBlock = new BoardCellBlock(badCell, badCell, badCell);

    public SimulationBoard(int rows, int cols) {
        numRows = rows;
        numCols = cols;
        cells = new BoardCellBlock[numRows * numCols];
    }

    public BoardCellBlock getCell(int index) {
        if (index < 0 || index >= numRows * numCols || cells[index] == null)
            return badCellBlock;
        return cells[index];
    }

    public BoardCellBlock getCell(int row, int col) {
        return getCell(numRows * numCols + col);
    }

    public int getNumRows() { return numRows; }
    public int getNumCols() { return numCols; }

    public void setCell(int index, BoardCellBlock cell) {
        if (index >= 0 && index < numRows * numCols)
            cells[index] = cell;
    }

    public void setCell(int row, int col, BoardCellBlock cell) {
        setCell(row * numCols + col, cell);
    }

    public int size() {return numRows * numCols;}

    /**
     * Called in GridAdapter from listener to Event Bus and sets Simulation Board based on int array board.
     * @param board Integer array board used by events.
     */
    public void setUsingBoard(int[][] board, int[][] tBoard) {
//        Log.d("SimBoard", "Setting Simulation Board");
        int index = 0;
        BoardCellFactory factory = new BoardCellFactory();

        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                // Create BoardCells for player, item, and terrain data
                BoardCell playerCell = factory.makeCell(board[i][j], i, j);
//                BoardCell itemCell = factory.makeCell(board[i][j + 1], i, j);
                BoardCell terrainCell = factory.makeCell(tBoard[i][j], i, j);

                // Create a new BoardCellBlock with the created BoardCells
                BoardCellBlock newBlock = new BoardCellBlock(playerCell, badCell, terrainCell);

                setCell(index, newBlock);
                index++;
            }
        }
    }
}
