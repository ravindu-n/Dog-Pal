package com.example.dogcare;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dogcare.classes.Review;

import java.util.List;

public class ReviewListAdapter extends RecyclerView.Adapter<ReviewListAdapter.ReviewViewHolder> {

    private final List<Review> reviews;
    private final Context context;

    public ReviewListAdapter(List<Review> reviews, Context context) {
        this.reviews = reviews;
        this.context = context;
    }

    @NonNull
    @Override
    public ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.custom_review_item, parent, false);
        return new ReviewViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewViewHolder holder, int position) {
        Review review = reviews.get(position);

        // Assuming you'll fetch customer name from somewhere else (e.g., User table)
        holder.customerNameTextView.setText("Customer " + (position + 1)); // For now, just showing a placeholder
        holder.reviewTextView.setText(review.getReviewText());

        setRatingStars(holder.ratingStarsLinearLayout, review.getRating());
    }

    // Helper function to set star icons based on  rating
    private void setRatingStars(LinearLayout starIconsLinearLayout, float rating) {
        for (int i = 0; i < starIconsLinearLayout.getChildCount(); i++) {
            ImageView starIcon = (ImageView) starIconsLinearLayout.getChildAt(i);
            if (i < rating) {
                starIcon.setImageResource(R.drawable.ic_star_solid);
            } else if (i == (int) rating && rating - (int) rating >= 0.5) {
                starIcon.setImageResource(R.drawable.ic_star_half);
            } else {
                starIcon.setImageResource(R.drawable.ic_star_solid_grey);
            }
        }
    }


    @Override
    public int getItemCount() {
        return reviews.size();
    }

    static class ReviewViewHolder extends RecyclerView.ViewHolder {
        TextView customerNameTextView;
        TextView reviewTextView;
        LinearLayout ratingStarsLinearLayout;

        ReviewViewHolder(View itemView) {
            super(itemView);
            customerNameTextView = itemView.findViewById(R.id.customer_name);
            reviewTextView = itemView.findViewById(R.id.review);
            ratingStarsLinearLayout = itemView.findViewById(R.id.linearLayout2);
        }
    }
}