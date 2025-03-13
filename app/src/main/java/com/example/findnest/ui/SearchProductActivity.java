package com.example.findnest.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.findnest.R;
import com.example.findnest.model.ProductEntity;
import com.example.findnest.repository.Repository;
import com.example.findnest.ui.adapters.ProductAdapter;
import java.util.List;

public class SearchProductActivity extends AppCompatActivity {

    private EditText edtSearchQuery, edtMinPrice, edtMaxPrice;
    private Spinner spinnerFilter;
    private Button btnSearch;
    private RecyclerView recyclerView;
    private Repository repository;
    private ProductAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_product);

        repository = new Repository(this);


        spinnerFilter = findViewById(R.id.spinner_filter);
        edtSearchQuery = findViewById(R.id.edt_search_query);
        edtMinPrice = findViewById(R.id.edt_min_price);
        edtMaxPrice = findViewById(R.id.edt_max_price);
        btnSearch = findViewById(R.id.btn_search);
        recyclerView = findViewById(R.id.recycle_search_results);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));


        String[] searchOptions = {"Category", "Name", "Count", "Price"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, searchOptions);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFilter.setAdapter(adapter);


        spinnerFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selected = searchOptions[position];
                if (selected.equals("Price")) {
                    edtSearchQuery.setVisibility(View.GONE);
                    edtMinPrice.setVisibility(View.VISIBLE);
                    edtMaxPrice.setVisibility(View.VISIBLE);
                } else {
                    edtSearchQuery.setVisibility(View.VISIBLE);
                    edtMinPrice.setVisibility(View.GONE);
                    edtMaxPrice.setVisibility(View.GONE);
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Xử lý tìm kiếm
        btnSearch.setOnClickListener(v -> performSearch());
    }

    private void performSearch() {
        String filter = spinnerFilter.getSelectedItem().toString();
        List<ProductEntity> results;

        if (filter.equals("Price")) {
            int minPrice = edtMinPrice.getText().toString().isEmpty() ? 0 : Integer.parseInt(edtMinPrice.getText().toString());
            int maxPrice = edtMaxPrice.getText().toString().isEmpty() ? Integer.MAX_VALUE : Integer.parseInt(edtMaxPrice.getText().toString());
            if (minPrice > maxPrice) Toast.makeText(this, "Invalid input data", Toast.LENGTH_SHORT).show();
            results = repository.searchByPriceRange(minPrice, maxPrice);
        } else {
            String keyword = edtSearchQuery.getText().toString().trim();
            results = repository.searchByField(filter, keyword);
        }
        if (results.isEmpty()){
            Toast.makeText(this, "There are no items found", Toast.LENGTH_SHORT).show();
        }

        adapter = new ProductAdapter(results);
        recyclerView.setAdapter(adapter);
    }
}
