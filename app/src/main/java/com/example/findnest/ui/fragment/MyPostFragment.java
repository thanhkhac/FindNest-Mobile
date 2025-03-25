package com.example.findnest.ui.fragment;

import android.animation.ValueAnimator;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.findnest.R;
import com.example.findnest.adapter.ListPostAdapter;
import com.example.findnest.adapter.MyListPostAdapter;
import com.example.findnest.api.IPostService;
import com.example.findnest.api.IUserAPI;
import com.example.findnest.api.client.auth.AuthManager;
import com.example.findnest.api.client.retrofit.RetrofitClient;
import com.example.findnest.model.Post;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class MyPostFragment extends Fragment {

    private RecyclerView rv_list_post;
    private TextView tv_num_result;
    //    private LinearLayout headerLayout;
    private LinearLayout contentLayout;
    private MyListPostAdapter adapter;
    private List<Post> postList;
    private int currentPage = 1;
    private final int PAGE_SIZE = 10;
    private boolean isLoading = false;
    private boolean isLastPage = false;
    private AuthManager authManager;
    private IUserAPI userAPI;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_my_post, container, false);

        init(view);
        authManager = new AuthManager(requireContext());
        userAPI = RetrofitClient.getClient(authManager).create(IUserAPI.class);
        fetchPosts(currentPage);

        return view;
    }

    private void fetchPosts(int page) {
        if (isLoading) return;
        isLoading = true;

        userAPI.getMyPosts(page, PAGE_SIZE).enqueue(new Callback<List<Post>>() {
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
                            tv_num_result.setText("Tìm thấy: " + totalCount + " kết quả");
                        } catch (JSONException e) {
                            Log.e("JSON_ERROR", "Lỗi parse JSON: " + e.getMessage());
                        }
                    }

                    if (newPosts.isEmpty()) {
                        isLastPage = true;
                    } else {
                        int startIndex = postList.size();
                        postList.addAll(newPosts);
                        adapter.notifyItemRangeInserted(startIndex, newPosts.size());
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
//        headerLayout = view.findViewById(R.id.header_layout);
        contentLayout = view.findViewById(R.id.content_layout);
        tv_num_result = view.findViewById(R.id.tv_num_result);
        rv_list_post = view.findViewById(R.id.rv_list_post);
        LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext());
        rv_list_post.setLayoutManager(layoutManager);

        postList = new ArrayList<>();
        adapter = new MyListPostAdapter(requireContext(), postList);
        rv_list_post.setAdapter(adapter);

        // điều chỉnh phần filter khi cuộn
        rv_list_post.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(
                    @NonNull
                    RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                // Load more
                int totalItemCount = layoutManager.getItemCount();
                int lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition();
                if (!isLoading && !isLastPage && lastVisibleItemPosition + 3 >= totalItemCount) { //load new items when 3 item left
                    fetchPosts(currentPage);
                }
            }
        });
    }
}