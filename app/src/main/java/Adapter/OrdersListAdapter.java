package Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fashion_app.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

import Entities.Orders;
import Entities.User;

public class OrdersListAdapter extends RecyclerView.Adapter<OrdersListAdapter.OrdersListViewHolder>{

    private Context context;
    private List<Orders> ordersList;
    private OrdersListAdapter.OnItemClickListener listener;

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

        // Format the price with a dot separator and add " đ"
        NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.GERMANY);
        String formattedPrice = numberFormat.format(orders.getTotalAmount()) + " đ";

        //Load Orders
        holder.ordersTotalAmount.setText( formattedPrice);

        // Fetch user data based on userId
        usersRef.child(orders.getUserId()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    User user = dataSnapshot.getValue(User.class);
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
    }

    @Override
    public int getItemCount() {
        return ordersList.size();
    }

    static class OrdersListViewHolder extends RecyclerView.ViewHolder {
        TextView ordersUserName;
        TextView ordersTotalAmount;
        TextView txtAction;

        public OrdersListViewHolder(View itemView) {
            super(itemView);
            ordersUserName = itemView.findViewById(R.id.ordersUserName);
            ordersTotalAmount = itemView.findViewById(R.id.ordersTotalAmount);
            txtAction = itemView.findViewById(R.id.btn_DropdownMenu);
        }
    }

}

