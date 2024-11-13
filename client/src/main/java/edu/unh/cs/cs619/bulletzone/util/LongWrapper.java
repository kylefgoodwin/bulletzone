package edu.unh.cs.cs619.bulletzone.util;

/**
 * Created by simon on 10/1/14.
 */
public class LongWrapper {
    private long result;
    public long tankId;
    public long builderId;

    public LongWrapper() {
        this.tankId = tankId;
        this.builderId = builderId;
    }

    public LongWrapper(long result) {
        this.result = result;
    }

    public long getTankId() {return this.tankId;}

    public void setTankId(long tankId) {this.tankId = tankId;}

    public long getBuilderId() {return this.builderId;}

    public void setBuilderId(long builderId) {this.builderId = builderId;}

    public long getResult() {
        return result;
    }

    public void setResult(long result) {
        this.result = result;
    }
}
