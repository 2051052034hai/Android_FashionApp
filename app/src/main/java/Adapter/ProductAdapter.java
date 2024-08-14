package Adapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import Entities.Product;

import com.bumptech.glide.Glide;
import com.example.fashion_app.R;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    private List<Product> productList;
    private OnItemClickListener listener;

    public ProductAdapter(List<Product> productList) {
        this.productList = productList;
    }

    public interface OnItemClickListener {
        void onItemClick(Product product);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_item, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = productList.get(position);
        //Tải hình ảnh sản phẩm lên
        Glide.with(holder.itemView.getContext())
                .load(product.getImageUrl())
                .placeholder(R.drawable.white_product)
                .error(R.drawable.white_product)
                .into(holder.imageProduct);
        //Tải thông tin sản phẩm
        holder.bind(product, listener);
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    public static class ProductViewHolder extends RecyclerView.ViewHolder {
        ImageView imageProduct;
        TextView textProductName, textProductPrice;

        //Hàm hiển thị thông tin sản phẩm
        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            imageProduct = itemView.findViewById(R.id.image_product);
            textProductName = itemView.findViewById(R.id.text_product_name);
            textProductPrice = itemView.findViewById(R.id.text_product_price);
        }

        //Hiển thị thông tin sản phẩm theo ID
        public void bind(final Product product, final OnItemClickListener listener) {
            // Convert price string to a long value
            long priceValue = Long.parseLong(product.getPrice());

            // Format the price with a dot separator and add " vnđ"
            NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.GERMANY);
            String formattedPrice = numberFormat.format(priceValue) + " vnđ";

            // Bind data to views
            textProductName.setText(product.getName());
            textProductPrice.setText(formattedPrice);

            imageProduct.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        listener.onItemClick(product);
                    }
                }
            });
        }

    }
}
