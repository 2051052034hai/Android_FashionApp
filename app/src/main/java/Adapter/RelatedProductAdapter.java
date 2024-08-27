package Adapter;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import Entities.Product;
import com.bumptech.glide.Glide;
import com.example.fashion_app.ProductDetailActivity;
import com.example.fashion_app.R;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class RelatedProductAdapter extends RecyclerView.Adapter<RelatedProductAdapter.RelatedProductViewHolder> {

    private List<Product> relatedProducts;
    private Context context;

    public RelatedProductAdapter(List<Product> relatedProducts, Context context) {
        this.relatedProducts = relatedProducts;
        this.context = context;
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
        double priceValue = product.getPrice();

        // Format the price with a dot separator and add " đ"
        NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.GERMANY);
        String formattedPrice = numberFormat.format(priceValue) + " đ";

        Glide.with(holder.itemView.getContext())
                .load(product.getImageUrl())
                .placeholder(R.drawable.white_product)
                .error(R.drawable.white_product)
                .into(holder.relatedProductImage);

        holder.relatedProductName.setText(product.getName());
        holder.relatedProductPrice.setText(formattedPrice);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start ProductDetailActivity with the clicked product's ID
                Intent intent = new Intent(context, ProductDetailActivity.class);
                intent.putExtra("PRODUCT_ID", product.getId()); // assuming Product has a method getId() to get the product ID
                context.startActivity(intent);
            }
        });
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
