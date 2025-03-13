package com.example.findnest.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.findnest.R;
import com.example.findnest.model.ProductEntity;
import com.example.findnest.repository.Repository;
import com.example.findnest.ui.adapters.ProductAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

public class ListProductActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private Repository repository;
    private ProductAdapter adapter;

    private FloatingActionButton btnAdd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_list_product);
        ///------------------------------------------------
        init();
        setup();
        loadProducts();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadProducts();
    }

    void init() {
        repository = new Repository(ListProductActivity.this);
        recyclerView = findViewById(R.id.recycle_product_list);
        btnAdd = findViewById(R.id.btn_add);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    void setup(){
        btnAdd.setOnClickListener(view -> {
            Intent intent = new Intent(this, ProductDetailActivity.class);
            intent.putExtra("mode", "insert");
            startActivity(intent);
        });
    }

    void loadProducts() {
        List<ProductEntity> productEntityList = repository.getAll();
        for (ProductEntity productEntity : productEntityList) {
            Log.d("INFO loadProducts", productEntity.toString());
        }
        adapter = new ProductAdapter(productEntityList);
        recyclerView.setAdapter(adapter);
    }
}