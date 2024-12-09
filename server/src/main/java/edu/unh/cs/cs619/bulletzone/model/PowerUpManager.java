package edu.unh.cs.cs619.bulletzone.model;

import java.util.ArrayDeque;
import java.util.Deque;

public class PowerUpManager {
    private static final String TAG = "PowerUpManager";

    private final Deque<Item> powerUps = new ArrayDeque<>();
    private final int baseMovementDelay;
    private final int baseFireDelay;
    private final PlayableType ownerType;
    private int currentMovementDelay;
    private int currentFireDelay;
    private int shieldHealth;
    private long repairKitExpiration;

    // Constants
    private static final int MAX_SHIELD_HEALTH = 50;
    private static final int SHIELD_HEAL_RATE = 1;
    private static final int REPAIR_KIT_DURATION = 120000; // 2 minutes in milliseconds

    // Counters for each type of power-up
    private int antiGravCount = 0;
    private int fusionReactorCount = 0;
    private int deflectorShieldCount = 0;
    private int repairKitCount = 0;

    private long lastShieldRegenTime;

    public PowerUpManager(int baseMovementDelay, int baseFireDelay, PlayableType ownerType) {
        this.baseMovementDelay = baseMovementDelay;
        this.baseFireDelay = baseFireDelay;
        this.currentMovementDelay = baseMovementDelay;
        this.currentFireDelay = baseFireDelay;
        this.shieldHealth = 0;
        this.repairKitExpiration = 0;
        this.lastShieldRegenTime = System.currentTimeMillis();
        this.ownerType = ownerType;
    }

    public void addPowerUp(Item powerUp) {
        if (powerUp == null) {
            return;
        }

        // Don't allow builders/soldiers to use tank power-ups unless they picked them up themselves
        if (ownerType != PlayableType.TANK && powerUp.getParent() == null) {
            return;
        }

        powerUps.addLast(powerUp);
        switch (powerUp.getType()) {
            case 2: // AntiGrav
                antiGravCount++;
                break;
            case 3: // FusionReactor
                fusionReactorCount++;
                break;
            case 4: // DeflectorShield
                deflectorShieldCount++;
                shieldHealth = MAX_SHIELD_HEALTH;
                lastShieldRegenTime = System.currentTimeMillis();
                break;
            case 5: // RepairKit
                repairKitCount++;
                repairKitExpiration = System.currentTimeMillis() + REPAIR_KIT_DURATION;
                break;
            default:
                break;
        }
        recalculateDelays();
    }

    public Item ejectLastPowerUp() {
        if (powerUps.isEmpty()) {
            return null;
        }

        Item powerUp = powerUps.removeLast();
        switch (powerUp.getType()) {
            case 2: // AntiGrav
                if (antiGravCount > 0) {
                    antiGravCount--;
                }
                break;
            case 3: // FusionReactor
                if (fusionReactorCount > 0) {
                    fusionReactorCount--;
                }
                break;
            case 4: // DeflectorShield
                if (deflectorShieldCount > 0) {
                    deflectorShieldCount--;
                    if (deflectorShieldCount == 0) {
                        shieldHealth = 0;
                    }
                }
                break;
            case 5: // RepairKit
                if (repairKitCount > 0) {
                    repairKitCount--;
                    if (repairKitCount == 0) {
                        repairKitExpiration = 0;
                    }
                }
                break;
        }

        recalculateDelays();
        return powerUp;
    }

    private void recalculateDelays() {
        // Reset to base values
        currentMovementDelay = baseMovementDelay;
        currentFireDelay = baseFireDelay;

        // Apply AntiGrav effects
        for (int i = 0; i < antiGravCount; i++) {
            currentMovementDelay /= 2;
            currentFireDelay += 100;
        }

        // Apply FusionReactor effects
        for (int i = 0; i < fusionReactorCount; i++) {
            currentFireDelay /= 2;
            currentMovementDelay += 100;
        }

        // Apply shield effects if active
        if (deflectorShieldCount > 0 && shieldHealth > 0) {
            currentFireDelay = (int)(currentFireDelay * 1.5);
        }

        // Ensure minimum delays
        currentMovementDelay = Math.max(currentMovementDelay, 100);
        currentFireDelay = Math.max(currentFireDelay, 100);
    }

    public int processDamage(int incomingDamage) {
        if (shieldHealth > 0 && deflectorShieldCount > 0) {
            // Calculate reduced damage
            int reducedDamage = incomingDamage / 2;

            // Apply full damage to shield
            shieldHealth = Math.max(0, shieldHealth - incomingDamage);

            // If shield breaks, remove all shield effects
            if (shieldHealth <= 0) {
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

        // Shield regeneration
        if (shieldHealth > 0 && deflectorShieldCount > 0 &&
                shieldHealth < MAX_SHIELD_HEALTH &&
                currentTime - lastShieldRegenTime >= 1000) {
            shieldHealth = Math.min(MAX_SHIELD_HEALTH, shieldHealth + SHIELD_HEAL_RATE);
            lastShieldRegenTime = currentTime;
        }

        // RepairKit expiration
        if (repairKitExpiration > 0 && currentTime >= repairKitExpiration) {
            repairKitCount--;
            if (repairKitCount <= 0) {
                repairKitCount = 0;
                repairKitExpiration = 0;
                powerUps.removeIf(item -> item.getType() == 5);
            }
            recalculateDelays();
        }
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
    public boolean hasActiveRepairKit() {
        return System.currentTimeMillis() < repairKitExpiration && repairKitCount > 0;
    }
    public long getRepairKitExpiration() { return repairKitExpiration; }
}