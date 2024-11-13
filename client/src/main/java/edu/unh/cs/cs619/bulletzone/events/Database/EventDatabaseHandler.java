package edu.unh.cs.cs619.bulletzone.events.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;

import edu.unh.cs.cs619.bulletzone.events.GameEvent;
import edu.unh.cs.cs619.bulletzone.util.GridWrapper;

/*
 * This class allows you to interface with a SQLite database on the device in order to store previous games that have been played
 * The useful functions from this are the addEvent, addGrid, getGame, and removeGame functions
 * */

public class EventDatabaseHandler extends SQLiteOpenHelper {

    private static final String DB_NAME = "games";
    private static final String TABLE1_NAME = "events";
    private static final String TABLE2_NAME = "grids";
    private static final int DB_VERSION = 1;
    private static final String TIME_COL = "time";
    private static final String JSON_COL = "json";
    private static final String GAME_COL = "game";
    private static final String INDEX_COL = "array_index";
    private static final String VALUE_COL = "value";
    private ObjectMapper om;


    public EventDatabaseHandler(Context context){
        super(context, DB_NAME, null, DB_VERSION);
        om = new ObjectMapper();
    }

    @Override
    public void onCreate(SQLiteDatabase db){
        String query = "CREATE TABLE " + TABLE1_NAME + " ("
                + TIME_COL + " INTEGER, "
                + GAME_COL + " INTEGER, "
                + JSON_COL + " TEXT)";
        db.execSQL(query);
        query = "CREATE TABLE " + TABLE2_NAME + " ("
                + TIME_COL + " INTEGER, "
                + GAME_COL + " INTEGER, "
                + INDEX_COL + " INTEGER, "
                + VALUE_COL + " INTEGER)";
        db.execSQL(query);
    }

    /*
     * This function is meant to store the initial grid data associated with a certain game
     * The game argument is an integer id for the game you are storing the grid for
     * The grid is just a grid wrapper for the grid you are storing
     */
    public void addGrid(int game, GridWrapper grid){
        SQLiteDatabase db = this.getWritableDatabase();
        int[][] gridArray = grid.getGrid();
        for(int i=0;i<16;i++){
            for(int j=0;j<16;j++){
                ContentValues values = new ContentValues();
                values.put(TIME_COL, grid.getTimeStamp());
                values.put(GAME_COL, game);
                values.put(INDEX_COL, i*16+j);
                values.put(VALUE_COL, gridArray[i][j]);
                db.insert(TABLE2_NAME,null,values);
            }
        }
    }

    /*
     * This function is meant to store an event in the database
     * The time argument is the time associated with the event
     * The game argument is the id of the game the event occurred in
     * The game event is the event you are storing
     */
    public void addEvent(long time, int game, GameEvent ge) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(TIME_COL, time);
        values.put(GAME_COL, game);
        try {
            values.put(JSON_COL, om.writeValueAsString(ge));
        } catch (JsonProcessingException e) {
            Log.d("database", "bad insert");
            throw new RuntimeException(e);
        }
        db.insert(TABLE1_NAME,null,values);
    }

    /*
     * This function is meant to return the relevant data needed to replay a game
     * The game argument is the id of the game you'd like to replay
     * The output is a pair where the first element is the initial grid of the game and the second element is an ArrayList of game events in ascending order by the time that they occurred
     */
    public ArrayList<GameEvent> getEvents(int game) {
        String[] projection = {TIME_COL, GAME_COL, JSON_COL};
        String selection = GAME_COL + " = ?";
        String[] selectionArgs = {game + ""};
        String sortOrder = TIME_COL + " ASC";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE1_NAME, projection, selection, selectionArgs, null, null, sortOrder);
        ArrayList<GameEvent> events = new ArrayList<>();
        while (cursor.moveToNext()) {
            int jsonColIndex = cursor.getColumnIndex(JSON_COL);
            String jsonValue = cursor.getString(jsonColIndex);
            try {
                events.add(om.readValue(jsonValue, GameEvent.class));
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }
        cursor.close();
        return events;
    }
    public GridWrapper getGrid(int game){
        SQLiteDatabase db = this.getReadableDatabase();
        String[] projection2 = {TIME_COL, GAME_COL, INDEX_COL, VALUE_COL};
        String selection2 = GAME_COL + " = ?";
        String[] selectionArgs2 = {game+""};
        String sortOrder2 = TIME_COL + " ASC, " + INDEX_COL + " ASC";
        Cursor cursor2 = db.query(TABLE2_NAME, projection2, selection2, selectionArgs2, null, null, sortOrder2);
        int[][] outArray = new int[16][16];
        long time = 0;
        for(int i=0;i<256;i++) {
            cursor2.moveToNext();
            int valueColIndex = cursor2.getColumnIndex(VALUE_COL);
            int timeColIndex = cursor2.getColumnIndex(TIME_COL);
            time = cursor2.getLong(timeColIndex);
            outArray[i / 16][i % 16] = cursor2.getInt(valueColIndex);
        }
        cursor2.close();

        GridWrapper outGrid = new GridWrapper();
        outGrid.setGrid(outArray);
        outGrid.setTimeStamp(time);
        return outGrid;
    }

    /*
     * This function removes all the data associated with a certain game id from the database
     */
    public void removeGame(int game){
        SQLiteDatabase db = this.getWritableDatabase();
        String selection = GAME_COL + " LIKE ?";
        String[] selectionArgs = {game+""};
        db.delete(TABLE1_NAME,selection,selectionArgs);
        db.delete(TABLE2_NAME,selection,selectionArgs);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE1_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE2_NAME);
        onCreate(db);
    }

}
