package edu.unh.cs.cs619.bulletzone.repository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import edu.unh.cs.cs619.bulletzone.datalayer.BulletZoneData;
import edu.unh.cs.cs619.bulletzone.datalayer.account.BankAccount;
import edu.unh.cs.cs619.bulletzone.datalayer.user.GameUser;

@Component
public class DataRepository {
    private static final Logger logger = LoggerFactory.getLogger(DataRepository.class);
    private BulletZoneData bzdata;

    DataRepository() {
        bzdata = new BulletZoneData(); // just use in-memory database
    }

    public GameUser validateUser(String username, String password, boolean create) {
        GameUser user;
        if (create) {
            logger.debug("Attempting to create new user: {}", username);
            // First check if user already exists
            user = bzdata.users.getUser(username);
            if (user != null) {
                logger.debug("User already exists: {}", username);
                return null; // User already exists
            }

            // Create new user with a default display name
            user = bzdata.users.createUser(username, username, password);
            if (user != null) {
                logger.debug("Created user with ID: {}", user.getId());
                // Create bank account with initial balance
                BankAccount account = bzdata.accounts.create();
                if (account != null) {
                    logger.debug("Created bank account for user ID: {}", user.getId());
                    // Associate account with user - balance is already initialized in BankAccount constructor
                    account.setOwner(user);
                    logger.debug("Associated account with user");
                    return user;
                } else {
                    logger.error("Failed to create bank account");
                }
            } else {
                logger.error("Failed to create user");
            }
            return null;
        } else {
            logger.debug("Attempting to validate login for: {}", username);
            user = bzdata.users.validateLogin(username, password);
            if (user != null) {
                logger.debug("Login successful for user ID: {}", user.getId());
            } else {
                logger.debug("Login failed for username: {}", username);
            }
            return user;
        }
    }

    public double getUserBalance(long userId) {
        logger.debug("Getting balance for user ID: {}", userId);
        // First get the user to verify they exist
        GameUser user = bzdata.users.getUser((int)userId);
        if (user == null) {
            logger.error("User not found for ID: {}", userId);
            return 0.0;
        }

        logger.debug("Found user: {}", user.getName());

        // Find account associated with this user
        for (BankAccount account : bzdata.accounts.getAccounts()) {
            logger.debug("Checking account with owner: {}",
                    (account.getOwner() != null ? account.getOwner().getId() : "null"));
            if (account.getOwner() != null && account.getOwner().getId() == userId) {
                double balance = account.getBalance();
                logger.debug("Found account for user with balance: {}", balance);
                return balance;
            }
        }

        logger.debug("No existing account found, creating new one");
        // If no account exists, create one with initial balance
        BankAccount newAccount = bzdata.accounts.create();
        if (newAccount != null) {
            newAccount.setOwner(user);
            double balance = newAccount.getBalance();
            logger.debug("Created new account with balance: {}", balance);
            return balance;
        }

        logger.error("Failed to create or find account for user");
        return 0.0;
    }

    public boolean deductUserBalance(long userId, double amount) {
        logger.debug("Deducting {} from user ID: {}", amount, userId);
        // First get the user to verify they exist
        GameUser user = bzdata.users.getUser((int)userId);
        if (user == null) {
            logger.error("User not found for ID: {}", userId);
            return false;
        }

        // Find account associated with this user
        for (BankAccount account : bzdata.accounts.getAccounts()) {
            if (account.getOwner() != null && account.getOwner().getId() == userId) {
                if (account.getBalance() >= amount) {
                    account.modifyBalance(-amount);
                    logger.debug("New balance for user {}: {}", userId, account.getBalance());
                    return true;
                } else {
                    logger.error("Insufficient balance for user {}", userId);
                    return false;
                }
            }
        }

        logger.error("No account found for user {}", userId);
        return false;
    }

    public boolean depositUserBalance(long userId, double amount) {
        logger.debug("Deducting {} from user ID: {}", amount, userId);
        // First get the user to verify they exist
        GameUser user = bzdata.users.getUser((int)userId);
        if (user == null) {
            logger.error("User not found for ID: {}", userId);
            return false;
        }

        // Find account associated with this user
        for (BankAccount account : bzdata.accounts.getAccounts()) {
            if (account.getOwner() != null && account.getOwner().getId() == userId) {
                if (account.getBalance() >= amount) {
                    account.modifyBalance(+amount);
                    logger.debug("New balance for user {}: {}", userId, account.getBalance());
                    return true;
                } else {
                    logger.error("Insufficient balance for user {}", userId);
                    return false;
                }
            }
        }

        logger.error("No account found for user {}", userId);
        return false;
    }
}