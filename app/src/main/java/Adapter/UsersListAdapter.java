package Adapter;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fashion_app.AddUserActivity;
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

import Entities.User;

public class UsersListAdapter extends RecyclerView.Adapter<UsersListAdapter.UsersListViewHolder>{
    private List<User> usersList;
    private Context context;
    private OnItemClickListener listener;

    public UsersListAdapter(List<User> usersList, Context context) {
        this.usersList = usersList;
        this.context = context;
    }

    public interface OnItemClickListener {
        void onItemClick(User user);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public UsersListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user_list, parent, false);
        return new UsersListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(UsersListViewHolder holder, int position) {
        User user = usersList.get(position);
        String roleUser = user.getRole() == 1 ? "Khách hàng": "Quản trị";
        // Bind data to views
        //Load product Name
        holder.userName.setText(user.getUserName());
        holder.userEmail.setText(user.getEmail());
        holder.userRole.setText(roleUser);

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
                        Intent intent = new Intent(context, AddUserActivity.class);
                        intent.putExtra("USER_ID", user.getId());
                        context.startActivity(intent);
                        return true;
                    case R.id.action_delete:
                        // Hiển thị một AlertDialog để xác nhận việc xóa
                        new AlertDialog.Builder(context)
                                .setTitle("Xác nhận xóa")
                                .setMessage("Bạn có chắc muốn xóa loại sản phẩm này?")
                                .setPositiveButton("OK", (dialog, which) -> {
                                    // Xử lý khi người dùng nhấn "OK"
                                    deleteUser(user);
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
        return usersList.size();
    }

    static class UsersListViewHolder extends RecyclerView.ViewHolder {
        TextView userName, userEmail, userRole;
        TextView txtAction;

        public UsersListViewHolder(View itemView) {
            super(itemView);
            userName = itemView.findViewById(R.id.user_name);
            userEmail = itemView.findViewById(R.id.user_Email);
            userRole = itemView.findViewById(R.id.user_Role);
            txtAction = itemView.findViewById(R.id.btn_DropdownMenu);
        }
    }

    //Hàm xử lý xoá loại sản phẩm
    private void deleteUser(User user) {
        // Nhận tham chiếu Cơ sở dữ liệu Firebase
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(user.getId());

        userRef.removeValue().addOnSuccessListener(aVoid -> {
            // Thông báo cho người dùng và cập nhật UI
            Snackbar.make(((Activity) context).findViewById(android.R.id.content), "Xóa người dùng thành công", Snackbar.LENGTH_LONG)
                    .setAction("OK", v -> {
                    })
                    .show();
            // Tải lại danh sách sản phẩm
            reloadUsersList();
        }).addOnFailureListener(e -> {
            // Không xóa được sản phẩm
            Toast.makeText(context, "Không xóa được người dùng", Toast.LENGTH_SHORT).show();
        });
    }

    //Xử lý load lại dữ liệu danh mục sản phẩm sau khi xóa
    private void reloadUsersList() {
        FirebaseDatabase.getInstance().getReference("users")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        usersList.clear();
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            User user = snapshot.getValue(User.class);
                            usersList.add(user);
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
    public void updateList(List<User> newList) {
        usersList.clear();
        usersList.addAll(newList);
        notifyDataSetChanged();
    }
}
