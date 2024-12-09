package edu.unh.cs.cs619.bulletzone.model;

public class BoardCellFactory {

    /**
     * Takes the rawServerValue, row, and column of a cell in the board and
     * builds that cell based on what type it should be.
     * Returns the built cell to the SimulationBoard
     */
    public BoardCell makeCell(int val, int row, int col) {
        if (val >= 50000000) {
            return new BoardCell(val, row, col);
        } else if (val >= 40000000 && val < 50000000) {
            return new TurnableShip(val, row, col);
        } else if (val >= 30000000 && val < 40000000) {
            return new TurnableSoldier(val, row, col);
        } else if (val >= 20000000 && val < 30000000){
            return new TurnableBuilder(val, row, col);
        } else if (val >= 10000000 && val < 20000000) {
            return new TurnableGoblin(val, row, col);
        } else if (val >= 2000000 && val < 3000000) {
            return new TankItem(val, row, col);
        } else if (val >= 4000 && val <= 4004) { //include water
            return new Terrain(val, row, col);
        } else if (val >= 3001 && val <= 3005){  // Updated to include new power-ups
            return new PowerUp(val, row, col);
        } else if (val >= 2000) {
            return new BoardCell(val, row, col);
        } else if (val < 2000 && val >= 1000) {
            return new Wall(val, row, col);
        } else if (val == 920){
            return new MiningFacility(val, row, col);
        } else {
            return new BoardCell(val, row, col);
        }
    }
}