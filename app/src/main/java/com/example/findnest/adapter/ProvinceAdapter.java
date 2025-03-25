package com.example.findnest.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.findnest.model.ProvinceDTO;

import java.util.List;
import java.util.function.Consumer;

public class ProvinceAdapter extends RecyclerView.Adapter<ProvinceAdapter.ProvinceViewHolder> {
    private final Context context;
    private final List<ProvinceDTO> provinceList;
    private final Consumer<ProvinceDTO> onItemClickListener;

    public ProvinceAdapter(Context context, List<ProvinceDTO> provinceList, Consumer<ProvinceDTO> onItemClickListener) {
        this.context = context;
        this.provinceList = provinceList;
        this.onItemClickListener = onItemClickListener;
    }

    @NonNull
    @Override
    public ProvinceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(android.R.layout.simple_list_item_1, parent, false);
        return new ProvinceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProvinceViewHolder holder, int position) {
        ProvinceDTO province = provinceList.get(position);
        holder.textView.setText(province.getName());
        holder.itemView.setOnClickListener(v -> onItemClickListener.accept(province));
    }

    @Override
    public int getItemCount() {
        return provinceList.size();
    }

    static class ProvinceViewHolder extends RecyclerView.ViewHolder {
        TextView textView;

        ProvinceViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(android.R.id.text1);
        }
    }
}