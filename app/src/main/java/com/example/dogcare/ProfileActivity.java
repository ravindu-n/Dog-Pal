package com.example.dogcare;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.room.Room;

import com.example.dogcare.classes.User;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class ProfileActivity extends AppCompatActivity {
    private TextInputEditText emailEditText, nameEditText, addressEditText, phoneEditText, currentPasswordEditText, newPasswordEditText, confirmNewPasswordEditText;
    private MaterialButton changePasswordButton, updateAccountButton;
    private AppDatabase db;
    private User currentUser;
    private Spinner paymentMethodSpinner;
    private String[] paymentMethods;
    private boolean isEditing;
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_account);

        db = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "dog-nutrition-db")
                .allowMainThreadQueries()
                .build();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        sharedPreferences = getSharedPreferences("user_preferences", MODE_PRIVATE);
        // Initialize UI elements
        emailEditText = findViewById(R.id.email);
        nameEditText = findViewById(R.id.name);
        addressEditText = findViewById(R.id.address);
        phoneEditText = findViewById(R.id.phone);
        currentPasswordEditText = findViewById(R.id.current_password);
        newPasswordEditText = findViewById(R.id.new_password);
        confirmNewPasswordEditText = findViewById(R.id.confirm_new_password);
        paymentMethodSpinner = findViewById(R.id.payment_method_spinner);
        changePasswordButton = findViewById(R.id.change_password_button);
        updateAccountButton = findViewById(R.id.update_account_button);

        paymentMethods = new String[]{"Cash", "Card"};
        SpinnerArray adapter = new SpinnerArray(this, paymentMethods);
        paymentMethodSpinner.setAdapter(adapter);

        // Get userId from intent
        int userId = sharedPreferences.getInt("userId", 0);
        if (userId== 0) {
            Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Fetch user data from database
        currentUser = db.userDao().getUserById(userId);
        if (currentUser != null) {
            populateUserData(currentUser);
        } else {
            Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show();
            finish();
        }

        isEditing = false;

        // Change password button click listener
        changePasswordButton.setOnClickListener(view -> toggleChangePasswordFields());

        // Update account details button click listener
        updateAccountButton.setOnClickListener(view -> {
            if (isEditing) {
                if (validateInput()) {
                    updateUserData();
                    disableEditFields();
                    updateAccountButton.setText("Update Account Details");
                    isEditing = false;
                }
            } else {
                enableEditFields();
                updateAccountButton.setText("Save");
                isEditing = true;
            }
        });


    }

    // Populate user data in UI
    private void populateUserData(User user) {
        emailEditText.setText(user.getEmail());
        nameEditText.setText(user.getName());
        addressEditText.setText(user.getAddress());
        phoneEditText.setText(user.getPhoneNumber());
        if (user.getPaymentMethod() != null) {
            int position = getSpinnerPosition(user.getPaymentMethod());
            paymentMethodSpinner.setSelection(position);
        }
    }

    private int getSpinnerPosition(String paymentMethod) {
        for (int i = 0; i < paymentMethods.length; i++) {
            if (paymentMethods[i].equals(paymentMethod)) {
                return i;
            }
        }
        return 0;
    }

    // Toggle change password fields visibility
    private void toggleChangePasswordFields() {
        View changePasswordFields = findViewById(R.id.change_password_fields);
        changePasswordFields.setVisibility(changePasswordFields.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
    }

    // Enable edit fields
    private void enableEditFields() {
        emailEditText.setEnabled(true);
        nameEditText.setEnabled(true);
        addressEditText.setEnabled(true);
        phoneEditText.setEnabled(true);
        paymentMethodSpinner.setEnabled(true);
    }

    // Disable edit fields
    private void disableEditFields() {
        emailEditText.setEnabled(false);
        nameEditText.setEnabled(false);
        addressEditText.setEnabled(false);
        phoneEditText.setEnabled(false);
        paymentMethodSpinner.setEnabled(false);
    }


    // Validate user input for update

    private boolean validateInput() {
        boolean isValid = true;

        String email = emailEditText.getText().toString().trim();
        String name = nameEditText.getText().toString().trim();
        String address = addressEditText.getText().toString().trim();
        String phone = phoneEditText.getText().toString().trim();
        String currentPassword = currentPasswordEditText.getText().toString().trim();
        String newPassword = newPasswordEditText.getText().toString().trim();
        String confirmNewPassword = confirmNewPasswordEditText.getText().toString().trim();

        if (email.isEmpty()) {
            emailEditText.setError("Email cannot be empty");
            isValid = false;
        } else if (!isValidEmail(email)) {
            emailEditText.setError("Invalid email address");
            isValid = false;
        }

        if (name.isEmpty()) {
            nameEditText.setError("Name cannot be empty");
            isValid = false;
        }

        if (address.isEmpty()) {
            addressEditText.setError("Address cannot be empty");
            isValid = false;
        }

        if (phone.isEmpty()) {
            phoneEditText.setError("Phone number cannot be empty");
            isValid = false;
        } else if (!isValidPhone(phone)) {
            phoneEditText.setError("Invalid phone number");
            isValid = false;
        }

        if (!currentPassword.isEmpty() && !newPassword.isEmpty() && !confirmNewPassword.isEmpty()) {
            if (!currentPassword.equals(currentUser.getPassword())) {
                currentPasswordEditText.setError("Incorrect current password");
                isValid = false;
            } else if (!newPassword.equals(confirmNewPassword)) {
                newPasswordEditText.setError("New passwords don't match");
                confirmNewPasswordEditText.setError("New passwords don't match");
                isValid = false;
            } else if (newPassword.length() < 8) {
                newPasswordEditText.setError("New password must be at least 8 characters");
                isValid = false;
            }
        }

        return isValid;
    }

    // Validate email address
    private boolean isValidEmail(String email) {
        String emailPattern = "[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}";
        return email.matches(emailPattern);
    }

    // Validate phone number
    private boolean isValidPhone(String phone) {
        String phonePattern = "[0-9]{10,13}"; // Assuming phone number can be 10-13 digits
        return phone.matches(phonePattern);
    }

    // Update user data in database
    private void updateUserData() {
        String newEmail = emailEditText.getText().toString().trim();
        String newName = nameEditText.getText().toString().trim();
        String newAddress = addressEditText.getText().toString().trim();
        String newPhone = phoneEditText.getText().toString().trim();
        String newPaymentMethod = paymentMethods[paymentMethodSpinner.getSelectedItemPosition()];

        // Update user object
        currentUser.setEmail(newEmail);
        currentUser.setName(newName);
        currentUser.setAddress(newAddress);
        currentUser.setPhoneNumber(newPhone);
        currentUser.setPaymentMethod(newPaymentMethod);

        // Update password if changed
        String currentPassword = currentPasswordEditText.getText().toString().trim();
        String newPassword = newPasswordEditText.getText().toString().trim();
        String confirmNewPassword = confirmNewPasswordEditText.getText().toString().trim();

        if (!newPassword.isEmpty() && !confirmNewPassword.isEmpty()) {
            if (currentPassword.equals(currentUser.getPassword()) && newPassword.equals(confirmNewPassword)) {
                currentUser.setPassword(newPassword);
            } else {
                Toast.makeText(this, "Incorrect current password or passwords don't match", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        // Update user data in the database on a background thread
        new Thread(() -> {
            db.userDao().updateUser(currentUser);
            runOnUiThread(() -> {
                Toast.makeText(ProfileActivity.this, "Account details updated successfully", Toast.LENGTH_SHORT).show();
            });
        }).start();
    }
}