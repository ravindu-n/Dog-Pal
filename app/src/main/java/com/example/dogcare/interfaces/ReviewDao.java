package com.example.dogcare.interfaces;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.dogcare.classes.Review;

import java.util.List;

@Dao
public interface ReviewDao {
    @Insert
    void insertReview(Review review);

    @Update
    void updateReview(Review review);

    @Delete
    void deleteReview(Review review);

    @Query("SELECT * FROM reviews WHERE productId = :productId")
    List<Review> getReviewsByProduct(int productId);
}
