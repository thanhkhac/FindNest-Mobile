package com.example.findnest.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.findnest.model.ProductEntity;
import com.example.findnest.R;

import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {
    private List<ProductEntity> productEntities;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(int productId);
    }

    public ProductAdapter(List<ProductEntity> productEntities, OnItemClickListener listener) {
        this.productEntities = productEntities;
        this.listener = listener;
    }


    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_1, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        ProductEntity product = productEntities.get(position);
        holder.textViewItem.setText(product.toString());

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(product.getId());
            }
        });
    }

    @Override
    public int getItemCount() {
        return productEntities.size();
    }

    public static class ProductViewHolder extends RecyclerView.ViewHolder {
        TextView textViewItem;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewItem = itemView.findViewById(android.R.id.text1);
        }
    }
}
