package com.example.dogcare;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.example.dogcare.interfaces.OrderDao;
import com.example.dogcare.interfaces.OrderItemDao;
import com.example.dogcare.interfaces.ProductDao;
import com.example.dogcare.interfaces.ReviewDao;
import com.example.dogcare.interfaces.UserDao;
import com.example.dogcare.classes.Order;
import com.example.dogcare.classes.OrderItem;
import com.example.dogcare.classes.Product;
import com.example.dogcare.classes.Review;
import com.example.dogcare.classes.User;

@Database(entities = {User.class, Product.class, Order.class, OrderItem.class, Review.class},exportSchema = false, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public abstract UserDao userDao();
    public abstract ProductDao productDao();
    public abstract OrderDao orderDao();
    public abstract OrderItemDao orderItemDao();
    public abstract ReviewDao reviewDao();

}
