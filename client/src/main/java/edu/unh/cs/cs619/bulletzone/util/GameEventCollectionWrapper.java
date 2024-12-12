package edu.unh.cs.cs619.bulletzone.util;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;

import java.util.Collection;

import edu.unh.cs.cs619.bulletzone.events.GameEvent;
import edu.unh.cs.cs619.bulletzone.events.HitEvent;
import edu.unh.cs.cs619.bulletzone.events.ItemPickupEvent;
import edu.unh.cs.cs619.bulletzone.events.MoveEvent;
import edu.unh.cs.cs619.bulletzone.events.PowerUpEjectEvent;
import edu.unh.cs.cs619.bulletzone.events.RemoveEvent;
import edu.unh.cs.cs619.bulletzone.events.SpawnEvent;
import edu.unh.cs.cs619.bulletzone.events.TerrainUpdateEvent;
import edu.unh.cs.cs619.bulletzone.events.TurnEvent;
import edu.unh.cs.cs619.bulletzone.events.UIUpdateEvent;

public class GameEventCollectionWrapper {
    @JsonTypeInfo(
            use = JsonTypeInfo.Id.NAME,
            include = As.PROPERTY,
            property = "@type"
    )
    @JsonSubTypes({
            @JsonSubTypes.Type(value = HitEvent.class, name = "hit"),
            @JsonSubTypes.Type(value = ItemPickupEvent.class, name = "itemPickup"),
            @JsonSubTypes.Type(value = MoveEvent.class, name = "move"),
            @JsonSubTypes.Type(value = RemoveEvent.class, name = "remove"),
            @JsonSubTypes.Type(value = SpawnEvent.class, name = "spawn"),
            @JsonSubTypes.Type(value = TurnEvent.class, name = "turn"),
            @JsonSubTypes.Type(value = TerrainUpdateEvent.class, name = "terrain"),
            @JsonSubTypes.Type(value = UIUpdateEvent.class, name = "UI"),
            @JsonSubTypes.Type(value = PowerUpEjectEvent.class, name = "powerUpEject")
    })
    private Collection<GameEvent> events;

    public GameEventCollectionWrapper() {}

    public GameEventCollectionWrapper(Collection<GameEvent> input) {
        this.events = input;
    }

    public Collection<GameEvent> getEvents() {
        return this.events;
    }

    public void setEvents(Collection<GameEvent> set) {
        this.events = set;
    }
}