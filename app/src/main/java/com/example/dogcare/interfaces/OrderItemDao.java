package com.example.dogcare.interfaces;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.dogcare.classes.OrderItem;

import java.util.List;

@Dao
public interface OrderItemDao {
    @Insert
    void insertOrderItem(OrderItem orderItem);

    @Update
    void updateOrderItem(OrderItem orderItem);

    @Delete
    void deleteOrderItem(OrderItem orderItem);

    @Query("SELECT * FROM order_items WHERE orderId = :orderId")
    List<OrderItem> getOrderItemsByOrder(int orderId);
}
