package edu.unh.cs.cs619.bulletzone.util;
import org.javatuples.Pair;
/**
 * Created by simon on 10/1/14.
 */
public class PlayableWrapper {
    private long result1;
    private long result2;

    public PlayableWrapper(long result1, long result2) {
        this.result1 = result1;
        this.result2 = result2;
    }

    // Get both results as a tuple
    public Pair<Long, Long> getResult() {
        return new Pair<>(result1, result2);
    }

    // Set both results using a tuple
    public void setResult(Pair<Long, Long> results) {
        this.result1 = results.getValue0();
        this.result2 = results.getValue1();
    }
}
