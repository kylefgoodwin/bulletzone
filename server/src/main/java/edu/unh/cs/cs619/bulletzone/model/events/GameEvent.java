package edu.unh.cs.cs619.bulletzone.model.events;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

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
        @JsonSubTypes.Type(name = "Healing", value = HealingEvent.class)
})
public abstract class GameEvent {
    private long timeStamp;

    protected GameEvent() {
        timeStamp = System.currentTimeMillis();
    }

    public abstract void applyTo(int[][] board);

    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long newTime) {
        this.timeStamp = newTime;
    }

    @Override
    public String toString() {
        return "@" + timeStamp;
    }
}