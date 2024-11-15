package edu.unh.cs.cs619.bulletzone.web;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.greenrobot.eventbus.EventBus;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collection;

import edu.unh.cs.cs619.bulletzone.BulletZoneServer;
import edu.unh.cs.cs619.bulletzone.model.Game;
import edu.unh.cs.cs619.bulletzone.model.events.EventHistory;
import edu.unh.cs.cs619.bulletzone.model.events.GameEvent;
import edu.unh.cs.cs619.bulletzone.model.events.SpawnEvent;
import edu.unh.cs.cs619.bulletzone.repository.GameRepository;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {BulletZoneServer.class})
public class EventHistoryTest {

    private MockMvc mockMvc;

    @Mock
    private GameRepository gameRepository;

    @InjectMocks
    private GameStateController gameStateController;

    private EventHistory eventHistory;
    private Game mockGame;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);

        // Create and set up mock game and repository
        mockGame = mock(Game.class);
        when(gameRepository.getGame()).thenReturn(mockGame);

        // Create controller with mocked dependencies
        gameStateController = new GameStateController(gameRepository);

        mockMvc = MockMvcBuilders.standaloneSetup(gameStateController).build();

        // Get EventHistory instance and clear it
        eventHistory = EventHistory.getInstance();
        eventHistory.clearHistory();

        // Register with EventBus if not already registered
        EventBus eventBus = EventBus.getDefault();
        if (!eventBus.isRegistered(eventHistory)) {
            eventBus.register(eventHistory);
        }
    }

    @Test
    public void EventHistory_SendEvent_AddsEventToEventHistory() throws Exception {
        // Create and set up the game event
        SpawnEvent gameEvent = new SpawnEvent();
        gameEvent.setTimeStamp(System.currentTimeMillis());

        // Post event to EventBus
        EventBus.getDefault().post(gameEvent);
        Thread.sleep(100); // Allow time for event processing

        // Verify event was added
        Collection<GameEvent> history = eventHistory.getHistory();
        assertTrue("Event should be added to history", historyContainsEvent(history, gameEvent));
    }

    @Test
    public void EventHistory_SendEvents_AddsMultipleEventsToEventHistory() throws Exception {
        // Create and post multiple events
        int eventCount = 50;
        SpawnEvent[] events = new SpawnEvent[eventCount];

        for (int i = 0; i < eventCount; i++) {
            events[i] = new SpawnEvent();
            events[i].setTimeStamp(System.currentTimeMillis() + i); // Ensure unique timestamps
            EventBus.getDefault().post(events[i]);
        }

        // Give more time for event processing
        Thread.sleep(500);

        Collection<GameEvent> history = eventHistory.getHistory();

        // Check that all events are in the history
        for (SpawnEvent event : events) {
            assertTrue("Event should be in history: " + event.getTimeStamp(),
                    historyContainsEvent(history, event));
        }
    }

    @Test
    public void EventHistory_getHistory_TrimsOldEvents() throws Exception {
        long currentTime = System.currentTimeMillis();

        // Create events with different timestamps
        GameEvent recentEvent = new SpawnEvent();
        recentEvent.setTimeStamp(currentTime - 25000);

        GameEvent oldEvent1 = new SpawnEvent();
        oldEvent1.setTimeStamp(currentTime - 130000);

        GameEvent oldEvent2 = new SpawnEvent();
        oldEvent2.setTimeStamp(currentTime - 150000);

        // Post events
        EventBus.getDefault().post(recentEvent);
        EventBus.getDefault().post(oldEvent1);
        EventBus.getDefault().post(oldEvent2);

        Thread.sleep(200); // Allow time for event processing

        Collection<GameEvent> history = eventHistory.getHistory();
        assertTrue("Recent event should be in history", historyContainsEvent(history, recentEvent));
        assertFalse("Old event should be trimmed", historyContainsEvent(history, oldEvent1));
        assertFalse("Old event should be trimmed", historyContainsEvent(history, oldEvent2));
    }

    @Test
    public void EventHistory_getHistoryQuery_TrimsEventsSinceTimeRequested() throws Exception {
        long currentTime = System.currentTimeMillis();

        // Create events with different timestamps
        GameEvent event1 = new SpawnEvent();
        event1.setTimeStamp(currentTime - 25000);

        GameEvent event2 = new SpawnEvent();
        event2.setTimeStamp(currentTime - 30000);

        GameEvent event3 = new SpawnEvent();
        event3.setTimeStamp(currentTime - 50000);

        // Post events
        EventBus.getDefault().post(event1);
        EventBus.getDefault().post(event2);
        EventBus.getDefault().post(event3);

        Thread.sleep(200); // Allow time for event processing

        Collection<GameEvent> history = eventHistory.getHistory(currentTime - 35000);
        assertTrue("Recent event should be included", historyContainsEvent(history, event1));
        assertTrue("Event within time range should be included", historyContainsEvent(history, event2));
        assertFalse("Old event should be excluded", historyContainsEvent(history, event3));
    }

    // Helper method to check if history contains a specific event
    private boolean historyContainsEvent(Collection<GameEvent> history, GameEvent event) {
        return history.stream()
                .anyMatch(e -> e.getTimeStamp() == event.getTimeStamp());
    }
}