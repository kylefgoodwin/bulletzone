package edu.unh.cs.cs619.bulletzone.model;

public class BoardCellBlock{
    public BoardCell playerData;
    public BoardCell itemData;
    public BoardCell terrainData;

    public BoardCellBlock(BoardCell player, BoardCell item, BoardCell terrain) {
        this.playerData = player;
        this.itemData = item;
        this.terrainData = terrain;
    }

    public BoardCell getTerrainData() {
        return terrainData;
    }

    public void setTerrainData(BoardCell terrainData) {
        this.terrainData = terrainData;
    }

    public BoardCell getItemData() {
        return itemData;
    }

    public void setItemData(BoardCell itemData) {
        this.itemData = itemData;
    }

    public BoardCell getPlayerData() {
        return playerData;
    }

    public void setPlayerData(BoardCell playerData) {
        this.playerData = playerData;
    }
}
