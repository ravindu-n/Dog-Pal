package com.example.dogcare.interfaces;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.dogcare.classes.Product;

import java.util.List;

@Dao
public interface ProductDao {
    @Insert
    void insertProduct(Product product);

    @Update
    void updateProduct(Product product);

    @Delete
    void deleteProduct(Product product);

    @Query("SELECT * FROM products")
    List<Product> getAllProducts();

    @Query("SELECT * FROM products WHERE brand = :brand")
    List<Product> getProductsByBrand(String brand);

    @Query("SELECT * FROM products WHERE type = :type")
    List<Product> getProductsByType(String type);

    @Query("SELECT * FROM products WHERE ageRange = :ageRange")
    List<Product> getProductsByAgeRange(int ageRange);

    @Query("SELECT * FROM products WHERE id = :productId")
    Product getProductById(int productId);
    @Query("SELECT * FROM products WHERE LOWER(name) LIKE '%' || :name || '%'")
    List<Product> getProductsByName(String name);
}
