package edu.unh.cs.cs619.bulletzone;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import edu.unh.cs.cs619.bulletzone.util.ResultWrapper;

@EActivity(R.layout.activity_authenticate)
public class AuthenticateActivity extends AppCompatActivity {
    private static final String TAG = "AuthenticateActivity";

    @ViewById
    EditText username_editText;

    @ViewById
    EditText password_editText;

    @ViewById
    TextView status_message;

    @Bean
    AuthenticationController controller;

    PlayerData playerData = PlayerData.getPlayerData();

    long userID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Since we are using the @EActivity annotation, anything done past this point will
        //be overridden by the work AndroidAnnotations does. If you need to do more setup,
        //add to the methods under @AfterViews (for view items) or @AfterInject (for Bean items) below
    }

    @AfterViews
    protected void afterViewInjection() {
        Log.d(TAG, "Views injected");
    }

    @AfterInject
    void afterInject() {
        Log.d(TAG, "Dependencies injected");
    }

    /**
     * Registers a new user and logs them in
     */
    @Click(R.id.register_button)
    @Background
    protected void onButtonRegister() {
        String username = username_editText.getText().toString();
        String password = password_editText.getText().toString();

        if (username.isEmpty() || password.isEmpty()) {
            setStatus("Username and password cannot be empty");
            return;
        }

        try {
            ResultWrapper<Long> result = controller.register(username, password);

            if (result.isSuccess()) {
                setStatus("Registration successful. Logging in...");
                // Automatically log in after successful registration
                ResultWrapper<Long> loginResult = controller.login(username, password);
                if (loginResult.isSuccess()) {
                    onLoginSuccess(loginResult.getResult());
                } else {
                    setStatus("Registration successful but login failed: " + loginResult.getMessage());
                }
            } else {
                setStatus("Registration failed: " + result.getMessage());
            }
        } catch (Exception e) {
            Log.e(TAG, "Registration error", e);
            setStatus("Registration error: " + e.getMessage());
        }
    }

    /**
     * Logs in an existing user
     */
    @Click(R.id.login_button)
    @Background
    protected void onButtonLogin() {
        String username = username_editText.getText().toString();
        String password = password_editText.getText().toString();

        if (username.isEmpty() || password.isEmpty()) {
            setStatus("Username and password cannot be empty");
            return;
        }

        try {
            ResultWrapper<Long> result = controller.login(username, password);

            if (result.isSuccess()) {
                Long userId = result.getResult();
                Log.d(TAG, "Login successful for userId: " + userId);

                // Verify balance access
                ResultWrapper<Double> balanceResult = controller.getBalance(userId);
                if (balanceResult.isSuccess()) {
                    Log.d(TAG, "Balance retrieved successfully: " + balanceResult.getResult());
                } else {
                    Log.w(TAG, "Balance retrieval failed: " + balanceResult.getMessage());
                }

                setStatus("Login successful");
                onLoginSuccess(userId);
            } else {
                setStatus("Login failed: " + result.getMessage());
            }
        } catch (Exception e) {
            Log.e(TAG, "Login error", e);
            setStatus("An unexpected error occurred: " + e.getMessage());
        }
    }

    @UiThread
    public void onLoginSuccess(Long userId) {
        Log.d(TAG, "onLoginSuccess called with userId: " + userId);

        // Start the main game activity
        Intent intent = new Intent(this, MenuActivity_.class);
        playerData.setUserId(userId);
        Log.d(TAG, "Starting MenuActivity_");
        startActivity(intent);
        Log.d(TAG, "MenuActivity_ started");
        finish(); // Close the login activity
    }

    @UiThread
    protected void setStatus(String message) {
        if (status_message != null) {
            status_message.setText(message);
            Log.d(TAG, "Status updated: " + message);
        }
    }
}