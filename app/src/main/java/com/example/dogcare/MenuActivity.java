package com.example.dogcare;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MenuActivity extends AppCompatActivity {
    SharedPreferences sharedPreferences;
    private Animation floatAnimation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main_menu);
        sharedPreferences = getSharedPreferences("user_preferences", MODE_PRIVATE);
        // Set up animation
        floatAnimation = AnimationUtils.loadAnimation(this, R.anim.float_up);

        // Set up click listeners for icons
        findViewById(R.id.user_icon).setOnClickListener(v -> onUserIconClick());
        findViewById(R.id.cart_icon).setOnClickListener(v -> onCartIconClick());

        findViewById(R.id.educational_content_button).setOnClickListener(v ->
                startActivity(new Intent(this, EducationalContentActivity.class))
        );

        // Set up click listeners for buttons
        findViewById(R.id.button_1).setOnClickListener(v -> onButton1Click());
        findViewById(R.id.button_2).setOnClickListener(v -> onButton2Click());
        findViewById(R.id.button_3).setOnClickListener(v -> onButton3Click());

        // Set up window insets listener
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    // Function to handle user icon click
    private void onUserIconClick() {
        // TODO: Implement user icon click action
        findViewById(R.id.user_icon).startAnimation(floatAnimation);
        Intent intent = new Intent(this, ProfileActivity.class);
        startActivity(intent);
    }

    // Function to handle cart icon click
    private void onCartIconClick() {
        // TODO: Implement cart icon click action
        findViewById(R.id.cart_icon).startAnimation(floatAnimation);
        Intent intent = new Intent(this, ItemCartActivity.class);
        startActivity(intent);
    }

    // Function to handle button 1 click
    private void onButton1Click() {
        // TODO: Implement button 1 click action
        findViewById(R.id.button_1).startAnimation(floatAnimation);
        String userType = sharedPreferences.getString("usertype", "");

        if (userType.equals("admin")) {
            startActivity(new Intent(this, ProductActivity.class));
        } else {
            startActivity(new Intent(this, BuyItemActivity.class));
        }
    }

    // Function to handle button 2 click
    private void onButton2Click() {
        // TODO: Implement button 2 click action
        findViewById(R.id.button_2).startAnimation(floatAnimation);
        Intent intent = new Intent(this, BillActivity.class);
        startActivity(intent);
    }

    // Function to handle button 3 click
    private void onButton3Click() {
        // TODO: Implement button 3 click action
        findViewById(R.id.button_3).startAnimation(floatAnimation);
        Intent intent = new Intent(this, ReviewActivity.class);
        startActivity(intent);
    }

}