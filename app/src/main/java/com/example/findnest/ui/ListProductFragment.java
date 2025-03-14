package com.example.findnest.ui;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.findnest.R;
import com.example.findnest.model.ProductEntity;
import com.example.findnest.repository.Repository;
import com.example.findnest.ui.adapters.ProductAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

public class ListProductFragment extends Fragment {

    private RecyclerView recyclerView;
    private Repository repository;
    private ProductAdapter adapter;
    private FloatingActionButton btnAdd;

    public ListProductFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_list_product, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        init(view);
        setup();
        loadProducts();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadProducts();
    }

    private void init(View view) {
        repository = new Repository(getContext());
        recyclerView = view.findViewById(R.id.recycle_product_list);
        btnAdd = view.findViewById(R.id.btn_add);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
    }

    private void setup() {
        btnAdd.setOnClickListener(view -> openProductDetailFragment(0, "insert"));
    }

    private void loadProducts() {
        List<ProductEntity> productEntityList = repository.getAll();
        for (ProductEntity productEntity : productEntityList) {
            Log.d("INFO loadProducts", productEntity.toString());
        }

        adapter = new ProductAdapter(productEntityList, productId -> openProductDetailFragment(productId, "update"));
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
