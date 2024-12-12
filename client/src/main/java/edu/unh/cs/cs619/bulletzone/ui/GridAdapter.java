package edu.unh.cs.cs619.bulletzone.ui;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.SystemService;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import edu.unh.cs.cs619.bulletzone.R;
import edu.unh.cs.cs619.bulletzone.events.UpdateBoardEvent;
import edu.unh.cs.cs619.bulletzone.model.BoardCell;
import edu.unh.cs.cs619.bulletzone.model.BoardCellBlock;
import edu.unh.cs.cs619.bulletzone.model.SimulationBoard;

@EBean
public class GridAdapter extends BaseAdapter {

    private final Object monitor = new Object();
    private static final String TAG = "GridAdapter";

    @SystemService
    protected LayoutInflater inflater;

    private int[][] mEntities = new int[16][16];
    private int[][] terrainEntities = new int[16][16];
    private SimulationBoard simBoard;
    public boolean isUpdated = false;
    private long tankId = -1;
    private boolean isTerrainView = false;
    private long builderId = -1;
    private long soldierId = -1;

    @AfterInject
    protected void afterInject() {
        // Initialize SimulationBoard before registering for events
        simBoard = new SimulationBoard(16, 16);
        EventBus.getDefault().register(this);
        //Log.d(TAG, "GridAdapter initialized with new SimulationBoard");
    }

    public void setTerrainView(boolean isTerrainView) {
        this.isTerrainView = isTerrainView;
    }

    /**
     * Updates the entities array of new input after events have changed it from the server
     *
     * @param entities Game board array
     */
    public void updateList(int[][] entities, int[][] tEntities) {
        synchronized (monitor) {
            // Add debug logging
            boolean foundPowerUp = false;
            for (int i = 0; i < entities.length; i++) {
                for (int j = 0; j < entities[i].length; j++) {
                    if (entities[i][j] >= 3000 && entities[i][j] <= 3003) {
                        foundPowerUp = true;
                        //Log.d(TAG, "Power-up found at position [" + i + "," + j + "]: " + entities[i][j]);
                    }
                }
            }
            if (!foundPowerUp) {
                //Log.d(TAG, "No power-ups found in update");
            }

            this.mEntities = entities;
            this.terrainEntities = tEntities;
            if (simBoard != null) {
                simBoard.setUsingBoard(mEntities, tEntities);
            } else {
                Log.e(TAG, "SimulationBoard is null in updateList, creating new instance");
                simBoard = new SimulationBoard(16, 16);
                simBoard.setUsingBoard(mEntities, tEntities);
            }
            this.notifyDataSetChanged();
            this.isUpdated = true;
        }
    }

    /**
     * Subscribes to changes from events from the UpdateBoardEvent and updates the view
     *
     * @param event New event made to the board
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void handleUpdate(UpdateBoardEvent event) {
        if (simBoard != null) {
            simBoard.setUsingBoard(mEntities, terrainEntities);
            Log.d(TAG, "Setting simboard using Board");
        } else {
            Log.e(TAG, "SimulationBoard is null in handleUpdate, creating new instance");
            simBoard = new SimulationBoard(16, 16);
            simBoard.setUsingBoard(mEntities, terrainEntities);
        }
        this.notifyDataSetChanged();
        simBoard.setUsingBoard(mEntities, terrainEntities); // Updates simulation board when events are posted
        this.isUpdated = true;
    }

    public void setSimBoard(SimulationBoard board) {
        if (board != null) {
            this.simBoard = board;
            if (mEntities != null) {
                simBoard.setUsingBoard(mEntities, terrainEntities);
            }
            //Log.d(TAG, "New SimulationBoard set successfully");
        } else {
            Log.e(TAG, "Attempted to set null SimulationBoard");
        }
    }

    public int[][] getBoard() {
        return mEntities;
    }

    @Override
    public int getCount() {
        return 16 * 16;
    }

    @Override
    public Object getItem(int position) {
        return mEntities[position / 16][position % 16];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void setTankId(long tankId) {
        this.tankId = tankId;
    }

    public void setBuilderId(long builderId) {
        this.builderId = builderId;
    }

    public void setSoldierId(long builderId) {
        this.soldierId = soldierId;
    }

    /**
     * Updates the desired cell from events in the gridView, using SimulationBoard's Board Cells
     *
     * @param position    The position in the SimulationBoard to be updated
     * @param convertView The view to be updated, it is gridView
     * @param parent      The parent activity of the view, Client Activity
     * @return Returns the updated view
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView;

        if (convertView == null) {
            imageView = new ImageView(parent.getContext());
            imageView.setLayoutParams(new GridView.LayoutParams(50, 50));
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setPadding(1, 1, 1, 1);
        } else {
            imageView = (ImageView) convertView;
        }

        if (this.isUpdated && simBoard != null) {
            int value = mEntities[position / 16][position % 16];
            BoardCellBlock currCell = simBoard.getCell(position);
            BoardCell playerCell = currCell.getPlayerData();
            BoardCell itemCell = currCell.getItemData();
            BoardCell terrainCell = currCell.getTerrainData();

//            Log.d(TAG, "isTerrainGrid: " + isTerrainView);
//            Log.d(TAG, "Terrain Cell: " + terrainCell.getCellType());


            if (!isTerrainView) {
//                Log.d(TAG, "Setting Tank Cell");
                // Handle tanks
                 if (playerCell.getCellType().equals("Tank")) {
                    int tankIdTest = (playerCell.getRawValue() / 10000) - 1000;
                    if (tankIdTest == this.tankId) {
                        imageView.setImageResource(R.drawable.goblin_rider_player);
                    } else { // Else set it to what it should be
                        imageView.setImageResource(playerCell.getResourceID());
                    }
                } else if (playerCell.getCellType().equals("Builder")) {
                    int builderIdTest = (playerCell.getRawValue() / 10000) - 1000;
                    if (builderIdTest == this.tankId) {
                        imageView.setImageResource(R.drawable.goblin_builder_player);
                    } else {
                        imageView.setImageResource(playerCell.getResourceID());
                    }
                } else if (playerCell.getCellType().equals("Soldier")) {
                     int soldierIdTest = (playerCell.getRawValue() / 10000) - 1000;
                     if (soldierIdTest == this.tankId) {
                         imageView.setImageResource(R.drawable.goblin_soldier_player);
                     } else {
                         imageView.setImageResource(playerCell.getResourceID());
                     }
                 } else if (playerCell.getCellType().equals("Ship")) {
                     int shipIdTest = (playerCell.getRawValue() / 10000) - 1000;
                     if (shipIdTest == this.tankId) {
                         imageView.setImageResource(R.drawable.ship);
                     } else {
                         imageView.setImageResource(playerCell.getResourceID());
                     }
                 } else {
                    if (playerCell.getCellType().equals("Empty")) {
                        imageView.setImageResource(android.R.color.transparent);
                    } else {
                        imageView.setImageResource(playerCell.getResourceID());
                    }
                }

                imageView.setRotation(playerCell.getRotation());
            } else {
//                Log.d(TAG, "Setting Terrain Cell");
//                Log.d(TAG, "Terrain Cell: " + terrainCell.getCellType());
                imageView.setImageResource(terrainCell.getResourceID());
//                imageView.setVisibility(View.VISIBLE);
            }

        } else {
            imageView.setImageResource(R.drawable.clear);
            if (simBoard == null) {
                Log.e(TAG, "SimulationBoard is null in getView");
            }
        }

        return imageView;
    }

    /**
     * Clean up when the adapter is no longer needed
     */
    public void onDestroy() {
        EventBus.getDefault().unregister(this);
        //Log.d(TAG, "GridAdapter destroyed and unregistered from EventBus");
    }
}