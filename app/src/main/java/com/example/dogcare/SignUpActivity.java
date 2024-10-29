package com.example.dogcare;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Spinner;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.room.Room;

import com.example.dogcare.classes.User;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;

public class SignUpActivity extends AppCompatActivity {

    private TextInputEditText emailEditText, nameEditText, addressEditText, phoneEditText, passwordEditText, confirmPasswordEditText;
    private Spinner paymentMethodSpinner;
    private MaterialButton registerButton;
    private MaterialButton loginButton;
    private AppDatabase db; // Declare a variable for the database instance

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);

        // Initialize the database instance
        db = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "dog-nutrition-db")
                .allowMainThreadQueries() // Allow database operations on the main thread (for simplicity)
                .build();

        // Set up onApplyWindowInsetsListener for system bars padding
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize UI elements
        emailEditText = findViewById(R.id.email);
        nameEditText = findViewById(R.id.name);
        addressEditText = findViewById(R.id.address);
        phoneEditText = findViewById(R.id.phone);
        passwordEditText = findViewById(R.id.password);
        confirmPasswordEditText = findViewById(R.id.confirm_password);
        paymentMethodSpinner = findViewById(R.id.payment_method_spinner);
        registerButton = findViewById(R.id.register_button);
        loginButton = findViewById(R.id.login_button);

        // Set up payment method spinner

        String[] paymentMethods = {"Cash", "Card"};
        SpinnerArray adapter = new SpinnerArray(this, paymentMethods);
        paymentMethodSpinner.setAdapter(adapter);

        // Set up register button click listener
        registerButton.setOnClickListener(view -> {
            if (validateInput()) {
                // Get user input
                String email = emailEditText.getText().toString().trim();
                String name = nameEditText.getText().toString().trim();
                String address = addressEditText.getText().toString().trim();
                String phone = phoneEditText.getText().toString().trim();
                String password = passwordEditText.getText().toString().trim();
                String paymentMethod = paymentMethods[paymentMethodSpinner.getSelectedItemPosition()];

                // Create User object and register
                User user = new User(email, name, address, phone, password, paymentMethod);
                registerUser(user);
            }
        });
        Intent intent = new Intent(this, SignInActivity.class);
        loginButton.setOnClickListener(view -> {
            startActivity(intent);
        });
    }

    // Function to validate user input
    private boolean validateInput() {
        boolean isValid = true;

        // Email validation
        String email = emailEditText.getText().toString().trim();
        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailEditText.setError("Please enter a valid email address");
            isValid = false;
        } else {
            emailEditText.setError(null);
        }

        // Name validation
        String name = nameEditText.getText().toString().trim();
        if (name.isEmpty()) {
            nameEditText.setError("Please enter your name");
            isValid = false;
        } else {
            nameEditText.setError(null);
        }

        // Address validation
        String address = addressEditText.getText().toString().trim();
        if (address.isEmpty()) {
            addressEditText.setError("Please enter your address");
            isValid = false;
        } else {
            addressEditText.setError(null);
        }

        // Phone number validation
        String phone = phoneEditText.getText().toString().trim();
        if (phone.isEmpty()) {
            phoneEditText.setError("Please enter your phone number");
            isValid = false;
        } else {
            phoneEditText.setError(null);
        }

        // Password validation
        String password = passwordEditText.getText().toString().trim();
        String confirmPassword = confirmPasswordEditText.getText().toString().trim();
        if (password.isEmpty() || password.length() < 8) {
            passwordEditText.setError("Password must be at least 8 characters long");
            isValid = false;
        } else if (!password.equals(confirmPassword)) {
            confirmPasswordEditText.setError("Passwords do not match");
            isValid = false;
        } else {
            passwordEditText.setError(null);
            confirmPasswordEditText.setError(null);
        }

        return isValid;
    }

    // Function to register user

    private void registerUser(User user) {
        new Thread(() -> {
            // Check if the email already exists in the database
            User existingUser = db.userDao().getUserByEmail(user.getEmail());
            if (existingUser != null) {
                // Email already exists, show a dialog box
                runOnUiThread(() -> {
                    showAlertDialog("Email Already Exists", "An account with this email address already exists. Please try a different email address.","ok");
                });
            } else {
                // Insert the new user into the database
                db.userDao().insertUser(user);
                // Update UI on the main thread:
                runOnUiThread(() -> {
                    // Show a success message or navigate to the next activity
                    showAlertDialog("Success", "User registered successfully!","ok");

                });
            }
        }).start();
    }

    private void showAlertDialog(String title, String message, String positiveButtonText) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton(positiveButtonText, (dialog, which) -> {
            Intent intent = new Intent(SignUpActivity.this, SignInActivity.class);
            startActivity(intent);
            finish();
        });
        builder.show();
    }
}