package edu.unh.cs.cs619.bulletzone;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.EBean;
import org.androidannotations.rest.spring.annotations.RestService;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientException;

import edu.unh.cs.cs619.bulletzone.rest.BulletZoneRestClient;
import edu.unh.cs.cs619.bulletzone.util.BooleanWrapper;
import edu.unh.cs.cs619.bulletzone.util.LongWrapper;
import edu.unh.cs.cs619.bulletzone.util.ResultWrapper;

@EBean
public class AuthenticationController {
    @RestService
    BulletZoneRestClient restClient;

    /**
     * Constructor for InputHandler
     * [Feel free to add arguments and initialization as needed]
     */
    public AuthenticationController() {
        //note: any work that needs to be done with an annotated item like @RestService or @Bean
        //      will not work here, but should instead go into a method below marked
        //      with the @AfterInject annotation.
    }

    @AfterInject
    public void afterInject() {
        //Any initialization involving components annotated with things like @RestService or @Bean
        //goes here.
    }

    /**
     * Uses restClient to login.
     *
     * @param username Username provided by user.
     * @param password Password for account provided by user.
     */
    public ResultWrapper<Long> login(String username, String password) {
        try {
            LongWrapper result = restClient.login(username, password);
            long userId = result.getResult();
            if (userId >= 0) {
                return new ResultWrapper<>(true, "Login successful", userId);
            } else {
                return new ResultWrapper<>(false, "Invalid username or password", null);
            }
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                return new ResultWrapper<>(false, "Invalid username or password", null);
            } else {
                return new ResultWrapper<>(false, "Login failed: " + e.getMessage(), null);
            }
        } catch (Exception e) {
            return new ResultWrapper<>(false, "Unexpected error: " + e.getMessage(), null);
        }
    }

    /**
     * Uses restClient to register.
     *
     * @param username New username provided by user.
     * @param password Password for new account provided by user.
     */
    public ResultWrapper<Long> register(String username, String password) {
        try {
            BooleanWrapper result = restClient.register(username, password);
            if (result.isResult()) {
                return new ResultWrapper<>(true, "Registration successful", null);
            } else {
                return new ResultWrapper<>(false, "Registration failed", null);
            }
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.BAD_REQUEST) {
                return new ResultWrapper<>(false, "User already exists", null);
            } else {
                return new ResultWrapper<>(false, "Registration failed: " + e.getMessage(), null);
            }
        } catch (Exception e) {
            return new ResultWrapper<>(false, "Unexpected error: " + e.getMessage(), null);
        }
    }

    /**
     * Helper for testing
     *
     * @param restClientPassed tested restClient
     */
    public void initialize(BulletZoneRestClient restClientPassed) {
        restClient = restClientPassed;
    }

    public ResultWrapper<Double> getBalance(long userId) {
        try {
            Double balance = restClient.getBalance(userId);
            if (balance != null) {
                return new ResultWrapper<>(true, "Balance retrieved", balance);
            } else {
                return new ResultWrapper<>(false, "Balance unavailable", null);
            }
        } catch (HttpServerErrorException e) {
            String errorMessage = "Server error: " + e.getResponseBodyAsString();
            if (e.getStatusCode() == HttpStatus.INTERNAL_SERVER_ERROR) {
                return new ResultWrapper<>(false, "Server error while retrieving balance. Please try again later.", null);
            }
            return new ResultWrapper<>(false, errorMessage, null);
        } catch (RestClientException e) {
            return new ResultWrapper<>(false, "Error connecting to server: " + e.getMessage(), null);
        } catch (Exception e) {
            return new ResultWrapper<>(false, "Unexpected error: " + e.getMessage(), null);
        }
    }
}