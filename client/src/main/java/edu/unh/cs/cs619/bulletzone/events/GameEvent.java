package edu.unh.cs.cs619.bulletzone.events;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.util.Comparator;

//This class is adapted from group Alpha's project from 2020, courtesy Gersi Doko
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(name = "move", value = MoveEvent.class),
        @JsonSubTypes.Type(name = "spawn", value = SpawnEvent.class),
        @JsonSubTypes.Type(name = "remove", value = RemoveEvent.class),
        @JsonSubTypes.Type(name = "turn", value = TurnEvent.class),
        @JsonSubTypes.Type(name = "hit", value = HitEvent.class)
})
public abstract class GameEvent {
    private long timeStamp;
    private final static Object lock = new Object();

    /**
     * Constructor of events of a specified type.
     */
    protected GameEvent() {
        synchronized (lock) {
            timeStamp = System.currentTimeMillis();
        }
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long newTime) {
        this.timeStamp = newTime;
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
