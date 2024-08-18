package com.example.fashion_app;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

import androidx.appcompat.app.AppCompatActivity;

public class TestShowMenuActivity extends AppCompatActivity {

    private ImageButton menuButton;
    private RelativeLayout menuLayout;
    private boolean isMenuVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_testshowmenu);

        menuButton = findViewById(R.id.menu_button);
        menuLayout = findViewById(R.id.menu_layout);

        menuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleMenu();
            }
        });
    }

    private void toggleMenu() {
        if (isMenuVisible) {
            menuLayout.setVisibility(View.GONE);
            menuButton.setImageResource(R.drawable.ic_menu); // Đặt lại thành dấu "="
        } else {
            menuLayout.setVisibility(View.VISIBLE);
            menuButton.setImageResource(R.drawable.ic_close); // Đặt thành dấu "x"
        }
        isMenuVisible = !isMenuVisible;
    }
}
