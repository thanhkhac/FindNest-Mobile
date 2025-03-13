package com.example.findnest.ui;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.example.findnest.R;
import com.example.findnest.model.ProductEntity;
import com.example.findnest.repository.Repository;

public class ProductDetailActivity extends AppCompatActivity {
    private EditText edtId, edtCategory, edtName, edtCount, edtPrice;
    private Button btnEdit, btnDelete, btnInsert;

    private Repository repository;
    private int productId = -1;
    private String mode = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        repository = new Repository(this);
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_product_detail);

        edtId = findViewById(R.id.edtid);
        edtCategory = findViewById(R.id.edtcategory);
        edtName = findViewById(R.id.edtname);
        edtCount = findViewById(R.id.edtcount);
        edtPrice = findViewById(R.id.edtprice);
        btnEdit = findViewById(R.id.btn_edit);
        btnDelete = findViewById(R.id.btn_delete);
        btnInsert = findViewById(R.id.btn_insert);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            mode = extras.getString("mode", "");
            if (mode.equalsIgnoreCase("update")) {
                btnInsert.setVisibility(View.GONE);
                edtId.setEnabled(false);
                productId = extras.getInt("id", -1);
                if (productId != -1) {
                    loadProductData(productId);
                }
            } else {
                btnDelete.setVisibility(View.GONE);
                btnEdit.setVisibility(View.GONE);
            }
        }

        btnEdit.setOnClickListener(view -> editProduct());

        btnDelete.setOnClickListener(view -> confirmDeleteProduct());

        btnInsert.setOnClickListener(view -> insertProduct());
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
        String category = edtCategory.getText().toString().trim();
        String name = edtName.getText().toString().trim();
        String countStr = edtCount.getText().toString().trim();
        String priceStr = edtPrice.getText().toString().trim();

        if (TextUtils.isEmpty(category) || TextUtils.isEmpty(name) ||
                TextUtils.isEmpty(countStr) || TextUtils.isEmpty(priceStr)) {
            Toast.makeText(this, "Please enter all required information", Toast.LENGTH_SHORT).show();
            return;
        }

        int count = Integer.parseInt(countStr);
        int price = Integer.parseInt(priceStr);

        int rowAffected = repository.update(new ProductEntity(productId, category, name, count, price));

        if (rowAffected > 0) {
            Toast.makeText(this, "Product updated successfully", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Product update failed", Toast.LENGTH_SHORT).show();
        }
    }


    private void confirmDeleteProduct() {
        new AlertDialog.Builder(this)
                .setTitle("Confirm Deletion")
                .setMessage("Are you sure you want to delete this product?")
                .setPositiveButton("Yes", (dialog, which) -> deleteProduct())
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void deleteProduct() {
        var rowAffected = repository.delete(productId);
        if (rowAffected > 0) {
            Toast.makeText(this, "Product deleted successfully", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Failed to delete product", Toast.LENGTH_SHORT).show();
        }
    }


    private void insertProduct() {
        String category = edtCategory.getText().toString().trim();
        String name = edtName.getText().toString().trim();
        String countStr = edtCount.getText().toString().trim();
        String priceStr = edtPrice.getText().toString().trim();
        String idStr = edtId.getText().toString().trim();



        if (TextUtils.isEmpty(idStr) ||TextUtils.isEmpty(category) || TextUtils.isEmpty(name) ||
                TextUtils.isEmpty(countStr) || TextUtils.isEmpty(priceStr)) {
            Toast.makeText(this, "Please enter all required information", Toast.LENGTH_SHORT).show();
            return;
        }
        int id = Integer.parseInt(idStr);
        int count = Integer.parseInt(countStr);
        int price = Integer.parseInt(priceStr);

        var existedProduct = repository.getById(id);
        if (existedProduct != null) {
            Toast.makeText(this, "Duplicate ID, please choose a different ID", Toast.LENGTH_SHORT).show();
        } else {
            long rowAffected = repository.create(new ProductEntity(id, category, name, count, price));

            if (rowAffected > 0) {
                Toast.makeText(this, "Product added successfully", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Failed to add product", Toast.LENGTH_SHORT).show();
            }
        }


    }
}
