package com.example.findnest.ui.fragment;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.findnest.R;
import com.example.findnest.adapter.CommentListAdapter;
import com.example.findnest.api.ICommentAPI;
import com.example.findnest.api.client.auth.AuthManager;
import com.example.findnest.api.client.retrofit.RetrofitClient;
import com.example.findnest.model.request.comment.CreateCommentReq;
import com.example.findnest.model.response.comment.CommentDetailRes;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CommentFragment extends Fragment {

    private static final String ARG_POST_ID = "post_id";
    private String postId;
    private ICommentAPI iCommentAPI;
    private AuthManager authManager;
    private List<CommentDetailRes> commentDetailRes;
    private RecyclerView list_item;
    private CommentListAdapter commentListAdapter;
    private TextInputEditText inputComment;
    private MaterialButton btnComment;


    public static CommentFragment newInstance(String postId) {
        CommentFragment fragment = new CommentFragment();
        Bundle args = new Bundle();
        args.putString(ARG_POST_ID, postId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            postId = getArguments().getString(ARG_POST_ID);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_comment, container, false);
        list_item = view.findViewById(R.id.list_item);
        btnComment = view.findViewById(R.id.btnComment);
        inputComment = view.findViewById(R.id.inputComment);
        fetchData();
        btnComment.setOnClickListener(v -> {
            String content = inputComment.getText().toString().trim();
            if (content.isEmpty()) {
                Toast.makeText(getContext(), "Vui lòng nhập nội dung bình luận!", Toast.LENGTH_SHORT).show();
                return;
            }
            createComment(content);
        });
        return view;
    }

    void fetchData(){
        commentDetailRes = new ArrayList<>();
        if (postId == null || postId.isEmpty()) {
            Toast.makeText(getContext(), "Lỗi: Không tìm thấy ID bài đăng!", Toast.LENGTH_SHORT).show();
            return;
        }
        authManager = new AuthManager(getContext());
        iCommentAPI = RetrofitClient.getClient(authManager).create(ICommentAPI.class);
        iCommentAPI.getCommentsByPostId(postId).enqueue(new Callback<List<CommentDetailRes>>() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onResponse(Call<List<CommentDetailRes>> call, Response<List<CommentDetailRes>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    commentDetailRes = response.body();
                    list_item.setLayoutManager(new LinearLayoutManager(getContext()));
                    commentListAdapter = new CommentListAdapter(response.body(), postId);
                    list_item.setAdapter(commentListAdapter);
                } else {
                    Toast.makeText(getContext(), "Lỗi lấy dữ liệu!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<CommentDetailRes>> call, Throwable t) {
                Log.e("API_ERROR", t.getMessage());
                Toast.makeText(getContext(), "Không thể kết nối!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    void createComment(String content){
        CreateCommentReq createCommentReq = CreateCommentReq.builder()
                .postId(postId)
                .parentId(null)
                .content(content)
                .build();
        authManager = new AuthManager(getContext());
        iCommentAPI = RetrofitClient.getClient(authManager).create(ICommentAPI.class);
        iCommentAPI.createComment(createCommentReq).enqueue(new Callback<CommentDetailRes>() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onResponse(Call<CommentDetailRes> call, Response<CommentDetailRes> response) {
                if (response.isSuccessful() && response.body() != null) {
                    commentDetailRes.add(response.body());
                    commentListAdapter.notifyItemInserted(commentDetailRes.size()-1);
                    inputComment.setText("");
                    list_item.scrollToPosition(commentDetailRes.size()-1);
                } else {
                    Toast.makeText(getContext(), "Lỗi call api!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<CommentDetailRes> call, Throwable t) {
                Log.e("API_ERROR", t.getMessage());
                Toast.makeText(getContext(), "Không thể kết nối!", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
