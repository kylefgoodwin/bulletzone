package edu.unh.cs.cs619.bulletzone.model;

import java.util.ArrayDeque;
import java.util.Deque;

public class PowerUpManager {
    private final Deque<Item> powerUps = new ArrayDeque<>();
    private final int baseMovementDelay;
    private final int baseFireDelay;
    private int currentMovementDelay;
    private int currentFireDelay;

    public PowerUpManager(int baseMovementDelay, int baseFireDelay) {
        this.baseMovementDelay = baseMovementDelay;
        this.baseFireDelay = baseFireDelay;
        this.currentMovementDelay = baseMovementDelay;
        this.currentFireDelay = baseFireDelay;
    }

    public void addPowerUp(Item powerUp) {
        powerUps.addLast(powerUp);
        recalculateDelays();
    }

    public Item ejectLastPowerUp() {
        if (!powerUps.isEmpty()) {
            Item powerUp = powerUps.removeLast();
            recalculateDelays();
            return powerUp;
        }
        return null;
    }

    private void recalculateDelays() {
        // Reset to base values
        currentMovementDelay = baseMovementDelay;
        currentFireDelay = baseFireDelay;

        // Apply each power-up's effects in order
        for (Item powerUp : powerUps) {
            if (powerUp.isAntiGrav()) {
                currentMovementDelay = currentMovementDelay / 2;  // Double speed (half delay)
                currentFireDelay += 100;  // Add 0.1s to fire rate
            } else if (powerUp.isFusionReactor()) {
                currentFireDelay = currentFireDelay / 2;  // Double fire rate (half delay)
                currentMovementDelay += 100;  // Add 0.1s to movement
            }
        }
    }

    public int getCurrentMovementDelay() {
        return currentMovementDelay;
    }

    public int getCurrentFireDelay() {
        return currentFireDelay;
    }

    public boolean hasPowerUps() {
        return !powerUps.isEmpty();
    }

    public int getPowerUpCount() {
        return powerUps.size();
    }

    // For UI display
    public int getAntiGravCount() {
        return (int) powerUps.stream().filter(Item::isAntiGrav).count();
    }

    public int getFusionReactorCount() {
        return (int) powerUps.stream().filter(Item::isFusionReactor).count();
    }
}