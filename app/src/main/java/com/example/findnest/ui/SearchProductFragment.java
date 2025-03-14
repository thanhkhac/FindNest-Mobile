package com.example.findnest.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.findnest.R;
import com.example.findnest.model.ProductEntity;
import com.example.findnest.repository.Repository;
import com.example.findnest.ui.adapters.ProductAdapter;

import java.util.List;

public class SearchProductFragment extends Fragment {

    private EditText edtSearchQuery, edtMinPrice, edtMaxPrice;
    private Button btnMenu, btnSearch;
    private RecyclerView recyclerView;
    private Repository repository;
    private ProductAdapter adapter;
    private String selectedFilter = "Category";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_search_product, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        repository = new Repository(requireContext());

        btnMenu = view.findViewById(R.id.btn_menu);
        edtSearchQuery = view.findViewById(R.id.edt_search_query);
        edtMinPrice = view.findViewById(R.id.edt_min_price);
        edtMaxPrice = view.findViewById(R.id.edt_max_price);
        btnSearch = view.findViewById(R.id.btn_search);
        recyclerView = view.findViewById(R.id.recycle_search_results);

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        btnMenu.setOnClickListener(this::showContextMenu);
        btnSearch.setOnClickListener(v -> performSearch());
    }

    private void showContextMenu(View view) {
        PopupMenu popup = new PopupMenu(requireContext(), view);
        popup.getMenuInflater().inflate(R.menu.search_filter_menu, popup.getMenu());

        popup.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case R.id.filter_category:
                    selectedFilter = "Category";
                    break;
                case R.id.filter_name:
                    selectedFilter = "Name";
                    break;
                case R.id.filter_count:
                    selectedFilter = "Count";
                    break;
                case R.id.filter_price:
                    selectedFilter = "Price";
                    break;
            }
            updateUIForFilter();
            return true;
        });
        popup.show();
    }

    private void updateUIForFilter() {
        if ("Price".equals(selectedFilter)) {
            edtSearchQuery.setVisibility(View.GONE);
            edtMinPrice.setVisibility(View.VISIBLE);
            edtMaxPrice.setVisibility(View.VISIBLE);
        } else {
            edtSearchQuery.setVisibility(View.VISIBLE);
            edtMinPrice.setVisibility(View.GONE);
            edtMaxPrice.setVisibility(View.GONE);
        }
    }

    private void performSearch() {
        List<ProductEntity> results;

        if ("Price".equals(selectedFilter)) {
            int minPrice = edtMinPrice.getText().toString().isEmpty() ? 0 : Integer.parseInt(edtMinPrice.getText().toString());
            int maxPrice = edtMaxPrice.getText().toString().isEmpty() ? Integer.MAX_VALUE : Integer.parseInt(edtMaxPrice.getText().toString());
            if (minPrice > maxPrice) {
                Toast.makeText(requireContext(), "Invalid input data", Toast.LENGTH_SHORT).show();
                return;
            }
            results = repository.searchByPriceRange(minPrice, maxPrice);
        } else {
            String keyword = edtSearchQuery.getText().toString().trim();
            results = repository.searchByField(selectedFilter, keyword);
        }

        if (results.isEmpty()) {
            Toast.makeText(requireContext(), "There are no items found", Toast.LENGTH_SHORT).show();
        }

        adapter = new ProductAdapter(results, productId -> openProductDetailFragment(productId, "update"));
        recyclerView.setAdapter(adapter);
    }

    private void openProductDetailFragment(int productId, String mode) {
        ProductDetailFragment fragment = ProductDetailFragment.newInstance(productId, mode);
        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        transaction.replace(R.id.frame_container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }
}