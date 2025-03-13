package com.example.findnest.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.findnest.R;
import com.example.findnest.utils.DatabaseHelper;

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
        btnListProduct = findViewById(R.id.btn_list_product);
        btnSearch = findViewById(R.id.btn_search);
        btnContacts = findViewById(R.id.btn_contact);
        btnStatistic = findViewById(R.id.btn_statistic);

        btnListProduct.setOnClickListener(view -> {
            Intent intent  = new Intent(MainActivity.this, ListProductActivity.class);
            startActivity(intent);
        });

        btnSearch.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, SearchProductActivity.class);
            startActivity(intent);
        });

        btnContacts.setOnClickListener(view -> {

        });

        btnStatistic.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, StatisticActivity.class);
            startActivity(intent);
        });

    }
}












































