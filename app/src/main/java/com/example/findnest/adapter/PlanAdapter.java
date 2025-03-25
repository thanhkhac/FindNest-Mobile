package com.example.findnest.adapter;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.findnest.R;
import com.example.findnest.model.response.plan.PlanDetailRes;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.util.List;

public class PlanAdapter extends RecyclerView.Adapter<PlanAdapter.PlanViewHolder> {
    public interface OnPlanButtonClickedListener {
        void onPlanSelected(PlanDetailRes plan);
    }

    private final Context context;
    private final List<PlanDetailRes> planList;
    private final OnPlanButtonClickedListener listener;

    public PlanAdapter(Context context, List<PlanDetailRes> planList, OnPlanButtonClickedListener listener) {
        this.context = context;
        this.planList = planList;
        this.listener = listener;
    }

    public static class PlanViewHolder extends RecyclerView.ViewHolder {
        TextView tvPriorityLevel, tvDuration, tvPrice;
        MaterialButton btnSelectPlan;
        MaterialCardView cardView;

        public PlanViewHolder(
                @NonNull
                View itemView) {
            super(itemView);
            tvPriorityLevel = itemView.findViewById(R.id.tv_priority_level);
            tvDuration = itemView.findViewById(R.id.tv_duration);
            tvPrice = itemView.findViewById(R.id.tv_price);
            btnSelectPlan = itemView.findViewById(R.id.btn_select_plan);
            cardView = (MaterialCardView) itemView;
        }
    }


    @NonNull
    @Override
    public PlanViewHolder onCreateViewHolder(
            @NonNull
            ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_plan, parent, false);
        return new PlanViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull
            PlanViewHolder holder, int position) {
        PlanDetailRes plan = planList.get(position);

        holder.tvPriorityLevel.setText("Độ ưu tiên " + String.valueOf(plan.getPriorityLevel()));
        holder.tvDuration.setText(String.valueOf(plan.getDuration()) + " ngày");
        holder.tvPrice.setText(String.valueOf(plan.getPrice()) + " VND");
        holder.btnSelectPlan.setOnClickListener(v -> listener.onPlanSelected(plan));

        if (plan.getPriorityLevel() == 1 || plan.getPriorityLevel() == 2 || plan.getPriorityLevel() == 3) {
            int textColor, backgroundColor;
            switch (plan.getPriorityLevel()) {
                case 1:
                    textColor = context.getResources().getColor(R.color.white);
                    backgroundColor = context.getResources().getColor(R.color.red);
                    break;
                case 2:
                    textColor = context.getResources().getColor(R.color.white);
                    backgroundColor = context.getResources().getColor(R.color.orange);
                    break;
                case 3:
                    textColor = context.getResources().getColor(R.color.black);
                    backgroundColor = context.getResources().getColor(R.color.yellow);
                    break;
                default:
                    textColor = context.getResources().getColor(R.color.white);
                    backgroundColor = context.getResources().getColor(R.color.black);
                    break;
            }
            holder.tvPriorityLevel.setTextColor(textColor);
            holder.tvPriorityLevel.setBackgroundColor(backgroundColor);
        }
    }

    @Override
    public int getItemCount() {
        return planList.size();
    }
}
