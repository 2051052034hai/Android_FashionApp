package Adapter;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fashion_app.AddProductCategoryActivity;
import com.example.fashion_app.R;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import Entities.ProductCategory;

public class ProductCategoryListAdapter extends RecyclerView.Adapter<ProductCategoryListAdapter.ProductCategoryListViewHolder>{
    private List<ProductCategory> productCatList;
    private Context context;
    private OnItemClickListener listener;

    public ProductCategoryListAdapter(List<ProductCategory> productCatList, Context context) {
        this.productCatList = productCatList;
        this.context = context;
    }

    public interface OnItemClickListener {
        void onItemClick(ProductCategory productCat);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ProductCategoryListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_product_category_list, parent, false);
        return new ProductCategoryListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ProductCategoryListViewHolder holder, int position) {
        ProductCategory productCat = productCatList.get(position);

        // Bind data to views
        //Load product Name
        holder.productCatName.setText(productCat.getName());
        holder.productCatDescription.setText(productCat.getDescription());

        // Set click listener for the action button
        holder.txtAction.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(context, holder.txtAction);
            popupMenu.inflate(R.menu.product_list_actions);

            // Hide the menu item with id "action_view"
            MenuItem actionViewItem = popupMenu.getMenu().findItem(R.id.action_view);
            if (actionViewItem != null) {
                actionViewItem.setVisible(false);
            }

            popupMenu.setOnMenuItemClickListener(item -> {
                switch (item.getItemId()) {
                    case R.id.action_edit:
                        Intent intent = new Intent(context, AddProductCategoryActivity.class);
                        intent.putExtra("PRODUCTCAT_ID", productCat.getId());
                        context.startActivity(intent);
                        return true;
                    case R.id.action_delete:
                        // Hiển thị một AlertDialog để xác nhận việc xóa
                        new AlertDialog.Builder(context)
                                .setTitle("Xác nhận xóa")
                                .setMessage("Bạn có chắc muốn xóa loại sản phẩm này?")
                                .setPositiveButton("OK", (dialog, which) -> {
                                    // Xử lý khi người dùng nhấn "OK"
                                    deleteProduct(productCat);
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
        return productCatList.size();
    }

    static class ProductCategoryListViewHolder extends RecyclerView.ViewHolder {
        TextView productCatName;
        TextView productCatDescription;
        TextView txtAction;

        public ProductCategoryListViewHolder(View itemView) {
            super(itemView);
            productCatName = itemView.findViewById(R.id.productCat_name);
            productCatDescription = itemView.findViewById(R.id.productCat_Description);
            txtAction = itemView.findViewById(R.id.btn_DropdownMenu);
        }
    }

    //Hàm xử lý xoá loại sản phẩm
    private void deleteProduct(ProductCategory productCat) {
        // Nhận tham chiếu Cơ sở dữ liệu Firebase
        DatabaseReference productCatRef = FirebaseDatabase.getInstance().getReference("categories").child(productCat.getId());

        productCatRef.removeValue().addOnSuccessListener(aVoid -> {
            // Thông báo cho người dùng và cập nhật UI
            Snackbar.make(((Activity) context).findViewById(android.R.id.content), "Xóa loại sản phẩm thành công", Snackbar.LENGTH_LONG)
                    .setAction("OK", v -> {
                    })
                    .show();
            // Tải lại danh sách sản phẩm
            reloadProductList();
        }).addOnFailureListener(e -> {
            // Không xóa được sản phẩm
            Toast.makeText(context, "Không xóa được loại sản phẩm", Toast.LENGTH_SHORT).show();
        });
    }

    //Xử lý load lại dữ liệu danh mục sản phẩm sau khi xóa
    private void reloadProductList() {
        FirebaseDatabase.getInstance().getReference("categories")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        productCatList.clear();
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            ProductCategory productCat = snapshot.getValue(ProductCategory.class);
                            productCatList.add(productCat);
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
    public void updateList(List<ProductCategory> newList) {
        productCatList.clear();
        productCatList.addAll(newList);
        notifyDataSetChanged();
    }
}
