
package com.example.dogcare;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dogcare.interfaces.ProductDao;
import com.example.dogcare.classes.CartItem;
import com.example.dogcare.classes.Product;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ItemCartArray extends RecyclerView.Adapter<ItemCartArray.ViewHolder> {
    private Context context;
    private List<CartItem> cartItems;
    private ProductDao productDao;
    private SharedPreferences sharedPreferences;
    // Interface for Cart Updates
    public interface CartUpdateListener {
        void onCartUpdated(double cartTotal);
    }

    private CartUpdateListener cartUpdateListener;

    public void setCartUpdateListener(CartUpdateListener listener) {
        this.cartUpdateListener = listener;
    }

    // Constructor
    public ItemCartArray(Context context, ProductDao productDao) {
        this.context = context;
        this.cartItems = new ArrayList<>();
        this.productDao = productDao;
        this.sharedPreferences = context.getSharedPreferences("cart", Context.MODE_PRIVATE);
        loadCartItemsFromSharedPrefs();
    }

    // ViewHolder for Cart Items
    public class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView productImageView;
        public TextView productNameTextView;
        public TextView productDetailsTextView;
        public TextView productPriceTextView;
        public TextView itemTotalTextView;
        public TextView qtyTextView;
        public ImageButton minusButton;
        public ImageButton plusButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            // Initialize your views here (Similar to ProductCustomArrayAdapter)
            productImageView = itemView.findViewById(R.id.productImageView);
            productNameTextView = itemView.findViewById(R.id.productNameTextView);
            productDetailsTextView = itemView.findViewById(R.id.productDetailsTextView);
            productPriceTextView = itemView.findViewById(R.id.productPriceTextView);
            itemTotalTextView = itemView.findViewById(R.id.itemTotal);
            qtyTextView = itemView.findViewById(R.id.qtyTextView);
            minusButton = itemView.findViewById(R.id.minusButton);
            plusButton = itemView.findViewById(R.id.plusButton);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View listItemView = LayoutInflater.from(context).inflate(R.layout.cart, parent, false);
        return new ViewHolder(listItemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {


        CartItem currentItem = cartItems.get(position);
        Product currentProduct = currentItem.getProduct();

        Bitmap bitmap = getBitmapFromUrl(currentProduct.getImageUrl());
        holder.productImageView.setImageBitmap(bitmap);
        holder.productNameTextView.setText(currentProduct.getName());
        holder.productDetailsTextView.setText(currentProduct.getDescription());
        holder.productPriceTextView.setText("$" + String.format("%.2f", currentProduct.getPrice()));
        holder.qtyTextView.setText(String.valueOf(currentItem.getQuantity()));

        // Calculate and display the item total
        double itemTotal = currentProduct.getPrice() * currentItem.getQuantity();
        holder.itemTotalTextView.setText("$" + String.format("%.2f", itemTotal));


        // Minus Button
        holder.minusButton.setOnClickListener(view -> {
            adjustQuantity(position, -1);
        });

        // Plus Button
        holder.plusButton.setOnClickListener(view -> {
            adjustQuantity(position, 1);
        });
    }

    @Override
    public int getItemCount() {
        return cartItems.size();
    }

    // Helper to load data from SharedPreferences
    private void loadCartItemsFromSharedPrefs() {
        cartItems.clear(); // Clear existing items

        String cartString = sharedPreferences.getString("cart", null);
        if (cartString != null) {
            HashMap<String, String> cartProducts = deserializeCart(cartString);

            for (Map.Entry<String, String> entry : cartProducts.entrySet()) {
                int productId = Integer.parseInt(entry.getKey());
                int quantity = Integer.parseInt(entry.getValue());

                // Get product from database
                Product product = productDao.getProductById(productId);

                if (product != null) {
                    CartItem cartItem = new CartItem(product, quantity);
                    cartItems.add(cartItem);
                }
            }
        }
    }

    // Helper Methods (Similar to what you had before)
    private HashMap<String, String> deserializeCart(String cartString) {
        HashMap<String, String> cartProducts = new HashMap<>();
        String[] products = cartString.split(",");
        for (String product : products) {
            String[] keyValue = product.split(":");
            if (keyValue.length == 2) { // Make sure you have key:value
                cartProducts.put(keyValue[0], keyValue[1]);
            }
        }
        return cartProducts;
    }

    private String serializeCart(HashMap<String, String> cartProducts) {
        StringBuilder cartString = new StringBuilder();
        for (Map.Entry<String, String> entry : cartProducts.entrySet()) {
            cartString.append(entry.getKey()).append(":").append(entry.getValue()).append(",");
        }
        // Remove trailing comma if it exists
        if (cartString.length() > 0 && cartString.charAt(cartString.length() - 1) == ',') {
            cartString.deleteCharAt(cartString.length() - 1);
        }
        return cartString.toString();
    }

    private Bitmap getBitmapFromUrl(String url) {
        try {
            File file = new File(url);
            return BitmapFactory.decodeStream(new FileInputStream(file));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            // Handle the error appropriately (e.g., display a placeholder image)
            return null;
        }
    }

    // Method to remove an item from the cart
    private void removeItem(int position) {
        cartItems.remove(position);
        notifyItemRemoved(position);
        saveCartToSharedPrefs(); // Update SharedPreferences

        // After updating the quantity, notify the listener
        if (cartUpdateListener != null) {
            cartUpdateListener.onCartUpdated(calculateCartTotal());
        }
    }

    // Method to adjust item quantity
    @SuppressLint("NotifyDataSetChanged")
    private void adjustQuantity(int position, int quantityChange) {
        CartItem cartItem = cartItems.get(position);
        int newQuantity = cartItem.getQuantity() + quantityChange;

        if (newQuantity <= 0) {
            removeItem(position); // Remove if quantity becomes 0 or less
        } else {
            cartItem.setQuantity(newQuantity);
            notifyDataSetChanged();
            saveCartToSharedPrefs(); // Update SharedPreferences
        }

        // After updating the quantity, notify the listener
        if (cartUpdateListener != null) {
            cartUpdateListener.onCartUpdated(calculateCartTotal());
        }
    }

    // Method to save cart data to SharedPreferences
    private void saveCartToSharedPrefs() {
        HashMap<String, String> cartProducts = new HashMap<>();
        for (CartItem cartItem : cartItems) {
            cartProducts.put(String.valueOf(cartItem.getProduct().getId()), String.valueOf(cartItem.getQuantity()));
        }

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("cart", serializeCart(cartProducts));
        editor.apply();
    }

    // Method to calculate the total cart value
    public double calculateCartTotal() {
        double total = 0;
        for (CartItem item : cartItems) {
            total += item.getProduct().getPrice() * item.getQuantity();
        }
        return total;
    }

    public List<CartItem> getCartItems() {
        return cartItems;
    }

    public void clearCartItems() {
        cartItems.clear();
    }
}
