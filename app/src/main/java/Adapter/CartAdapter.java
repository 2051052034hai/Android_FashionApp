package Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import Entities.CartItem;
import java.util.List;
import com.example.fashion_app.R;
import Common.CartManager ;

public class CartAdapter extends BaseAdapter {
    private Context context;
    private  ImageView btn_delete_item;
    private List<CartItem> cartItems;
    private CartManager cartManager;
    private OnQuantityChangeListener quantityChangeListener;

    public CartAdapter(Context context, List<CartItem> cartItems, CartManager cartManager, OnQuantityChangeListener quantityChangeListener) {
        this.context = context;
        this.cartItems = cartItems;
        this.cartManager = cartManager;
        this.quantityChangeListener = quantityChangeListener;
    }

    @Override
    public int getCount() {
        return cartItems.size();
    }

    @Override
    public Object getItem(int position) {
        return cartItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.cart_item, parent, false);
        }

        CartItem item = cartItems.get(position);

        ImageView imageView = convertView.findViewById(R.id.cart_item_image);
        TextView nameView = convertView.findViewById(R.id.cart_item_name);
        TextView priceView = convertView.findViewById(R.id.cart_item_price);
        final TextView quantityView = convertView.findViewById(R.id.cart_item_quantity);
        TextView itemIncrease = convertView.findViewById(R.id.cart_item_increase);
        TextView itemDecrease = convertView.findViewById(R.id.cart_item_decrease);

        // Load image if you have a resource or URL for it
        // imageView.setImageResource(R.drawable.ic_product_image); // Use an actual image

        nameView.setText(item.getProductName());
        priceView.setText(String.format("Price: %.2fđ", item.getPrice()));
        quantityView.setText(String.format("x%d", item.getQuantity()));

        // Handle quantity increase
        itemIncrease.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                item.setQuantity(item.getQuantity() + 1);
                cartManager.updateCartItem(item);
                quantityView.setText(String.valueOf(item.getQuantity()));
                if (quantityChangeListener != null) {
                    quantityChangeListener.onQuantityChanged();
                }
                notifyDataSetChanged();
            }
        });

        // Handle quantity decrease
        itemDecrease.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (item.getQuantity() > 1) {
                    item.setQuantity(item.getQuantity() - 1);
                    cartManager.updateCartItem(item);
                    quantityView.setText(String.valueOf(item.getQuantity()));
                    if (quantityChangeListener != null) {
                        quantityChangeListener.onQuantityChanged();
                    }
                    notifyDataSetChanged();
                }
            }
        });

        // Handle delete item cart
        btn_delete_item = convertView.findViewById(R.id.btn_delete_item);
        btn_delete_item.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cartManager.handleDeleteCartItem(item);
                if (quantityChangeListener != null) {
                    quantityChangeListener.onQuantityChanged();
                }
                notifyDataSetChanged();
            }
        });

        return convertView;
    }
    public interface OnQuantityChangeListener {
        void onQuantityChanged();
    }
}
