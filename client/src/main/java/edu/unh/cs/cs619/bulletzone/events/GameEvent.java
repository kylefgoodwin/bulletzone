package edu.unh.cs.cs619.bulletzone.events;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.io.Serializable;
import java.util.Comparator;

import edu.unh.cs.cs619.bulletzone.util.ReplayData;
import edu.unh.cs.cs619.bulletzone.util.ReplayDataFlat;

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
        @JsonSubTypes.Type(name = "hit", value = HitEvent.class),
        @JsonSubTypes.Type(name = "turn", value = TurnEvent.class),
        @JsonSubTypes.Type(name = "itemPickup", value = ItemPickupEvent.class),
        @JsonSubTypes.Type(name = "UI", value = UIUpdateEvent.class),
        @JsonSubTypes.Type(name = "terrain", value = TerrainUpdateEvent.class),
        @JsonSubTypes.Type(name = "Healing", value = HealingEvent.class),
        @JsonSubTypes.Type(name = "powerUpEject", value = HealingEvent.class)
})
public abstract class GameEvent implements Serializable {
    private static final long serialVersionUID = 1L;
    private long timeStamp;
    private long deltaTimeStamp;
    private final static Long lock = new Long(0L);

    protected GameEvent() {
        synchronized (lock) {
            timeStamp = System.currentTimeMillis();
            deltaTimeStamp = timeStamp - (ReplayData.getReplayData().getInitialTimestamp());
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