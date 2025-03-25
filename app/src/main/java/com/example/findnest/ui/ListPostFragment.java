package com.example.findnest.ui;

import android.animation.ValueAnimator;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.findnest.R;
import com.example.findnest.adapter.ListPostAdapter;
import com.example.findnest.api.IPostService;
import com.example.findnest.api.client.retrofit.RetrofitClient;
import com.example.findnest.model.Post;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class ListPostFragment extends Fragment {
    private RecyclerView rv_list_post;
    private TextView tv_num_result;
    private LinearLayout headerLayout;
    private LinearLayout contentLayout;
    private ListPostAdapter adapter;
    private List<Post> postList;
    private IPostService postService;
    private int currentPage = 1;
    private final int PAGE_SIZE = 10;
    private boolean isLoading = false;
    private boolean isLastPage = false;
    private boolean isHeaderHidden = false;
    private ValueAnimator currentAnimator;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull
            LayoutInflater inflater,
            @Nullable
            ViewGroup container,
            @Nullable
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.list_post, container, false);
        init(view);
        postService = RetrofitClient.getClient(null).create(IPostService.class);
        fetchPosts(currentPage);
        return view;
    }

    private void fetchPosts(int page) {
        if (isLoading) return;
        isLoading = true;

        postService.getPosts(page, PAGE_SIZE).enqueue(new Callback<List<Post>>() {
            @Override
            public void onResponse(Call<List<Post>> call, Response<List<Post>> response) {
                isLoading = false;
                if (response.isSuccessful() && response.body() != null) {
                    List<Post> newPosts = response.body();

                    // Total post
                    String paginationHeader = response.headers().get("x-pagination");
                    if (paginationHeader != null) {
                        try {
                            JSONObject json = new JSONObject(paginationHeader);
                            int totalCount = json.getInt("TotalCount");
                            tv_num_result.setText(totalCount + " kết quả");
                        } catch (JSONException e) {
                            Log.e("JSON_ERROR", "Lỗi parse JSON: " + e.getMessage());
                        }
                    }

                    if (newPosts.isEmpty()) {
                        isLastPage = true;
                    } else {
                        postList.addAll(newPosts);
                        adapter.notifyItemRangeInserted(postList.size(), newPosts.size());
                        currentPage++;
                    }
                } else {
                    Toast.makeText(requireContext(), "Lỗi tải dữ liệu: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Post>> call, Throwable t) {
                isLoading = false;
                Toast.makeText(requireContext(), "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void init(View view) {
        headerLayout = view.findViewById(R.id.header_layout);
        contentLayout = view.findViewById(R.id.content_layout);
        tv_num_result = view.findViewById(R.id.tv_num_result);
        rv_list_post = view.findViewById(R.id.rv_list_post);
        LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext());
        rv_list_post.setLayoutManager(layoutManager);

        postList = new ArrayList<>();
        adapter = new ListPostAdapter(requireContext(), postList);
        rv_list_post.setAdapter(adapter);

        // điều chỉnh phần filter khi cuộn
        rv_list_post.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(
                    @NonNull
                    RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                // Điều chỉnh header và content khi cuộn
                if (dy > 0 && !isHeaderHidden) { // Cuộn xuống và header chưa ẩn
                    hideHeader();
                } else if (dy < 0 && isHeaderHidden) { // Cuộn lên và header đang ẩn
                    showHeader();
                }

                // Load more
                int totalItemCount = layoutManager.getItemCount();
                int lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition();
                if (!isLoading && !isLastPage && lastVisibleItemPosition + 1 >= totalItemCount) {
                    fetchPosts(currentPage);
                }
            }
        });
    }

    private void hideHeader() {
        if (headerLayout.getHeight() == 0 || contentLayout == null) return;
        if (currentAnimator != null && currentAnimator.isRunning())
            return; // Tránh chạy nhiều animator cùng lúc

        currentAnimator = ValueAnimator.ofFloat(0, -headerLayout.getHeight());
        currentAnimator.setDuration(200);
        currentAnimator.addUpdateListener(animation -> {
            float value = (float) animation.getAnimatedValue();
            headerLayout.setTranslationY(value); // Header trượt lên
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) contentLayout.getLayoutParams();
            if (params != null) {
                params.topMargin = (int) value; // Content trôi lên và chiếm chỗ
                contentLayout.setLayoutParams(params);
            }
        });
        currentAnimator.addListener(new android.animation.AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(android.animation.Animator animation) {
                isHeaderHidden = true;
                currentAnimator = null; // Reset animator sau khi hoàn thành
            }
        });
        currentAnimator.start();
    }

    private void showHeader() {
        if (headerLayout.getHeight() == 0 || contentLayout == null) return;
        if (currentAnimator != null && currentAnimator.isRunning()) return;

        currentAnimator = ValueAnimator.ofFloat(-headerLayout.getHeight(), 0);
        currentAnimator.setDuration(200);
        currentAnimator.addUpdateListener(animation -> {
            float value = (float) animation.getAnimatedValue();
            headerLayout.setTranslationY(value);
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) contentLayout.getLayoutParams();
            if (params != null) {
                params.topMargin = (int) value;
                contentLayout.setLayoutParams(params);
            }
        });
        currentAnimator.addListener(new android.animation.AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(android.animation.Animator animation) {
                isHeaderHidden = false;
                currentAnimator = null;
            }
        });
        currentAnimator.start();
    }
}