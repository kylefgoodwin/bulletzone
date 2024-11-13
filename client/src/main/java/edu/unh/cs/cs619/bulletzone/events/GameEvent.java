package edu.unh.cs.cs619.bulletzone.events;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import edu.unh.cs.cs619.bulletzone.util.ReplayData;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type"
)
@JsonSubTypes({
        @JsonSubTypes.Type(name = "move", value = MoveEvent.class),
        @JsonSubTypes.Type(name = "spawn", value = SpawnEvent.class),
        @JsonSubTypes.Type(name = "remove", value = RemoveEvent.class),
        @JsonSubTypes.Type(name = "turn", value = TurnEvent.class),
        @JsonSubTypes.Type(name = "itemPickup", value = ItemPickupEvent.class)
})
public abstract class GameEvent {
    private long timeStamp;
    private long deltaTimeStamp;
    private final static Object lock = new Object();
    ReplayData replayData = ReplayData.getReplayData();

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

    abstract void applyTo(int[][] board);

    @Override
    public String toString() {
        return "@" + timeStamp;
    }

    public long getDeltaTimeStamp() {
        return deltaTimeStamp;
    }
}