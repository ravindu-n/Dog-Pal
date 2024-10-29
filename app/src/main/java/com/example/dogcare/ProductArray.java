package com.example.dogcare;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dogcare.interfaces.ProductDao;
import com.example.dogcare.classes.Product;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProductArray extends RecyclerView.Adapter<ProductArray.ViewHolder> {
    private Context context;
    private List<Product> products;
    private ProductDao productDao;
    private boolean isUserCustomer;
    private ProductEditListener editListener;

    public interface ProductEditListener {
        void onEditProduct(Product product);
    }

    public ProductArray(Context context, List<Product> products, ProductDao productDao, boolean isUserCustomer, ProductEditListener editListener) {
        this.context = context;
        this.products = products;
        this.productDao = productDao;
        this.isUserCustomer = isUserCustomer;
        this.editListener = editListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View listItemView = LayoutInflater.from(context).inflate(R.layout.product, parent, false);
        return new ViewHolder(listItemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final Product currentProduct = products.get(position);

        Bitmap bitmap = getBitmapFromUrl(currentProduct.getImageUrl());
        holder.productImageView.setImageBitmap(bitmap);
        holder.productNameTextView.setText(currentProduct.getName());
        holder.productDetailsTextView.setText(currentProduct.getDescription());
        holder.productPriceTextView.setText("$" + String.format("%.2f", currentProduct.getPrice()));

        if (isUserCustomer) {
            holder.addToCartButton.setVisibility(View.VISIBLE);
            holder.editButton.setVisibility(View.GONE);
            holder.deleteButton.setVisibility(View.GONE);

            holder.addToCartButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SharedPreferences sharedPreferences = context.getSharedPreferences("cart", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();

                    // **Modified Part Start**
                    HashMap<String, String> cartProducts = loadCartFromSharedPrefs(sharedPreferences);

                    String currentProductId = String.valueOf(currentProduct.getId());
                    int qty = Integer.parseInt(cartProducts.getOrDefault(currentProductId, "0"));
                    cartProducts.put(currentProductId, String.valueOf(Integer.parseInt(String.valueOf(qty)) + 1));

                    saveCartToSharedPrefs(editor, cartProducts);
                    // **Modified Part End**

                    Toast.makeText(context, "Product added to cart", Toast.LENGTH_SHORT).show();
                    Log.d("cart", String.valueOf(cartProducts));
                }
            });
        } else {
            holder.editButton.setVisibility(View.VISIBLE);
            holder.deleteButton.setVisibility(View.VISIBLE);
            holder.addToCartButton.setVisibility(View.GONE);

            holder.editButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (editListener != null) {
                        // Notify the listener (ProductActivity) about the edit action
                        editListener.onEditProduct(currentProduct);
                    }
                }
            });

            holder.deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new AlertDialog.Builder(context)
                            .setTitle("Delete Product")
                            .setMessage("Are you sure you want to delete this product?")
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    deleteProduct(currentProduct);
                                }
                            })
                            .setNegativeButton(android.R.string.no, null)
                            .show();
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView productImageView;
        public TextView productNameTextView;
        public TextView productDetailsTextView;
        public TextView productPriceTextView;
        public Button editButton;
        public Button deleteButton;
        public Button addToCartButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            productImageView = itemView.findViewById(R.id.productImageView);
            productNameTextView = itemView.findViewById(R.id.productNameTextView);
            productDetailsTextView = itemView.findViewById(R.id.productDetailsTextView);
            productPriceTextView = itemView.findViewById(R.id.productPriceTextView);
            editButton = itemView.findViewById(R.id.editButton);
            deleteButton = itemView.findViewById(R.id.deleteButton);
            addToCartButton = itemView.findViewById(R.id.addToCartButton);
        }
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

    @SuppressLint("NotifyDataSetChanged")
    private void deleteProduct(Product product) {
        productDao.deleteProduct(product);
        products.remove(product);
        notifyDataSetChanged();
        Toast.makeText(context, "Product Deleted", Toast.LENGTH_SHORT).show();
    }

    // Add a method to update the product list
    @SuppressLint("NotifyDataSetChanged")
    public void updateProducts(List<Product> newProducts) {
        this.products.clear();
        this.products.addAll(newProducts);
        notifyDataSetChanged();
    }

    // Helper Methods (Add these to your ProductCustomArrayAdapter)
    private HashMap<String, String> loadCartFromSharedPrefs(SharedPreferences sharedPreferences) {
        HashMap<String, String> cartProducts = new HashMap<>();
        String cartString = sharedPreferences.getString("cart", null);
        if (cartString != null) {
            cartProducts = deserializeCart(cartString);
        }
        return cartProducts;
    }

    private void saveCartToSharedPrefs(SharedPreferences.Editor editor, HashMap<String, String> cartProducts) {
        String cartStringToSave = serializeCart(cartProducts);
        editor.putString("cart", cartStringToSave);
        editor.apply();
    }

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

}