package Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fashion_app.R;
import com.example.fashion_app.ViewOdersActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import Entities.Orders;
import Entities.Product;
import Entities.User;

public class OrdersListAdapter extends RecyclerView.Adapter<OrdersListAdapter.OrdersListViewHolder>{

    private Context context;
    private List<Orders> ordersList;
    private OrdersListAdapter.OnItemClickListener listener;
    private User user;

    public OrdersListAdapter(List<Orders> ordersList, Context context) {
        this.ordersList = ordersList;
        this.context = context;
    }

    public interface OnItemClickListener {
        void onItemClick(Orders orders);
    }

    public void setOnItemClickListener(OrdersListAdapter.OnItemClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public OrdersListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_orders_list, parent, false);
        return new OrdersListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(OrdersListViewHolder holder, int position) {
        Orders orders = ordersList.get(position);
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");
        //Load Orders
        String formattedDateStr = formatCreatedDate(orders.getCreatedDate());
        holder.ordersCreatedDate.setText(formattedDateStr);
        // Fetch user data based on userId
        usersRef.child(orders.getUserId()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    user = dataSnapshot.getValue(User.class);
                    if (user != null) {
                        holder.ordersUserName.setText(user.getUserName());
                    }
                } else {
                    holder.ordersUserName.setText("Unknown User");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle possible errors.
                holder.ordersUserName.setText("Error Loading User");
            }
        });

        // Handle txtView click to transition to a new page
        holder.txtView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create an Intent to start the new activity
                Intent intent = new Intent(v.getContext(), ViewOdersActivity.class);

                // Pass necessary data with the intent (e.g., order ID)
                intent.putExtra("ORDER_ID", orders.getId());
                intent.putExtra("USERNAME", user.getUserName());
                intent.putExtra("CREATEDDATE", orders.getCreatedDate());

                // Start the new activity
                v.getContext().startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return ordersList.size();
    }

    static class OrdersListViewHolder extends RecyclerView.ViewHolder {
        TextView ordersUserName;
        TextView ordersCreatedDate;
        ImageView txtView;

        public OrdersListViewHolder(View itemView) {
            super(itemView);
            ordersUserName = itemView.findViewById(R.id.ordersUserName);
            ordersCreatedDate = itemView.findViewById(R.id.ordersCreatedDate);
            txtView = itemView.findViewById(R.id.btn_view_item);

        }
    }

    private String formatCreatedDate(String createdDateStr) {
        // Original date format
        SimpleDateFormat originalFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        // Desired date format
        SimpleDateFormat targetFormat = new SimpleDateFormat("dd/MM/yyyy HH'h' mm 'phút'");
        Date date = null;
        try {
            // Parse the original date string
            date = originalFormat.parse(createdDateStr);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        // Format the date into the desired string
        return targetFormat.format(date);
    }

    // Method to update the product list and refresh the adapter
    public void updateList(List<Orders> newList) {
        ordersList.clear();
        ordersList.addAll(newList);
        notifyDataSetChanged();
    }

}

