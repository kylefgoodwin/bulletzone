package edu.unh.cs.cs619.bulletzone.replay;

import static org.mockito.Mockito.verify;

import org.greenrobot.eventbus.EventBus;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;

import edu.unh.cs.cs619.bulletzone.events.GameEvent;
import edu.unh.cs.cs619.bulletzone.rest.GridReplayTask;
import edu.unh.cs.cs619.bulletzone.util.ReplayDataFlat;

@RunWith(MockitoJUnitRunner.class)
public class ReplayFileTests {

    private GridReplayTask gridReplayTask;
    @Mock
    private EventBus eventBusMock;
    @Mock
    private GameEvent testEvent;

    private List<ReplayDataFlat> testReplayList = new ArrayList<>();

    @Before
    public void setUp() {
        gridReplayTask = new GridReplayTask();
        gridReplayTask.setEventBus(eventBusMock);
        gridReplayTask.createTestEvent(testEvent);
    }

    @Test
    public void SaveReplayList_WhenCalled_CreatesReplayFile() {
        gridReplayTask.doReplay();
        verify(eventBusMock).post(testEvent);
    }
}
