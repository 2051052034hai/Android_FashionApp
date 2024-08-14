package Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import Entities.Product;
import com.example.fashion_app.R;

import java.util.List;

public class RelatedProductAdapter extends RecyclerView.Adapter<RelatedProductAdapter.RelatedProductViewHolder> {

    private List<Product> relatedProducts;

    public RelatedProductAdapter(List<Product> relatedProducts) {
        this.relatedProducts = relatedProducts;
    }

    @NonNull
    @Override
    public RelatedProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.related_product_item, parent, false);
        return new RelatedProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RelatedProductViewHolder holder, int position) {
        Product product = relatedProducts.get(position);
        holder.relatedProductImage.setImageResource(Integer.parseInt(product.getImageUrl()));
        holder.relatedProductName.setText(product.getName());
        holder.relatedProductPrice.setText(product.getPrice());
        // Assuming you have a way to load images, e.g., using Glide or Picasso
        // Glide.with(holder.relatedProductImage.getContext()).load(product.getImageUrl()).into(holder.relatedProductImage);
    }

    @Override
    public int getItemCount() {
        return relatedProducts.size();
    }

    static class RelatedProductViewHolder extends RecyclerView.ViewHolder {
        ImageView relatedProductImage;
        TextView relatedProductName, relatedProductPrice;

        public RelatedProductViewHolder(@NonNull View itemView) {
            super(itemView);
            relatedProductImage = itemView.findViewById(R.id.relatedProductImage);
            relatedProductName = itemView.findViewById(R.id.relatedProductName);
            relatedProductPrice = itemView.findViewById(R.id.relatedProductPrice);
        }
    }
}
