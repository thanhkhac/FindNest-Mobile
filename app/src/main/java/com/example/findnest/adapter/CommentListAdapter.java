package com.example.findnest.adapter;

import android.annotation.SuppressLint;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.findnest.R;
import com.example.findnest.api.ICommentAPI;
import com.example.findnest.api.client.auth.AuthManager;
import com.example.findnest.api.client.retrofit.RetrofitClient;
import com.example.findnest.model.request.comment.CreateCommentReq;
import com.example.findnest.model.response.comment.CommentDetailRes;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CommentListAdapter extends RecyclerView.Adapter<CommentListAdapter.ViewHolder> {

    private  List<CommentDetailRes> commentDetailRes;
    private String postId;
    private ICommentAPI iCommentAPI;
    private AuthManager authManager;
    public CommentListAdapter(List<CommentDetailRes> commentDetailRes, String postId) {
        this.commentDetailRes = commentDetailRes;
        this.postId = postId;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_comment_main, parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CommentDetailRes comment = commentDetailRes.get(position);
        holder.list_item.setVisibility(View.GONE);
        holder.UserName.setText(comment.getCreatedBy().getFullName());
        holder.Date.setText(formatDate(comment.getCreatedAt()));
        holder.Comment.setText(comment.getContent());
        holder.countComment.setText("Trả lời (" +comment.getReplyCount() + ")");
        holder.countCmt = comment.getReplyCount();
        holder.layout_reply.setVisibility(View.GONE);
        CommentAdapter commentListAdapter = new CommentAdapter(new ArrayList<>(comment.getReplies()));
        holder.list_item.setLayoutManager(new LinearLayoutManager(holder.itemView.getContext()));
        holder.list_item.setAdapter(commentListAdapter);

        holder.btnSend.setOnClickListener(s -> {
            String content = holder.input_reply.getText().toString().trim();
            if (content.isEmpty()) {
                Toast.makeText(holder.itemView.getContext(), "Vui lòng nhập nội dung bình luận!", Toast.LENGTH_SHORT).show();
                return;
            }
            createComment(content, holder, comment.getId().toString(),comment.getReplies());
        });
        holder.countComment.setOnClickListener(v -> {
            if (holder.list_item.getVisibility() == View.VISIBLE) {
                holder.list_item.setVisibility(View.GONE);
            } else {
                holder.list_item.setVisibility(View.VISIBLE);
            }
        });
        holder.btn_reply.setOnClickListener(v -> {
            if (holder.layout_reply.getVisibility() == View.VISIBLE) {
                holder.layout_reply.setVisibility(View.GONE);
            } else {
                holder.layout_reply.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public int getItemCount() {
        return commentDetailRes.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView UserName, Date, Comment, countComment;
        LinearLayout layout_reply, btn_reply;
        MaterialButton btnSend;
        TextInputEditText input_reply;
        RecyclerView list_item;
        int countCmt = 0;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            UserName = itemView.findViewById(R.id.UserName);
            Date = itemView.findViewById(R.id.Date);
            Comment = itemView.findViewById(R.id.Comment);
            countComment = itemView.findViewById(R.id.countComment);
            list_item = itemView.findViewById(R.id.list_item);
            layout_reply = itemView.findViewById(R.id.layout_reply);
            btn_reply = itemView.findViewById(R.id.btn_reply);
            btnSend = itemView.findViewById(R.id.btnSend);
            input_reply = itemView.findViewById(R.id.input_reply);
        }
    }
    private String formatDate(String dateString) {
        try {
            SimpleDateFormat originalFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            SimpleDateFormat targetFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
            Date date = originalFormat.parse(dateString);
            return targetFormat.format(date);
        } catch (Exception e) {
            e.printStackTrace();
            return dateString; // Nếu có lỗi, trả về ngày gốc
        }
    }

    void createComment(String content, ViewHolder holder, String parentId, List<CommentDetailRes> commentDetail){
        CreateCommentReq createCommentReq = CreateCommentReq.builder()
                .postId(postId)
                .parentId(parentId)
                .content(content)
                .build();
        authManager = new AuthManager(holder.itemView.getContext());
        iCommentAPI = RetrofitClient.getClient(authManager).create(ICommentAPI.class);
        iCommentAPI.createComment(createCommentReq).enqueue(new Callback<CommentDetailRes>() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onResponse(Call<CommentDetailRes> call, Response<CommentDetailRes> response) {
                if (response.isSuccessful() && response.body() != null) {
                    commentDetail.add(response.body());
                    holder.countComment.setText("Trả lời (" +(holder.countCmt+ 1)+ ")");
                    holder.input_reply.setText("");
                    CommentAdapter commentListAdapter = new CommentAdapter(new ArrayList<>(commentDetail));
                    holder.list_item.setLayoutManager(new LinearLayoutManager(holder.itemView.getContext()));
                    holder.list_item.setAdapter(commentListAdapter);
                } else {
                    Toast.makeText(holder.itemView.getContext(), "Lỗi call api!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<CommentDetailRes> call, Throwable t) {
                Log.e("API_ERROR", t.getMessage());
                Toast.makeText(holder.itemView.getContext(), "Không thể kết nối!", Toast.LENGTH_SHORT).show();
            }
        });
    }
}

