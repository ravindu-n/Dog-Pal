package com.example.dogcare;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.room.Room;

import com.example.dogcare.classes.User;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class SignInActivity extends AppCompatActivity {

    private EditText emailEditText;
    private EditText passwordEditText;
    private Button loginButton;
    private Button registerButton;
    private ImageView emailIconImageView;
    private ImageView passwordIconImageView;

    private AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        emailEditText = findViewById(R.id.email);
        passwordEditText = findViewById(R.id.password);
        loginButton = findViewById(R.id.login_button);
        registerButton = findViewById(R.id.register_button);
        emailIconImageView = findViewById(R.id.email_icon);
        passwordIconImageView = findViewById(R.id.password_icon);
        SharedPreferences sharedPreferences = getSharedPreferences("user_preferences", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        // Initialize the database
        db = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "dog-nutrition-db")
                .allowMainThreadQueries() // This is for demo purposes only, do not use on the main thread in production
                .build();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailEditText.getText().toString();
                String password = passwordEditText.getText().toString();

                if (!isValidEmail(email)) {
                    emailEditText.setError("Invalid email address");
                } else {
                    emailEditText.setError(null);
                }

                if (!isValidPassword(password)) {
                    passwordEditText.setError("Password must be at least 8 characters long");
                } else {
                    passwordEditText.setError(null);
                }

                if (isValidEmail(email) && isValidPassword(password)) {
                    if (email.equals("admin@gmail.com") && password.equals("admin123")) {
                        showAlertDialog("Admin Login", "Admin login successful!", "OK");
                        // Save user ID and user type to SharedPreferences
                        editor.putInt("userId", 0);
                        editor.putString("usertype", "admin");
                        editor.apply();
                        // Handle admin login
                        Intent intent = new Intent(SignInActivity.this, MenuActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        // Check if credentials match in database
                        User user = db.userDao().getUserByEmail(email);
                        if (user != null && user.getPassword().equals(password)) {
                            showAlertDialog("Login", "Login successful!", "OK");
                            // Save user ID and user type to SharedPreferences
                            editor.putInt("userId", user.getId());
                            editor.putString("usertype", "customer");
                            editor.apply();
                            // Handle user login
                            Intent intent = new Intent(SignInActivity.this, MenuActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            showAlertDialog("Error", "Invalid credentials", "OK");
                        }
                    }
                }
            }
        });

        Intent intent = new Intent(this, SignUpActivity.class);
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(intent);
            }
        });
    }

    private boolean isValidEmail(String email) {
        String emailRegex = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
        return email.matches(emailRegex);
    }

    private boolean isValidPassword(String password) {
        return password.length() >= 8;
    }

    private void showAlertDialog(String title, String message, String positiveButtonText) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton(positiveButtonText, (dialog, which) -> {
            // Dismiss the dialog
            dialog.dismiss();
        });
        builder.show();
    }
}