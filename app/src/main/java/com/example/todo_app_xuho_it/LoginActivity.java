package com.example.todo_app_xuho_it;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {
    private EditText edUsername;
    private EditText edPassword;
    private CheckBox cbRememberMe;
    private Button btnLogin;
    
    private SharedPreferences sharedPreferences;
    private static final String PREFS_NAME = "LoginPrefs";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        
        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        
        // Initialize views first
        initViews();
        
        // Check if user is already logged in
        if (isUserLoggedIn()) {
            navigateToMainActivity();
            return;
        }
        
        setupEventListeners();
        loadSavedCredentials();
    }
    
    private void initViews() {
        try {
            edUsername = findViewById(R.id.edUsername);
            edPassword = findViewById(R.id.edPassword);
            cbRememberMe = findViewById(R.id.cbRememberMe);
            btnLogin = findViewById(R.id.btnLogin);
            
            // Check if views were found
            if (edUsername == null || edPassword == null || cbRememberMe == null || btnLogin == null) {
                Toast.makeText(this, "Error: Could not find login views", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error initializing views: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
    
    private void setupEventListeners() {
        btnLogin.setOnClickListener(v -> handleLogin());
    }
    
    private void handleLogin() {
        String username = edUsername.getText().toString().trim();
        String password = edPassword.getText().toString().trim();
        
        // Validate input
        if (TextUtils.isEmpty(username)) {
            edUsername.setError("Username is required");
            edUsername.requestFocus();
            return;
        }
        
        if (TextUtils.isEmpty(password)) {
            edPassword.setError("Password is required");
            edPassword.requestFocus();
            return;
        }
        
        // Check credentials
        if (username.equals("admin") && password.equals("123456")) {
            // Login successful
            Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show();
            
            // Save login state if "Remember Me" is checked
            if (cbRememberMe.isChecked()) {
                saveLoginState(username);
            } else {
                // Save temporary login state (will be cleared on app restart)
                saveTemporaryLoginState(username);
            }
            
            navigateToMainActivity();
        } else {
            // Invalid credentials
            Toast.makeText(this, "Invalid username or password", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void saveLoginState(String username) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_USERNAME, username);
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.apply();
    }
    
    private void clearLoginState() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
    }
    
    private void saveTemporaryLoginState(String username) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_USERNAME, username);
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putBoolean("is_temporary_login", true); // Mark as temporary
        editor.apply();
    }
    
    private boolean isUserLoggedIn() {
        return sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false);
    }
    
    private void loadSavedCredentials() {
        String savedUsername = sharedPreferences.getString(KEY_USERNAME, "");
        if (!savedUsername.isEmpty()) {
            edUsername.setText(savedUsername);
            cbRememberMe.setChecked(true);
        }
    }
    
    private void navigateToMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        String username = "User";
        if (edUsername != null) {
            username = edUsername.getText().toString().trim();
        } else {
            // Get username from SharedPreferences if EditText is null
            username = sharedPreferences.getString(KEY_USERNAME, "User");
        }
        intent.putExtra("username", username);
        startActivity(intent);
        finish();
    }
}
