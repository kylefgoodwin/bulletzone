package edu.unh.cs.cs619.bulletzone.events;

import java.io.Serializable;

import edu.unh.cs.cs619.bulletzone.rest.GridPollerTask;

public class MiningCreditsEvent implements Serializable {
    private static final long serialVersionUID = 1L;
    private final long timeStamp;
    private final long ownerId;
    private final double creditAmount;

    public MiningCreditsEvent(long timeStamp, long ownerId, double creditAmount) {
        this.timeStamp = timeStamp;
        this.ownerId = ownerId;
        this.creditAmount = creditAmount;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public long getOwnerId() {
        return ownerId;
    }

    public double getCreditAmount() {
        return creditAmount;
    }
}
