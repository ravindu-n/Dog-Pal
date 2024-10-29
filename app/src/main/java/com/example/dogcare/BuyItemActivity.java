package com.example.dogcare;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import com.example.dogcare.classes.Product;
import com.google.android.material.textfield.TextInputEditText;

import java.util.List;

public class BuyItemActivity extends AppCompatActivity {

    private AppDatabase db;
    private ProductArray adapter;
    private List<Product> allProducts; // Store all products for filtering

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_customer_product);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize Database
        db = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "dog-nutrition-db")
                .allowMainThreadQueries()
                .build();

        // Get the RecyclerView
        RecyclerView productList = findViewById(R.id.productList);

        // Set up the LayoutManager
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        productList.setLayoutManager(layoutManager);

        // Fetch all products initially
        allProducts = db.productDao().getAllProducts();

        // Set up the Adapter
        adapter = new ProductArray(this, allProducts, db.productDao(), true, null);
        productList.setAdapter(adapter);

        // Search functionality
        TextInputEditText searchBar = findViewById(R.id.search_bar);
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Not used in this implementation
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterProducts(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Not used in this implementation
            }
        });
    }

    private void filterProducts(String query) {
        List<Product> filteredList;

        if (query.isEmpty()) {
            // Show all products if the query is empty
            filteredList = db.productDao().getAllProducts();
        } else {
            // Filter based on product name (case-insensitive)
            filteredList = db.productDao().getProductsByName(query.toLowerCase());
        }

        // Update the adapter with the filtered OR complete list
        adapter.updateProducts(filteredList);
    }
}