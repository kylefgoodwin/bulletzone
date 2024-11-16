package edu.unh.cs.cs619.bulletzone.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import edu.unh.cs.cs619.bulletzone.datalayer.user.GameUser;
import edu.unh.cs.cs619.bulletzone.repository.DataRepository;
import edu.unh.cs.cs619.bulletzone.util.BooleanWrapper;
import edu.unh.cs.cs619.bulletzone.util.LongWrapper;

@RestController
@RequestMapping(value = "/games/account")
public class AccountController {
    private static final Logger log = LoggerFactory.getLogger(AccountController.class);

    private final DataRepository data;
    private final Lock balanceLock = new ReentrantLock();

    @Autowired
    public AccountController(DataRepository repo) {
        this.data = repo;
    }

    @RequestMapping(method = RequestMethod.PUT, value = "register/{name}/{password}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @ResponseBody
    public ResponseEntity<BooleanWrapper> register(@PathVariable String name, @PathVariable String password) {
        log.debug("Register '{}' with password '{}'", name, password);
        try {
            GameUser user = data.validateUser(name, password, true);
            boolean success = user != null;
            return new ResponseEntity<>(new BooleanWrapper(success), success ? HttpStatus.CREATED : HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            log.error("Error registering user '{}': {}", name, e.getMessage(), e);
            return new ResponseEntity<>(new BooleanWrapper(false), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(method = RequestMethod.PUT, value = "login/{name}/{password}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public ResponseEntity<LongWrapper> login(@PathVariable String name, @PathVariable String password) {
        log.debug("Login '{}' with password '{}'", name, password);
        try {
            GameUser user = data.validateUser(name, password, false);
            if (user != null) {
                return new ResponseEntity<>(new LongWrapper(user.getId()), HttpStatus.OK);
            } else {
                return new ResponseEntity<>(new LongWrapper(-1L), HttpStatus.UNAUTHORIZED);
            }
        } catch (Exception e) {
            log.error("Error logging in user '{}': {}", name, e.getMessage(), e);
            return new ResponseEntity<>(new LongWrapper(-1L), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(method = RequestMethod.GET, value = "balance/{userId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public ResponseEntity<Double> getBalance(@PathVariable int userId) {
        log.debug("Get balance for user ID: {}", userId);
        try {
            double balance = data.getUserBalance(userId);
            if (balance < 0) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            return new ResponseEntity<>(balance, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error getting balance for user '{}': {}", userId, e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/balance/{userId}/deduct/{amount}")
    @ResponseBody
    public ResponseEntity<BooleanWrapper> deductBalance(
            @PathVariable("userId") long userId,
            @PathVariable("amount") double amount) {
        log.debug("Deduct balance called for user {} amount {}", userId, amount);
        balanceLock.lock();
        try {
            double currentBalance = data.getUserBalance(userId);
            log.debug("Current balance for user {}: {}", userId, currentBalance);

            if (currentBalance < amount) {
                log.debug("Insufficient balance for user {}: {} < {}", userId, currentBalance, amount);
                return new ResponseEntity<>(new BooleanWrapper(false), HttpStatus.BAD_REQUEST);
            }

            boolean success = data.deductUserBalance(userId, amount);
            log.debug("Deduction result for user {}: {}", userId, success);

            if (success) {
                double newBalance = data.getUserBalance(userId);
                log.debug("New balance for user {}: {}", userId, newBalance);
            }

            return new ResponseEntity<>(new BooleanWrapper(success), HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error deducting balance for user '{}': {}", userId, e.getMessage(), e);
            return new ResponseEntity<>(new BooleanWrapper(false), HttpStatus.INTERNAL_SERVER_ERROR);
        } finally {
            balanceLock.unlock();
        }
    }

    @GetMapping("/ping")
    @ResponseBody
    public String ping() {
        return "AccountController is working";
    }

    @PutMapping("/balance/{userId}/deposit/{amount}")
    @ResponseBody
    public ResponseEntity<BooleanWrapper> depositBalance(
            @PathVariable("userId") long userId,
            @PathVariable("amount") double amount) {
        log.debug("Deposit balance called for user {} amount {}", userId, amount);
        balanceLock.lock();
        try {
            double currentBalance = data.getUserBalance(userId);
            log.debug("Current balance for user {}: {}", userId, currentBalance);

            boolean success = data.depositUserBalance(userId, amount);
            log.debug("Deposit result for user {}: {}", userId, success);

            if (success) {
                double newBalance = data.getUserBalance(userId);
                log.debug("New balance for user {}: {}", userId, newBalance);
                return new ResponseEntity<>(new BooleanWrapper(true), HttpStatus.OK);
            }
            return new ResponseEntity<>(new BooleanWrapper(false), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            log.error("Error depositing balance for user '{}': {}", userId, e.getMessage(), e);
            return new ResponseEntity<>(new BooleanWrapper(false), HttpStatus.INTERNAL_SERVER_ERROR);
        } finally {
            balanceLock.unlock();
        }
    }
}