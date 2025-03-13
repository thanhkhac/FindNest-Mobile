package com.example.findnest.ui.adapters;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.findnest.R;
import com.example.findnest.model.ProductEntity;
import com.example.findnest.ui.ProductDetailActivity;

import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.StudentViewHolder>
{
    private List<ProductEntity> productEntities;

    public ProductAdapter(List<ProductEntity> productEntities)
    {
        this.productEntities = productEntities;
    }

    @NonNull
    @Override
    //Phương thức này sẽ chạy khi khởi động hoặc danh sách cuộn đến các item chưa hiển thị
    public StudentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {

//        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.product_item, parent, false);

        View view = LayoutInflater.from(parent.getContext())
                .inflate(android.R.layout.simple_list_item_1, parent, false);

        return new StudentViewHolder(view);
    }

    public interface OnItemClickListener {
        void onItemClick(int productId);
    }

    //Set dữ liệu vào holder
    @Override
    public void onBindViewHolder(@NonNull StudentViewHolder holder, int position)
    {
        ProductEntity product = productEntities.get(position);
        var content = product.toString();
        holder.textview_item.setText(content);
        holder.itemView.setOnClickListener(v ->
        {
            Intent intent = new Intent(holder.itemView.getContext(), ProductDetailActivity.class);
            intent.putExtra("id", product.getId());
            intent.putExtra("mode","update");
            holder.itemView.getContext().startActivity(intent);
        });
    }

    //Trả về kích thước của list để biết có bao nhiêu item cần hiển thị
    //Size mà là 0 thì không hiển thị gì
    @Override
    public int getItemCount()
    {
        return productEntities.size();
    }

    public static class StudentViewHolder extends RecyclerView.ViewHolder
    {
        TextView textview_item;

        public StudentViewHolder(@NonNull View itemView)
        {
            super(itemView);
//            textview_item = itemView.findViewById(R.id.textview_item);
            textview_item = itemView.findViewById(android.R.id.text1);
        }
    }
}