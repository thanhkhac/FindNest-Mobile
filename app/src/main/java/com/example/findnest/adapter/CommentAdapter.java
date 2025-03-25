package com.example.findnest.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.findnest.R;
import com.example.findnest.model.response.comment.CommentDetailRes;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.ViewHolder> {

    private List<CommentDetailRes> commentDetailRes;


    public CommentAdapter(List<CommentDetailRes> commentDetailRes) {
        this.commentDetailRes = commentDetailRes;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_comment, parent, false);
        return new CommentAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CommentDetailRes comment = commentDetailRes.get(position);
        holder.UserName.setText(comment.getCreatedBy().getFullName());
        holder.Date.setText(formatDate(comment.getCreatedAt()));
        holder.Comment.setText(comment.getContent());
    }

    @Override
    public int getItemCount() {
        return commentDetailRes.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

    TextView UserName, Date, Comment;
    public ViewHolder(@NonNull View itemView) {
        super(itemView);
        UserName = itemView.findViewById(R.id.UserName_item);
        Date = itemView.findViewById(R.id.Date_item);
        Comment = itemView.findViewById(R.id.Comment_item);
    }
}
    private String formatDate(String dateString) {
        try {
            SimpleDateFormat originalFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            SimpleDateFormat targetFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
            java.util.Date date = originalFormat.parse(dateString);
            return targetFormat.format(date);
        } catch (Exception e) {
            e.printStackTrace();
            return dateString; // Nếu có lỗi, trả về ngày gốc
        }
    }
}
