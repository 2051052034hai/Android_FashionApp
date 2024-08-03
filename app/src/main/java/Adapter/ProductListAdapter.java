package Adapter;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.RecyclerView;
import com.example.fashion_app.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import Entities.Product;

public class ProductListAdapter extends RecyclerView.Adapter<ProductListAdapter.ProductListViewHolder>{
    private List<Product> productList;
    private Context context;
    private OnItemClickListener listener;

    public ProductListAdapter(List<Product> productList, Context context) {
        this.productList = productList;
        this.context = context;
    }

    public interface OnItemClickListener {
        void onItemClick(Product product);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ProductListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_product_list, parent, false);
        return new ProductListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ProductListViewHolder holder, int position) {
        Product product = productList.get(position);

        // Bind data to views
        holder.productName.setText(product.getProductName());

        // Load product image
        //holder.productImage.setImageResource(product.getImageUrl());

        // Set click listener for the action button
        holder.txtAction.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(context, holder.txtAction);
            popupMenu.inflate(R.menu.product_list_actions);
            popupMenu.setOnMenuItemClickListener(item -> {
                switch (item.getItemId()) {
                    case R.id.action_edit:
                        // Handle edit action
                        return true;
                    case R.id.action_delete:
                        // Handle delete action
                        return true;
                    default:
                        return false;
                }
            });
            popupMenu.show();
        });
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    static class ProductListViewHolder extends RecyclerView.ViewHolder {

        ImageView productImage;
        TextView productName;
        //TextView productStockQuantity;
        TextView txtAction;

        public ProductListViewHolder(View itemView) {
            super(itemView);
            productImage = itemView.findViewById(R.id.product_image);
            productName = itemView.findViewById(R.id.product_name);
            //productStockQuantity = itemView.findViewById(R.id.product_stock_quantity);
            txtAction = itemView.findViewById(R.id.btn_DropdownMenu);
        }
    }
}
