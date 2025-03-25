package com.example.findnest.ui.activity;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.findnest.R;
import com.example.findnest.ui.fragment.AccountFragment;
import com.example.findnest.ui.fragment.CommentFragment;
import com.example.findnest.ui.fragment.HomeFragment;
import com.example.findnest.ui.fragment.PostDetailFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {


    @Override
    //Bundle savedInstanceState:
    // Tham số này chứa trạng thái trước đó của Activity nếu nó bị hủy và tạo lại
    // ví dụ: sau khi xoay màn hình hoặc bị hệ thống kill để tiết kiệm tài nguyên
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Phương thức của Activity, dùng để gắn một tệp layout XML làm giao diện chính cho Activity.
        //R.layout.activity_main: Tham chiếu đến tệp activity_main.xml trong thư mục res/layout.
        setContentView(R.layout.activity_main);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;

            switch (item.getItemId()) {
                case R.id.nav_home:
                    selectedFragment = PostDetailFragment.newInstance("92017737-39e8-4f94-80cb-f4a6d2c44dcf");
                    break;
                case R.id.nav_search:
                    selectedFragment = new CommentFragment();

                    break;
                case R.id.nav_profile:
                    selectedFragment = new AccountFragment();
                    break;
            }

            if (selectedFragment != null) {
                getSupportFragmentManager() //Trả về một FragmentManager từ AndroidX, quản lý các Fragment trong Activity
                        .beginTransaction()
                        //Thêm Fragment mới vào container (Tìm theo id)
                        .replace(R.id.frame_container, selectedFragment)
                        .commit();
            }

            return true;
        });

        // Đặt fragement mặc định
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.frame_container, new HomeFragment())
                .commit();
    }
}