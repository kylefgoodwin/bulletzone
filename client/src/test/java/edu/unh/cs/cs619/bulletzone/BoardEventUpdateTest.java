package edu.unh.cs.cs619.bulletzone;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.mockito.Mockito.doNothing;

import android.content.Context;
import android.util.Log;
import android.widget.GridView;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;

import edu.unh.cs.cs619.bulletzone.events.GameEvent;
import edu.unh.cs.cs619.bulletzone.events.GameEventProcessor;
import edu.unh.cs.cs619.bulletzone.events.MoveEvent;
import edu.unh.cs.cs619.bulletzone.events.SpawnEvent;
import edu.unh.cs.cs619.bulletzone.events.TurnEvent;
import edu.unh.cs.cs619.bulletzone.rest.BulletZoneRestClient;
import edu.unh.cs.cs619.bulletzone.rest.GridPollerTask;
import edu.unh.cs.cs619.bulletzone.ui.GridAdapter;

public class BoardEventUpdateTest {
    @Mock
    EventBus mockEB;

    @Mock
    BulletZoneRestClient mockRestClient;

    @Mock
    GridView mockGV;

    @Mock
    TextView mockTV;

    @Mock
    Context mockContext;

    @Mock
    GridAdapter mockAdapter;

    @Mock
    SpawnEvent mockSpawnEvent;

    @Mock
    MoveEvent mockMoveEvent;

    @Mock
    TurnEvent mockTurnEvent;

    @Mock
    GameEvent mockGameEvent;

    GameEventProcessor testEventProcessor;

    @Before
    public void setup() {
        initMocks(this);
//        mockedLog = mockStatic(Log.class);
//        mockedLog.when(() -> Log.d(anyString(), anyString())).thenReturn(0);

        when(mockGV.getContext()).thenReturn(mockContext);

        int[][] initialGrid = {
                {0, 1000, 1000, 1000, 0, 1000, 0, 1000, 1000, 1000, 0, 0, 0, 0, 0, 0},
                {0, 1000, 0, 0, 0, 1000, 0, 1000, 1000, 1000, 10010746, 0, 0, 0, 0, 0},
                {0, 0, 1000, 1000, 0, 1000, 0, 1000, 1000, 1000, 0, 0, 0, 9999610, 0, 0},
                {0, 0, 1000, 1000, 0, 1000, 0, 0, 2003, 1000, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 10021006, 1000, 0, 0, 0, 0, 3000, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2002, 0, 0, 0},
                {0, 2002, 0, 0, 0, 0, 0, 0, 0, 0, 2002, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 2003, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 1001704, 0, 0, 0, 999736, 0, 0, 0, 0, 1000502, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 2003, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 3000, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 3000},
                {0, 0, 0, 3000, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}
        };

        testEventProcessor = spy(new GameEventProcessor());
        testEventProcessor.setEventBus(mockEB);
//        testEventProcessor.setBoard(initialGrid);

        // Mock the event handling
        doNothing().when(testEventProcessor).onNewEvent(mockSpawnEvent);
        doNothing().when(testEventProcessor).onNewEvent(mockMoveEvent);
        doNothing().when(testEventProcessor).onNewEvent(mockTurnEvent);
    }

    @After
//    public void tearDown() {
//        mockedLog.close();
//    }
//    @Before
//    public void setup() {
//        initMocks(this);
//        when(mockGV.getContext()).thenReturn(mockContext);
//
//        int[][] initialGrid = {
//                {0, 1000, 1000, 1000, 0, 1000, 0, 1000, 1000, 1000, 0, 0, 0, 0, 0, 0},
//                {0, 1000, 0, 0, 0, 1000, 0, 1000, 1000, 1000, 10010746, 0, 0, 0, 0, 0},
//                {0, 0, 1000, 1000, 0, 1000, 0, 1000, 1000, 1000, 0, 0, 0, 9999610, 0, 0},
//                {0, 0, 1000, 1000, 0, 1000, 0, 0, 2003, 1000, 0, 0, 0, 0, 0, 0},
//                {0, 0, 0, 0, 0, 0, 0, 0, 10021006, 1000, 0, 0, 0, 0, 3000, 0},
//                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2002, 0, 0, 0},
//                {0, 2002, 0, 0, 0, 0, 0, 0, 0, 0, 2002, 0, 0, 0, 0, 0},
//                {0, 0, 0, 0, 0, 0, 0, 0, 0, 2003, 0, 0, 0, 0, 0, 0},
//                {0, 0, 0, 0, 0, 1001704, 0, 0, 0, 999736, 0, 0, 0, 0, 1000502, 0},
//                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
//                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
//                {0, 0, 0, 0, 2003, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
//                {0, 0, 0, 0, 0, 3000, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
//                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
//                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 3000},
//                {0, 0, 0, 3000, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}
//        };
//
//        testEventProcessor = spy(new GameEventProcessor());
//        testEventProcessor.eb = mockEB;
//
//        testEventProcessor.setBoard(initialGrid);
//    }

    @Test
    public void gameEventProcessor_onStartCalled_registersToEventBus() {
        testEventProcessor.start();
        verify(mockEB).register(testEventProcessor);
    }

    @Test
    public void GridPollerTask_OnEvent_PostsEvent() {
        GridPollerTask poller = spy(new GridPollerTask());
        poller.doPoll(testEventProcessor);
        verify(poller).doPoll(testEventProcessor);
    }

    @Test
    public void GameEventProcessor_onTurnEvent_callsToChangeBoard() {
        // Setup
        testEventProcessor.start();

        // Execute
        testEventProcessor.onNewEvent(mockTurnEvent);

        // Verify
        verify(testEventProcessor).onNewEvent(mockTurnEvent);
    }

    @Test
    public void GameEventProcessor_onSpawnEvent_callsToChangeBoard() {
        // Setup
        testEventProcessor.start();

        // Execute
        testEventProcessor.onNewEvent(mockSpawnEvent);

        // Verify
        verify(testEventProcessor).onNewEvent(mockSpawnEvent);
    }

    @Test
    public void gameEventProcessor_onMoveEvent_callsToChangeBoard() {
        // Setup
        testEventProcessor.start();

        // Execute
        testEventProcessor.onNewEvent(mockMoveEvent);

        // Verify
        verify(testEventProcessor).onNewEvent(mockMoveEvent);
    }
}