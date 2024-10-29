package com.example.dogcare;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;
import com.example.dogcare.classes.Product;
import java.util.List;

public class ReviewActivity extends AppCompatActivity {
    private RecyclerView reviewRecyclerView;
    private ReviewArray reviewArray;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review);

        reviewRecyclerView = findViewById(R.id.reviewRecyclerView);
        reviewRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Fetch products from your database
        AppDatabase db = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "dog-nutrition-db")
                .allowMainThreadQueries()
                .build();
        List<Product> products = db.productDao().getAllProducts();

        // Pass the ReviewDao instance to the adapter
        reviewArray = new ReviewArray(products, this, db.reviewDao());
        reviewRecyclerView.setAdapter(reviewArray);
    }
}