package Adapter;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.fashion_app.AddProductActivity;
import com.example.fashion_app.ViewProductActivity;
import com.example.fashion_app.R;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import Entities.Product;

public class ProductListAdapter extends RecyclerView.Adapter<ProductListAdapter.ProductListViewHolder>{
    private List<Product> productList;
    private Context context;
    private OnItemClickListener listener;

    private int checkView;

    public ProductListAdapter(List<Product> productList, Context context, int checkView) {
        this.productList = productList;
        this.context = context;
        this.checkView = checkView;
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
        //Load product Name
        holder.productName.setText(product.getName());
        // Load product image
        // Load product image using Glide
        Glide.with(holder.itemView.getContext())
                .load(product.getImageUrl()) // Tải hình ảnh từ URL
                .placeholder(R.drawable.white_product) // Hình ảnh giữ chỗ trong khi tải
                .error(R.drawable.white_product) // Hình ảnh lỗi nếu tải không thành công
                .into(holder.productImage); // ImageView nơi hình ảnh sẽ được tải



        // Set click listener for the action button
        holder.txtAction.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(context, holder.txtAction);
            popupMenu.inflate(R.menu.product_list_actions);

            MenuItem action_edit_Item =  popupMenu.getMenu().findItem(R.id.action_edit);
            MenuItem action_edit_Del =  popupMenu.getMenu().findItem(R.id.action_delete);

            if(checkView == 1){
                action_edit_Item.setVisible(false);
                action_edit_Del.setVisible(false);
            }

            popupMenu.setOnMenuItemClickListener(item -> {
                switch (item.getItemId()) {
                    case R.id.action_view:
                        // Handle view action
                        Intent intent = new Intent(context, ViewProductActivity.class);
                        intent.putExtra("PRODUCT_ID", product.getId());
                        context.startActivity(intent);
                        return true;
                    case R.id.action_edit:
                        intent = new Intent(context, AddProductActivity.class);
                        intent.putExtra("PRODUCT_ID", product.getId());
                        context.startActivity(intent);
                        return true;
                    case R.id.action_delete:
                        // Hiển thị một AlertDialog để xác nhận việc xóa
                        new AlertDialog.Builder(context)
                                .setTitle("Xác nhận xóa")
                                .setMessage("Bạn có chắc muốn xóa sản phẩm này?")
                                .setPositiveButton("OK", (dialog, which) -> {
                                    // Xử lý khi người dùng nhấn "OK"
                                    deleteProduct(product);
                                })
                                .setNegativeButton("Không", (dialog, which) -> {
                                    // Xử lý khi người dùng nhấn "Không" (nếu cần)
                                    dialog.dismiss();
                                })
                                .show();
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
        TextView txtAction;

        public ProductListViewHolder(View itemView) {
            super(itemView);
            productImage = itemView.findViewById(R.id.product_image);
            productName = itemView.findViewById(R.id.product_name);
            txtAction = itemView.findViewById(R.id.btn_DropdownMenu);
        }
    }

    //Hàm xử lý xoá sản phẩm
    private void deleteProduct(Product product) {
        // Nhận tham chiếu Cơ sở dữ liệu Firebase
        DatabaseReference productRef = FirebaseDatabase.getInstance().getReference("products").child(product.getId());

        // Kiểm tra nếu sản phẩm có URL hình ảnh không
        if (product.getImageUrl() != null && !product.getImageUrl().isEmpty()) {
            // Nhận tài liệu tham khảo về Firebase Storage
            StorageReference imageRef = FirebaseStorage.getInstance().getReferenceFromUrl(product.getImageUrl());

            // Xóa hình ảnh khỏi Firebase Storage
            imageRef.delete().addOnSuccessListener(aVoid -> {
                // Hình ảnh đã được xóa thành công
                deleteProductFromDatabase(productRef);
            }).addOnFailureListener(e -> {
                // Không xóa được hình ảnh
                Toast.makeText(context, "Không xóa được hình ảnh", Toast.LENGTH_SHORT).show();
            });
        } else {
            // Nếu không có URL hình ảnh, chỉ cần xóa sản phẩm khỏi Cơ sở dữ liệu
            deleteProductFromDatabase(productRef);
        }
    }

    //Hàm xử lý xoá sản phẩm
    private void deleteProductFromDatabase(DatabaseReference productRef) {
        productRef.removeValue().addOnSuccessListener(aVoid -> {
            // Thông báo cho người dùng và cập nhật UI
            Snackbar.make(((Activity) context).findViewById(android.R.id.content), "Xóa sản phẩm thành công", Snackbar.LENGTH_LONG)
                    .setAction("OK", v -> {
                    })
                    .show();
            // Tải lại danh sách sản phẩm
            reloadProductList();
        }).addOnFailureListener(e -> {
            // Không xóa được sản phẩm
            Toast.makeText(context, "Không xóa được sản phẩm", Toast.LENGTH_SHORT).show();
        });
    }

    //Xử lý load lại dữ liệu danh mục sản phẩm sau khi xóa
    private void reloadProductList() {
        FirebaseDatabase.getInstance().getReference("products")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        productList.clear();
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            Product product = snapshot.getValue(Product.class);
                            productList.add(product);
                        }
                        notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        // Xử lý các lỗi có thể xảy ra.
                    }
                });
    }

    // Method to update the product list and refresh the adapter
    public void updateList(List<Product> newList) {
        productList.clear();
        productList.addAll(newList);
        notifyDataSetChanged();
    }
}
