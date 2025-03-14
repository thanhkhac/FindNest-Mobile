package com.example.findnest.ui;

import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.findnest.R;
import com.example.findnest.utils.DatabaseHelper;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity
{
    Button btnListProduct;
    Button btnSearch;
    Button btnContacts;
    Button btnStatistic;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);


        DatabaseHelper databaseHelper = new DatabaseHelper(this);
        databaseHelper.DB2SDCard();




        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;

            switch (item.getItemId()) {
                case R.id.nav_home:
                    selectedFragment = new ListProductFragment();
                    break;
                case R.id.nav_search:
                    selectedFragment = new SearchProductFragment();
                    break;
                case R.id.nav_statistic:
                    selectedFragment = new StatisticFragment();
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
                .replace(R.id.frame_container, new ListProductFragment())
                .commit();

    }
}












































