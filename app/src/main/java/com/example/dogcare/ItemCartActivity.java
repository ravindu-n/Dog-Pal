package com.example.dogcare;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dogcare.interfaces.OrderDao;
import com.example.dogcare.interfaces.OrderItemDao;
import com.example.dogcare.classes.CartItem;
import com.example.dogcare.classes.Order;
import com.example.dogcare.classes.OrderItem;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import androidx.room.Room;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ItemCartActivity extends AppCompatActivity implements ItemCartArray.CartUpdateListener{
    private AppDatabase db; // Your Room database instance
    private ItemCartArray adapter;
    private TextView cartTotalTextView;
    SharedPreferences sharedPreferences;

    @SuppressLint("DefaultLocale")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_cart);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        cartTotalTextView = findViewById(R.id.textView2);
        sharedPreferences = getSharedPreferences("user_preferences", MODE_PRIVATE);

        MaterialButton checkoutButton = findViewById(R.id.button);
        checkoutButton.setOnClickListener(v -> showConfirmationDialog());


        // Initialize Database
        db = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "dog-nutrition-db")
                .allowMainThreadQueries()
                .build();

        // Get RecyclerView
        RecyclerView cartItemList = findViewById(R.id.cartItemList);

        // Set up LayoutManager
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        cartItemList.setLayoutManager(layoutManager);

        // Set up Adapter
        adapter = new ItemCartArray(this, db.productDao()); // Pass database access
        adapter.setCartUpdateListener(this);
        cartItemList.setAdapter(adapter);
        // Calculate and set initial total
        double initialTotal = adapter.calculateCartTotal();
        cartTotalTextView.setText(String.format("$ %.2f", initialTotal));

    }

    @SuppressLint("DefaultLocale")
    @Override
    public void onCartUpdated(double cartTotal) {
        cartTotalTextView.setText(String.format("$ %.2f", cartTotal));
    }

    private void showConfirmationDialog() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Confirm Checkout")
                .setMessage("Are you sure you want to proceed with the checkout?")
                .setPositiveButton("Proceed", (dialog, which) -> processCheckout())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void processCheckout() {
        int userId = sharedPreferences.getInt("userId", 0);

        // 2. Create a new Order
        Order newOrder = new Order();
        newOrder.setUserId(userId);
        newOrder.setOrderDate(getCurrentDate());
        newOrder.setTotalPrice(adapter.calculateCartTotal());

        // 3. Insert Order into the database
        OrderDao orderDao = db.orderDao();
        long orderId = orderDao.insertOrder(newOrder); // Get generated order ID

        // 4. Insert Order Items
        OrderItemDao orderItemDao = db.orderItemDao();
        List<CartItem> cartItems = adapter.getCartItems(); // Get cart items from adapter
        for (CartItem cartItem : cartItems) {
            OrderItem orderItem = new OrderItem();
            orderItem.setOrderId((int) orderId);
            orderItem.setProductId(cartItem.getProduct().getId());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setSubtotal(cartItem.getProduct().getPrice() * cartItem.getQuantity());
            orderItemDao.insertOrderItem(orderItem);
        }

        // 5. Clear the cart (from SharedPreferences and adapter)
        clearCart();

        // 6. Show success message
        Toast.makeText(this, "Order placed successfully!", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this, BillActivity.class);
        startActivity(intent);
        finish(); // Optional: Close CartActivity after successful checkout
    }

    @SuppressLint({"NotifyDataSetChanged", "DefaultLocale"})
    private void clearCart() {
        SharedPreferences sharedPreferences = getSharedPreferences("cart", Context.MODE_PRIVATE);
        sharedPreferences.edit().clear().apply();
        adapter.clearCartItems(); // Assuming you add a clearCartItems() method to your adapter
        adapter.notifyDataSetChanged();

        // Update cart total
        cartTotalTextView.setText(String.format("$ %.2f", 0.00));
    }

    private String getCurrentDate() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return dateFormat.format(new Date());
    }
}