package com.example.findnest.ui;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import com.example.findnest.R;
import com.example.findnest.model.ProductEntity;
import com.example.findnest.repository.Repository;

public class ProductDetailFragment extends Fragment {
    private static final String ARG_PRODUCT_ID = "id";
    private static final String ARG_MODE = "mode";

    private EditText edtId, edtCategory, edtName, edtCount, edtPrice;
    private Button btnEdit, btnDelete, btnInsert;
    private Repository repository;
    private int productId;
    private String mode;

    public static ProductDetailFragment newInstance(int id, String mode) {
        ProductDetailFragment fragment = new ProductDetailFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_PRODUCT_ID, id);
        args.putString(ARG_MODE, mode);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_product_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        repository = new Repository(requireContext());

        edtId = view.findViewById(R.id.edtid);
        edtCategory = view.findViewById(R.id.edtcategory);
        edtName = view.findViewById(R.id.edtname);
        edtCount = view.findViewById(R.id.edtcount);
        edtPrice = view.findViewById(R.id.edtprice);
        btnEdit = view.findViewById(R.id.btn_edit);
        btnDelete = view.findViewById(R.id.btn_delete);
        btnInsert = view.findViewById(R.id.btn_insert);

        if (getArguments() != null) {
            mode = getArguments().getString(ARG_MODE, "");
            productId = getArguments().getInt(ARG_PRODUCT_ID, -1);

            if ("update".equalsIgnoreCase(mode) && productId != -1) {
                btnInsert.setVisibility(View.GONE);
                edtId.setEnabled(false);
                loadProductData(productId);
            } else {
                btnDelete.setVisibility(View.GONE);
                btnEdit.setVisibility(View.GONE);
            }
        }

        btnEdit.setOnClickListener(v -> editProduct());
        btnDelete.setOnClickListener(v -> confirmDeleteProduct());
        btnInsert.setOnClickListener(v -> insertProduct());
    }

    private void loadProductData(int id) {
        ProductEntity product = repository.getById(id);
        if (product != null) {
            edtId.setText(String.valueOf(product.getId()));
            edtCategory.setText(product.getCategory());
            edtName.setText(product.getName());
            edtCount.setText(String.valueOf(product.getCount()));
            edtPrice.setText(String.valueOf(product.getPrice()));
        }
    }

    private void editProduct() {
        if (!validateInput()) return;

        int count = Integer.parseInt(edtCount.getText().toString().trim());
        int price = Integer.parseInt(edtPrice.getText().toString().trim());
        int rowAffected = repository.update(new ProductEntity(productId, edtCategory.getText().toString().trim(), edtName.getText().toString().trim(), count, price));

        showToast(rowAffected > 0 ? "Product updated successfully" : "Product update failed");
        if (rowAffected > 0) navigateBack();
    }

    private void confirmDeleteProduct() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Confirm Deletion")
                .setMessage("Are you sure you want to delete this product?")
                .setPositiveButton("Yes", (dialog, which) -> deleteProduct())
                .setNegativeButton("No", null)
                .show();
    }

    private void deleteProduct() {
        int rowAffected = repository.delete(productId);
        showToast(rowAffected > 0 ? "Product deleted successfully" : "Failed to delete product");
        if (rowAffected > 0) navigateBack();
    }

    private void insertProduct() {
        if (!validateInput()) return;

        int id = Integer.parseInt(edtId.getText().toString().trim());
        if (repository.getById(id) != null) {
            showToast("Duplicate ID, please choose a different ID");
            return;
        }

        int count = Integer.parseInt(edtCount.getText().toString().trim());
        int price = Integer.parseInt(edtPrice.getText().toString().trim());
        long rowAffected = repository.create(new ProductEntity(id, edtCategory.getText().toString().trim(), edtName.getText().toString().trim(), count, price));

        showToast(rowAffected > 0 ? "Product added successfully" : "Failed to add product");
        if (rowAffected > 0) navigateBack();
    }

    private boolean validateInput() {
        if (TextUtils.isEmpty(edtCategory.getText().toString().trim()) ||
                TextUtils.isEmpty(edtName.getText().toString().trim()) ||
                TextUtils.isEmpty(edtCount.getText().toString().trim()) ||
                TextUtils.isEmpty(edtPrice.getText().toString().trim())) {
            showToast("Please enter all required information");
            return false;
        }
        return true;
    }

    private void showToast(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void navigateBack() {
        requireActivity().getSupportFragmentManager().popBackStack();
    }
}
