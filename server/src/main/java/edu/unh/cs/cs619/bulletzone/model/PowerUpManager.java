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
    private static final int MAX_SHIELD_HEALTH = 50;  // Updated to 50
    private static final int SHIELD_HEAL_RATE = 1;
    private static final int REPAIR_KIT_DURATION = 120000;
    private long lastShieldRegenTime;

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
        if (powerUp.getType() == 4) {  // Shield
            shieldHealth = MAX_SHIELD_HEALTH;
            lastShieldRegenTime = System.currentTimeMillis();
            System.out.println("Shield activated with health: " + shieldHealth);
        } else if (powerUp.getType() == 5) {  // Repair Kit
            repairKitExpiration = System.currentTimeMillis() + REPAIR_KIT_DURATION;
        }
        recalculateDelays();
    }

    public Item ejectLastPowerUp() {
        if (!powerUps.isEmpty()) {
            Item powerUp = powerUps.removeLast();
            if (powerUp.getType() == 4) {
                shieldHealth = 0;
                System.out.println("Shield deactivated");
            } else if (powerUp.getType() == 5) {
                repairKitExpiration = 0;
                return null;
            }
            recalculateDelays();
            return powerUp;
        }
        return null;
    }

    public int processDamage(int incomingDamage) {
        if (shieldHealth > 0) {
            // Shield reduces damage by 50%
            int reducedDamage = incomingDamage / 2;

            // Shield takes full damage
            shieldHealth = Math.max(0, shieldHealth - incomingDamage);

            if (shieldHealth <= 0) {
                shieldHealth = 0;
                powerUps.removeIf(item -> item.getType() == 4);
                recalculateDelays();
                System.out.println("Shield depleted and destroyed");
            }

            System.out.println("Shield absorbed 50% damage. Shield health: " + shieldHealth);
            return reducedDamage; // Return half damage
        }
        return incomingDamage; // No shield, full damage
    }

    private void recalculateDelays() {
        currentMovementDelay = baseMovementDelay;
        currentFireDelay = baseFireDelay;

        for (Item powerUp : powerUps) {
            switch (powerUp.getType()) {
                case 2: // AntiGrav
                    currentMovementDelay /= 2;
                    currentFireDelay += 100;
                    break;
                case 3: // FusionReactor
                    currentFireDelay /= 2;
                    currentMovementDelay += 100;
                    break;
                case 4: // Deflector Shield - 50% slower firing
                    currentFireDelay = (int)(currentFireDelay * 1.5);
                    break;
            }
        }
    }

    public void update() {
        long currentTime = System.currentTimeMillis();

        // Shield regeneration
        if (shieldHealth > 0 && shieldHealth < MAX_SHIELD_HEALTH &&
                currentTime - lastShieldRegenTime >= 1000) {  // Check if 1 second has passed
            shieldHealth = Math.min(MAX_SHIELD_HEALTH, shieldHealth + SHIELD_HEAL_RATE);
            lastShieldRegenTime = currentTime;
            System.out.println("Shield regenerated to: " + shieldHealth);
        }

        // Repair kit handling
        if (repairKitExpiration > 0) {
            if (currentTime >= repairKitExpiration) {
                powerUps.removeIf(item -> item.getType() == 5);
                repairKitExpiration = 0;
                recalculateDelays();
            }
        }
    }

    // In PowerUpManager.java
    public boolean hasActiveRepairKit() {
        long currentTime = System.currentTimeMillis();
        return currentTime < repairKitExpiration &&
                powerUps.stream().anyMatch(item -> item.getType() == 5);
    }

    public int getCurrentMovementDelay() { return currentMovementDelay; }
    public int getCurrentFireDelay() { return currentFireDelay; }
    public int getShieldHealth() { return shieldHealth; }
    public boolean hasPowerUps() { return !powerUps.isEmpty(); }
}