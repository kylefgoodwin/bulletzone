package edu.unh.cs.cs619.bulletzone;

import android.util.Log;
import android.widget.GridView;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import edu.unh.cs.cs619.bulletzone.model.SimulationBoard;
import edu.unh.cs.cs619.bulletzone.rest.GridUpdateEvent;
import edu.unh.cs.cs619.bulletzone.ui.GridAdapter;
import edu.unh.cs.cs619.bulletzone.util.GridWrapper;
import edu.unh.cs.cs619.bulletzone.util.ReplayData;

@EBean
public class SimBoardView {
    private final SimulationBoard simBoard = new SimulationBoard(16,16);

    @Bean
    protected GridAdapter adapter;
    @Bean
    protected GridAdapter tAdapter;

    private PlayerData playerData = PlayerData.getPlayerData();
    private ReplayData replayData = ReplayData.getReplayData();

    public Object gridEventHandler = new Object() {
        @Subscribe
        public void onUpdateGrid(GridUpdateEvent event) {
            updateGrid(event.gw, event.tw);
        }
    };

    public void updateGrid(GridWrapper gw, GridWrapper tw) {
        adapter.updateList(gw.getGrid(), tw.getGrid());
        tAdapter.updateList(gw.getGrid(), tw.getGrid());
    }

    public void attach(GridView gView, GridView tGridView, Long tankID) {
        adapter.setSimBoard(simBoard);
        tAdapter.setSimBoard(simBoard);
        adapter.setTankId(tankID);

        adapter.setTerrainView(false);
        gView.setAdapter(adapter);

        tAdapter.setTerrainView(true);
        tGridView.setAdapter(tAdapter);
        EventBus.getDefault().register(gridEventHandler);
    }

    public void replayAttach(GridView gView, GridView tGridView) {
        adapter.setSimBoard(simBoard);
        tAdapter.setSimBoard(simBoard);
        adapter.setTankId(replayData.getPlayerTankID());

        adapter.setTerrainView(false);
        gView.setAdapter(adapter);

        tAdapter.setTerrainView(true);
        tGridView.setAdapter(tAdapter);
        EventBus.getDefault().register(gridEventHandler);
    }

    public void detach() {
        EventBus.getDefault().unregister(gridEventHandler);
    }

}
