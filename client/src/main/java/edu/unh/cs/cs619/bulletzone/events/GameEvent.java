package edu.unh.cs.cs619.bulletzone.events;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.util.Comparator;

import edu.unh.cs.cs619.bulletzone.util.ReplayData;

//This class is adapted from group Alpha's project from 2020, courtesy Gersi Doko
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(name = "move", value = MoveEvent.class),
        @JsonSubTypes.Type(name = "spawn", value = SpawnEvent.class),
        @JsonSubTypes.Type(name = "remove", value = RemoveEvent.class),
        @JsonSubTypes.Type(name = "turn", value = TurnEvent.class)
})
public abstract class GameEvent {
    private long timeStamp;
    private long deltaTimeStamp;
    private final static Object lock = new Object();
    ReplayData replayData = ReplayData.getReplayData();

    /**
     * Constructor of events of a specified type.
     */
    protected GameEvent() {
        synchronized (lock) {
            timeStamp = System.currentTimeMillis();
            deltaTimeStamp = timeStamp - replayData.getInitialTimestamp();
        }
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long newTime) {
        this.timeStamp = newTime;
    }

    public long getDeltaTimeStamp() {
        return deltaTimeStamp;
    }

    /**
     * This is how two events are compared for sorting of events by timestamp.
     * (earlier time stamps come first)
     */
    public static Comparator<GameEvent> eventComparator = (e1, e2) -> {
        Long e1Time = e1.getTimeStamp();
        Long e2Time = e2.getTimeStamp();

        //ascending order
        return e1Time.compareTo(e2Time);
    };

    abstract void applyTo(int [][]board);

    @Override
    public String toString() {
        return "@" + timeStamp;
    }
}
