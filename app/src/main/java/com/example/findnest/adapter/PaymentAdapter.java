package com.example.findnest.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.findnest.R;
import com.example.findnest.model.response.comment.CommentDetailRes;
import com.example.findnest.model.response.transaction.PaymentRes;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.List;

public class PaymentAdapter extends RecyclerView.Adapter<PaymentAdapter.ViewHolder>{

    List<PaymentRes> paymentRes;

    public PaymentAdapter(List<PaymentRes> paymentRes) {
        this.paymentRes = paymentRes;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_payment, parent, false);
        return new PaymentAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PaymentRes payment = paymentRes.get(position);
        DecimalFormat formatter = new DecimalFormat("#,###");
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        holder.userPayment.setText(payment.getAction());
        holder.txtDatePay.setText(dateFormat.format(payment.getDate()));
        holder.txtMoney.setText(formatter.format(payment.getPrice())+"vnđ");
    }

    @Override
    public int getItemCount() {
        return paymentRes.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView userPayment, txtDatePay, txtMoney;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            userPayment = itemView.findViewById(R.id.userPayment);
            txtDatePay = itemView.findViewById(R.id.txtDatePay);
            txtMoney = itemView.findViewById(R.id.txtMoney);
        }
    }
}
