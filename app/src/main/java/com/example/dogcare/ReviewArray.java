package com.example.dogcare;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dogcare.interfaces.ReviewDao;
import com.example.dogcare.classes.Product;
import com.example.dogcare.classes.Review;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.List;

public class ReviewArray extends RecyclerView.Adapter<ReviewArray.ProductViewHolder> {

    private List<Product> productList;
    private Context context;
    private ReviewDao reviewDao;

    public ReviewArray(List<Product> productList, Context context, ReviewDao reviewDao) {
        this.productList = productList;
        this.context = context;
        this.reviewDao = reviewDao;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.review_product, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = productList.get(position);
        holder.productNameTextView.setText(product.getName());

        // Calculate and set the average rating
        float averageRating = calculateAverageRating(product.getId());
        setRatingStars(holder.starIconsLinearLayout, averageRating);

        // Load the image using the getBitmapFromUrl method
        Bitmap bitmap = getBitmapFromUrl(product.getImageUrl());
        if (bitmap != null) {
            holder.productImageView.setImageBitmap(bitmap);
        } else {
            // Set a default image if bitmap is null (loading failed)
            holder.productImageView.setImageResource(R.drawable.ic_star_solid_grey); // Use a placeholder image
        }

        // Initially hide the review list
        holder.reviewListRecyclerView.setVisibility(View.GONE);

        // Handle "See All Reviews" button click
        holder.seeAllReviewsButton.setOnClickListener(v -> {
            toggleReviewsVisibility(holder);
        });

        // Handle "Review Now" button click
        holder.reviewNowButton.setOnClickListener(v -> showReviewDialog(product.getId(), holder));
    }

    private void toggleReviewsVisibility(ProductViewHolder holder) {
        if (holder.reviewListRecyclerView.getVisibility() == View.VISIBLE) {
            holder.reviewListRecyclerView.setVisibility(View.GONE);
            holder.seeAllReviewsButton.setText("All Reviews"); // Change button text back
        } else {
            // Only fetch reviews when showing the list
            List<Review> reviews = reviewDao.getReviewsByProduct(holder.getAdapterPosition() + 1);
            ReviewListAdapter reviewListAdapter = new ReviewListAdapter(reviews, context);
            holder.reviewListRecyclerView.setAdapter(reviewListAdapter);
            holder.reviewListRecyclerView.setLayoutManager(new LinearLayoutManager(context));
            holder.reviewListRecyclerView.setVisibility(View.VISIBLE);
            holder.seeAllReviewsButton.setText("Hide Reviews"); // Change button text
        }
    }

    // Helper method to show the custom dialog for adding a review
    private void showReviewDialog(int productId, ProductViewHolder holder) {
        View dialogView = LayoutInflater.from(context).inflate(R.layout.custom_review, null);

        LinearLayout starIconsLinearLayout = dialogView.findViewById(R.id.starIcons);
        TextView reviewEditText = dialogView.findViewById(R.id.editTextTextMultiLine);
        Button rateButton = dialogView.findViewById(R.id.button2);

        setRatingStars(starIconsLinearLayout, 0);

        for (int i = 0; i < starIconsLinearLayout.getChildCount(); i++) {
            ImageView starIcon = (ImageView) starIconsLinearLayout.getChildAt(i);
            final int rating = i + 1;

            starIcon.setOnClickListener(v -> {
                setRatingStars(starIconsLinearLayout, rating);
            });
        }

        // Directly handle the "Rate Now" button click in the dialog
        rateButton.setOnClickListener(view -> {
            String reviewText = reviewEditText.getText().toString();
            int rating = getSelectedRating(starIconsLinearLayout);

            if (reviewText.isEmpty()) {
                Toast.makeText(context, "Please enter a review", Toast.LENGTH_SHORT).show();
                return;
            }

            if (rating == 0) {
                Toast.makeText(context, "Please select a rating", Toast.LENGTH_SHORT).show();
                return;
            }

            Review newReview = new Review();
            newReview.setProductId(productId);
            newReview.setReviewText(reviewText);
            newReview.setRating(rating);

            reviewDao.insertReview(newReview);
            Toast.makeText(context, "Review submitted successfully!", Toast.LENGTH_SHORT).show();
            notifyDataSetChanged();
        });

        // Create and show the dialog (without the Submit/Cancel buttons)
        new AlertDialog.Builder(context)
                .setView(dialogView)
                .show();
    }


    private float calculateAverageRating(int productId) {
        List<Review> reviews = reviewDao.getReviewsByProduct(productId);
        if (reviews.isEmpty()) {
            return 0f;
        }

        int totalRating = 0;
        for (Review review : reviews) {
            totalRating += review.getRating();
        }
        return (float) totalRating / reviews.size();
    }

    private Bitmap getBitmapFromUrl(String url) {
        try {
            File file = new File(url);
            return BitmapFactory.decodeStream(new FileInputStream(file));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }



    private int getSelectedRating(LinearLayout starIconsLinearLayout) {
        int rating = 0;
        for (int i = 0; i < starIconsLinearLayout.getChildCount(); i++) {
            ImageView starIcon = (ImageView) starIconsLinearLayout.getChildAt(i);
            // Check for a tag indicating the selected state
            if (starIcon.getTag() != null && starIcon.getTag().toString().equals("selected")) {
                rating++;
            } else {
                break; // Stop counting if a star isn't selected
            }
        }
        return rating;
    }

    private void setRatingStars(LinearLayout starIconsLinearLayout, float rating) {
        for (int i = 0; i < starIconsLinearLayout.getChildCount(); i++) {
            ImageView starIcon = (ImageView) starIconsLinearLayout.getChildAt(i);
            if (i < rating) {
                starIcon.setImageResource(R.drawable.ic_star_solid);
                starIcon.setTag("selected"); // Set a tag to indicate selected
            } else {
                starIcon.setImageResource(R.drawable.ic_star_solid_grey);
                starIcon.setTag(null);       // Clear the tag
            }
        }
    }


    @Override
    public int getItemCount() {
        return productList.size();
    }

    public void setProducts(List<Product> products) {
        this.productList = products;
        notifyDataSetChanged();
    }

    static class ProductViewHolder extends RecyclerView.ViewHolder {
        ImageView productImageView;
        TextView productNameTextView;
        LinearLayout starIconsLinearLayout;
        Button seeAllReviewsButton;
        Button reviewNowButton;
        RecyclerView reviewListRecyclerView;

        ProductViewHolder(View itemView) {
            super(itemView);
            productImageView = itemView.findViewById(R.id.imageView);
            productNameTextView = itemView.findViewById(R.id.review_product_name);
            starIconsLinearLayout = itemView.findViewById(R.id.starIcons);
            seeAllReviewsButton = itemView.findViewById(R.id.see_all);
            reviewNowButton = itemView.findViewById(R.id.review_now);
            reviewListRecyclerView = itemView.findViewById(R.id.review_list);

        }
    }
}