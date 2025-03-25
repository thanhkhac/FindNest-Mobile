package com.example.findnest.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.findnest.R;

import java.util.List;
import java.util.function.Function;

public class FilterAdapter<T> extends RecyclerView.Adapter<FilterAdapter.FilterViewHolder> {
    private Context context;
    private List<T> optionList;
    private int selectedPosition = -1;
    private OnOptionSelectedListener<T> listener;
    private Function<T, String> displayTextExtractor;

    public FilterAdapter(Context context, List<T> optionList, Function<T, String> displayTextExtractor, OnOptionSelectedListener<T> listener) {
        this.context = context;
        this.optionList = optionList;
        this.displayTextExtractor = displayTextExtractor;
        this.listener = listener;
    }

    @NonNull
    @Override
    public FilterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_filter, parent, false);
        return new FilterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FilterViewHolder holder, int position) {
        T option = optionList.get(position);
        String displayText = displayTextExtractor.apply(option);
        holder.tvOption.setText(displayText != null ? displayText : "Không xác định");
        holder.rbOption.setChecked(position == selectedPosition);

        holder.rbOption.setOnClickListener(v -> {
            int previousPosition = selectedPosition;
            selectedPosition = holder.getAdapterPosition();
            Log.d("FilterAdapter", "Đã chọn: " + displayText + " tại vị trí: " + selectedPosition);
            if (previousPosition != selectedPosition) {
                notifyItemChanged(previousPosition); // Cập nhật item trước đó
                notifyItemChanged(selectedPosition); // Cập nhật item hiện tại
            }
            listener.onOptionSelected(option);
        });
    }

    @Override
    public int getItemCount() {
        return optionList.size();
    }

    static class FilterViewHolder extends RecyclerView.ViewHolder {
        TextView tvOption;
        RadioButton rbOption;

        FilterViewHolder(@NonNull View itemView) {
            super(itemView);
            tvOption = itemView.findViewById(R.id.tv_filter_option);
            rbOption = itemView.findViewById(R.id.rb_filter_option);
        }
    }

    public interface OnOptionSelectedListener<T> {
        void onOptionSelected(T option);
    }
}