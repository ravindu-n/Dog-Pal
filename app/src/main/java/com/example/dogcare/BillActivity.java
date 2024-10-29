package com.example.dogcare;

import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room; // Import Room
import com.example.dogcare.classes.Order;
import java.util.List;

public class BillActivity extends AppCompatActivity {
    private RecyclerView orderRecyclerView;
    private BillArray orderAdapter;
    SharedPreferences sharedPreferences;
    private AppDatabase db; // Database instance

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order);

        // Correctly initialize AppDatabase
        db = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "dog-nutrition-db")
                .allowMainThreadQueries() // Only for simple apps, NOT recommended for production
                .build();

        orderRecyclerView = findViewById(R.id.orderRecyclerView);
        orderRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        sharedPreferences = getSharedPreferences("user_preferences", MODE_PRIVATE);
        String userType = sharedPreferences.getString("usertype", "");

        if(userType.equals("admin")){
            List<Order> orders = db.orderDao().getAllOrders();
            // Initialize the adapter and pass the database instance
            orderAdapter = new BillArray(this, orders, db);
            orderRecyclerView.setAdapter(orderAdapter);
        }else{
            int userId = sharedPreferences.getInt("userId", 0);
            List<Order> orders = db.orderDao().getOrdersByUser(userId);
            // Initialize the adapter and pass the database instance
            orderAdapter = new BillArray(this, orders, db);
            orderRecyclerView.setAdapter(orderAdapter);
        }

    }
}