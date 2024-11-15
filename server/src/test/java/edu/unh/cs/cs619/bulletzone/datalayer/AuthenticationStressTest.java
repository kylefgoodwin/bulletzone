package edu.unh.cs.cs619.bulletzone.datalayer;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import edu.unh.cs.cs619.bulletzone.datalayer.account.BankAccount;
import edu.unh.cs.cs619.bulletzone.datalayer.user.GameUser;

public class AuthenticationStressTest {
    static BulletZoneData db;
    static final int NUM_THREADS = 10;
    static final int OPERATIONS_PER_THREAD = 100;
    static final int TIMEOUT_MINUTES = 5;

    private AtomicInteger successfulOperations = new AtomicInteger(0);
    private AtomicInteger failedOperations = new AtomicInteger(0);
    private List<String> createdUsers = new ArrayList<>();

    @BeforeClass
    static public void setup() {
        db = new BulletZoneData();
        db.rebuildData();
    }

    @Test
    public void stressTest_MultipleUserOperations_HandlesLoadSuccessfully() throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(NUM_THREADS);
        CountDownLatch latch = new CountDownLatch(NUM_THREADS);
        long startTime = System.currentTimeMillis();

        // Create threads for concurrent operations
        for (int i = 0; i < NUM_THREADS; i++) {
            final int threadId = i;
            executor.submit(() -> {
                try {
                    performOperations(threadId);
                } finally {
                    latch.countDown();
                }
            });
        }

        // Wait for all threads to complete or timeout
        boolean completed = latch.await(TIMEOUT_MINUTES, TimeUnit.MINUTES);
        long duration = System.currentTimeMillis() - startTime;

        // Shutdown executor and wait for remaining tasks
        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.MINUTES);

        // Log results
        System.out.println("Stress Test Results:");
        System.out.println("Duration: " + duration / 1000 + " seconds");
        System.out.println("Successful operations: " + successfulOperations.get());
        System.out.println("Failed operations: " + failedOperations.get());
        System.out.println("Total users created: " + createdUsers.size());
        System.out.println("Operations per second: " +
                (double)(successfulOperations.get() + failedOperations.get()) / (duration / 1000));

        // Verify test results
        Assert.assertTrue("Test should complete within timeout", completed);
        Assert.assertTrue("Success rate should be above 90%",
                (double)successfulOperations.get() / (successfulOperations.get() + failedOperations.get()) > 0.9);
    }

    private void performOperations(int threadId) {
        for (int i = 0; i < OPERATIONS_PER_THREAD; i++) {
            try {
                // Create new user
                String username = "stressTest_" + threadId + "_" + i;
                String password = "pass" + i;
                GameUser newUser = db.users.createUser(username, username, password);

                if (newUser != null) {
                    synchronized(createdUsers) {
                        createdUsers.add(username);
                    }
                    successfulOperations.incrementAndGet();

                    // Create and verify bank account
                    BankAccount account = db.accounts.create();
                    if (account != null) {
                        db.permissions.setOwner(account, newUser);
                        successfulOperations.incrementAndGet();

                        // Verify balance
                        if (account.getBalance() == 1000.0) {
                            successfulOperations.incrementAndGet();
                        } else {
                            failedOperations.incrementAndGet();
                        }
                    } else {
                        failedOperations.incrementAndGet();
                    }

                    // Test login
                    GameUser loggedInUser = db.users.validateLogin(username, password);
                    if (loggedInUser != null && loggedInUser.getId() == newUser.getId()) {
                        successfulOperations.incrementAndGet();
                    } else {
                        failedOperations.incrementAndGet();
                    }
                } else {
                    failedOperations.incrementAndGet();
                }

                // Random delay between operations (0-100ms)
                Thread.sleep((long)(Math.random() * 100));

            } catch (Exception e) {
                failedOperations.incrementAndGet();
                System.err.println("Error in thread " + threadId + ": " + e.getMessage());
            }
        }
    }

    @Test
    public void stressTest_ConcurrentBalanceOperations_MaintainsConsistency() throws InterruptedException {
        // Create test user and account
        GameUser testUser = db.users.createUser("balanceTest", "balanceTest", "password");
        BankAccount testAccount = db.accounts.create();
        db.permissions.setOwner(testAccount, testUser);

        ExecutorService executor = Executors.newFixedThreadPool(NUM_THREADS);
        CountDownLatch latch = new CountDownLatch(NUM_THREADS);
        AtomicInteger successfulTransfers = new AtomicInteger(0);
        double initialBalance = testAccount.getBalance();

        // Perform concurrent balance modifications
        for (int i = 0; i < NUM_THREADS; i++) {
            executor.submit(() -> {
                try {
                    for (int j = 0; j < 10; j++) {
                        if (db.accounts.modifyBalance(testAccount, 100) &&
                                db.accounts.modifyBalance(testAccount, -100)) {
                            successfulTransfers.incrementAndGet();
                        }
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(1, TimeUnit.MINUTES);
        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.MINUTES);

        // Verify final balance matches initial balance
        Assert.assertEquals("Balance should remain unchanged after paired modifications",
                initialBalance, testAccount.getBalance(), 0.001);
        System.out.println("Successful paired transfers: " + successfulTransfers.get());
    }
}