package edu.unh.cs.cs619.bulletzone.model;

import java.util.ArrayDeque;
import java.util.Deque;

public class PowerUpManager {
    private final Deque<Item> powerUps = new ArrayDeque<>();
    private final int baseMovementDelay;
    private final int baseFireDelay;
    private int currentMovementDelay;
    private int currentFireDelay;
    private int shieldHealth;
    private long repairKitExpiration;
    private static final int MAX_SHIELD_HEALTH = 50;
    private static final int SHIELD_HEAL_RATE = 1;
    private static final int REPAIR_KIT_DURATION = 120000;
    private long lastShieldRegenTime;

    private int antiGravCount = 0;
    private int fusionReactorCount = 0;
    private int deflectorShieldCount = 0;
    private int repairKitCount = 0;

    public PowerUpManager(int baseMovementDelay, int baseFireDelay) {
        this.baseMovementDelay = baseMovementDelay;
        this.baseFireDelay = baseFireDelay;
        this.currentMovementDelay = baseMovementDelay;
        this.currentFireDelay = baseFireDelay;
        this.shieldHealth = 0;
        this.repairKitExpiration = 0;
        this.lastShieldRegenTime = System.currentTimeMillis();
    }

    public void addPowerUp(Item powerUp) {
        powerUps.addLast(powerUp);
        switch (powerUp.getType()) {
            case 2: // AntiGrav
                antiGravCount++;
                currentMovementDelay /= 2;
                currentFireDelay += 100;
                break;
            case 3: // FusionReactor
                fusionReactorCount++;
                currentFireDelay /= 2;
                currentMovementDelay += 100;
                break;
            case 4: // Shield
                deflectorShieldCount++;
                shieldHealth = MAX_SHIELD_HEALTH;
                lastShieldRegenTime = System.currentTimeMillis();
                break;
            case 5: // RepairKit
                repairKitCount++;
                repairKitExpiration = System.currentTimeMillis() + REPAIR_KIT_DURATION;
                break;
        }
        recalculateDelays();
    }

    public Item ejectLastPowerUp() {
        if (!powerUps.isEmpty()) {
            Item powerUp = powerUps.removeLast();
            switch (powerUp.getType()) {
                case 2: // AntiGrav
                    if (antiGravCount > 0) {
                        antiGravCount--;
                        currentMovementDelay = baseMovementDelay;
                        currentFireDelay = baseFireDelay;
                        recalculateDelays();
                    }
                    break;
                case 3: // FusionReactor
                    if (fusionReactorCount > 0) {
                        fusionReactorCount--;
                        currentMovementDelay = baseMovementDelay;
                        currentFireDelay = baseFireDelay;
                        recalculateDelays();
                    }
                    break;
                case 4: // Shield
                    if (deflectorShieldCount > 0) {
                        deflectorShieldCount--;
                        shieldHealth = 0;
                        if (deflectorShieldCount == 0) {
                            powerUps.removeIf(item -> item.getType() == 4);
                        }
                        recalculateDelays();
                        return null; // Don't drop shield item when ejected
                    }
                    break;
                case 5: // RepairKit
                    if (repairKitCount > 0) {
                        repairKitCount--;
                        if (repairKitCount == 0) {
                            repairKitExpiration = 0;
                            powerUps.removeIf(item -> item.getType() == 5);
                        }
                    }
                    break;
            }
            return powerUp;
        }
        return null;
    }

    private void recalculateDelays() {
        currentMovementDelay = baseMovementDelay;
        currentFireDelay = baseFireDelay;

        // Apply stacked AntiGrav effects
        for (int i = 0; i < antiGravCount; i++) {
            currentMovementDelay /= 2;
            currentFireDelay += 100;
        }

        // Apply stacked FusionReactor effects
        for (int i = 0; i < fusionReactorCount; i++) {
            currentFireDelay /= 2;
            currentMovementDelay += 100;
        }

        // Apply shield effects only if we have active shields
        if (deflectorShieldCount > 0 && shieldHealth > 0) {
            currentFireDelay = (int)(currentFireDelay * 1.5);
        }
    }

    public int processDamage(int incomingDamage) {
        if (shieldHealth > 0 && deflectorShieldCount > 0) {
            // Calculate reduced damage first
            int reducedDamage = incomingDamage / 2;

            // Apply full damage to shield
            shieldHealth = Math.max(0, shieldHealth - incomingDamage);

            // If shield breaks, remove all shield effects
            if (shieldHealth <= 0) {
                shieldHealth = 0;
                deflectorShieldCount = 0;
                powerUps.removeIf(item -> item.getType() == 4);
                recalculateDelays();
            }

            // Return the halved damage
            return reducedDamage;
        }
        return incomingDamage;
    }

    public void update() {
        long currentTime = System.currentTimeMillis();

        // Only regenerate shield if we have active shield power-ups
        if (shieldHealth > 0 && deflectorShieldCount > 0 &&
                shieldHealth < MAX_SHIELD_HEALTH &&
                currentTime - lastShieldRegenTime >= 1000) {
            shieldHealth = Math.min(MAX_SHIELD_HEALTH, shieldHealth + SHIELD_HEAL_RATE);
            lastShieldRegenTime = currentTime;
        }

        if (repairKitExpiration > 0 && currentTime >= repairKitExpiration) {
            repairKitCount--;
            if (repairKitCount <= 0) {
                repairKitExpiration = 0;
                powerUps.removeIf(item -> item.getType() == 5);
            }
            recalculateDelays();
        }
    }

    public boolean hasActiveRepairKit() {
        return System.currentTimeMillis() < repairKitExpiration && repairKitCount > 0;
    }

    // Getters
    public int getCurrentMovementDelay() { return currentMovementDelay; }
    public int getCurrentFireDelay() { return currentFireDelay; }
    public int getShieldHealth() { return shieldHealth; }
    public boolean hasPowerUps() { return !powerUps.isEmpty(); }
    public int getAntiGravCount() { return antiGravCount; }
    public int getFusionReactorCount() { return fusionReactorCount; }
    public int getDeflectorShieldCount() { return deflectorShieldCount; }
    public int getRepairKitCount() { return repairKitCount; }
}