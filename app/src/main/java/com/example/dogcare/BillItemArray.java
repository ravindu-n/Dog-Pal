package com.example.dogcare;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.dogcare.classes.OrderItem;
import com.example.dogcare.classes.Product;
import java.util.List;

public class BillItemArray extends RecyclerView.Adapter<BillItemArray.BillItemViewHolder> {

    private List<OrderItem> orderItemsList;
    private AppDatabase db;

    public BillItemArray(List<OrderItem> orderItemsList, AppDatabase db) {
        this.orderItemsList = orderItemsList;
        this.db = db;
    }

    @NonNull
    @Override
    public BillItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.bill_item, parent, false);
        return new BillItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BillItemViewHolder holder, int position) {
        OrderItem orderItem = orderItemsList.get(position);
        Product product = getProductFromDb(orderItem.getProductId());

        if (product != null) {
            holder.itemIdTextView.setText(String.valueOf(product.getId())); // Assuming 'id' is the field for product ID
            holder.itemNameTextView.setText(product.getName());
            holder.itemAmountTextView.setText(String.format("$%.2f", product.getPrice())); // Assuming 'price' is the field for product price
            holder.itemQuantityTextView.setText("x" + orderItem.getQuantity());
            holder.itemTotalTextView.setText(String.format("$%.2f", orderItem.getSubtotal()));
        } else {
            // Handle the case where the product is not found (maybe set some default text)
            holder.itemIdTextView.setText("N/A");
            holder.itemNameTextView.setText("Product Not Found");
            holder.itemAmountTextView.setText("N/A");
            holder.itemQuantityTextView.setText("N/A");
            holder.itemTotalTextView.setText("N/A");
        }
    }

    //  Important: Execute database operations on a background thread
    private Product getProductFromDb(int productId) {
        return db.productDao().getProductById(productId);
    }

    @Override
    public int getItemCount() {
        return orderItemsList.size();
    }

    public static class BillItemViewHolder extends RecyclerView.ViewHolder {
        TextView itemIdTextView; // Added for Item Id
        TextView itemNameTextView;
        TextView itemAmountTextView; // Added for individual item amount
        TextView itemQuantityTextView;
        TextView itemTotalTextView;

        public BillItemViewHolder(@NonNull View itemView) {
            super(itemView);
            itemIdTextView = itemView.findViewById(R.id.item_id);   // Connect to item_id
            itemNameTextView = itemView.findViewById(R.id.item_name);
            itemAmountTextView = itemView.findViewById(R.id.item_amount); // Connect to item_amount
            itemQuantityTextView = itemView.findViewById(R.id.item_qty);
            itemTotalTextView = itemView.findViewById(R.id.item_total);
        }
    }
}