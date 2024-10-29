package com.example.dogcare;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.dogcare.classes.Order;
import com.example.dogcare.classes.OrderItem;
import java.util.List;

public class BillArray extends RecyclerView.Adapter<BillArray.OrderViewHolder> {

    private List<Order> orderList;
    private Context context;
    private AppDatabase db;

    public BillArray(Context context, List<Order> orderList, AppDatabase db) {
        this.context = context;
        this.orderList = orderList;
        this.db = db;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.bill, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Order order = orderList.get(position);
        holder.orderIdTextView.setText(String.valueOf(order.getId()));
        holder.totalAmountTextView.setText(String.format("$%.2f", order.getTotalPrice()));

        // Fetch Order Items for this order
        List<OrderItem> orderItems = db.orderItemDao().getOrderItemsByOrder(order.getId());

        // Set up nested RecyclerView for Order Items
        LinearLayoutManager layoutManager = new LinearLayoutManager(context);
        holder.orderItemsRecyclerView.setLayoutManager(layoutManager);
        BillItemArray billItemAdapter = new BillItemArray(orderItems,db);
        holder.orderItemsRecyclerView.setAdapter(billItemAdapter);
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    public static class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView orderIdTextView;
        TextView totalAmountTextView;
        RecyclerView orderItemsRecyclerView;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            orderIdTextView = itemView.findViewById(R.id.custom_order_id);
            totalAmountTextView = itemView.findViewById(R.id.custom_total_amount);
            orderItemsRecyclerView = itemView.findViewById(R.id.orderItemsRecyclerView);
        }
    }
}
