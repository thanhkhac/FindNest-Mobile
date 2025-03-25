package com.example.findnest.ui.fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.findnest.R;
import com.example.findnest.adapter.MyListPostAdapter;
import com.example.findnest.api.IUserAPI;
import com.example.findnest.api.client.auth.AuthManager;
import com.example.findnest.api.client.retrofit.RetrofitClient;
import com.example.findnest.model.Post;
import com.example.findnest.ui.customEvents.OnMyPostButtonClickedListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class MyPostFragment extends Fragment implements OnMyPostButtonClickedListener {

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
    LinearLayoutManager layoutManager;

    private final String ARGS_USER_ID = "id";
    private final String ARGS_USER_FULLNAME = "fullName";
    private final String ARGS_USER_IMAGE_URL = "imageUrl";
    private final String ARGS_USER_BALANCE = "balance";

    private final String ARGS_POST_ID = "id";
    private final String ARGS_POST_TITLE = "title";
    private final String ARGS_POST_THUMBNAIL_URL = "thumbnailURL";
    private final String ARGS_POST_PRICE = "price";
    private final String ARGS_POST_AREA = "area";
    private final String ARGS_POST_ADDRESS = "regionAddress";
    public final String BASE_UPLOAD_URL = "https://thanhkhac.id.vn";

    private final String BUTTON_TYPE_EDIT = "edit";
    private final String BUTTON_TYPE_BUY_PLAN = "buyPlan";
    private final String BUTTON_TYPE_DELETE = "delete";
    private String id;
    private String fullName;
    private String imageUrl;
    private String balance;

    private void Init(View view) {
//        headerLayout = view.findViewById(R.id.header_layout);
        contentLayout = view.findViewById(R.id.content_layout);
        tv_num_result = view.findViewById(R.id.tv_num_result);
        rv_list_post = view.findViewById(R.id.rv_list_post);
        layoutManager = new LinearLayoutManager(requireContext());
        rv_list_post.setLayoutManager(layoutManager);

        postList = new ArrayList<>();
        adapter = new MyListPostAdapter(requireContext(), postList, this);
        rv_list_post.setAdapter(adapter);

        // retrieve arguments
        Bundle args = getArguments();
        if (args != null) {
            id = args.getString(ARGS_USER_ID);
            fullName = args.getString(ARGS_USER_FULLNAME);
            imageUrl = args.getString(ARGS_USER_IMAGE_URL);
            balance = args.getString(ARGS_USER_BALANCE);
        }
    }

    void InitEvents() {
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_my_post, container, false);

        Init(view);
        InitEvents();
        authManager = new AuthManager(requireContext());
        userAPI = RetrofitClient.getClient(authManager).create(IUserAPI.class);
        fetchPosts(currentPage);

        return view;
    }

    @Override
    public void onButtonClicked(int position, String type) {
        Post post = postList.get(position);
        switch (type) {
            case BUTTON_TYPE_EDIT:
//                Toast.makeText(this, "Editing: " + post, Toast.LENGTH_SHORT).show();
                break;
            case BUTTON_TYPE_BUY_PLAN:
                Toast.makeText(requireContext(), "ByPlanFor: : " + post.getId(), Toast.LENGTH_SHORT).show();

                //init bundle to pass data
                Bundle bundle = new Bundle();
                bundle.putString(ARGS_USER_ID, id);
                bundle.putString(ARGS_USER_FULLNAME, fullName.isEmpty() ? "" : fullName);
                bundle.putString(ARGS_USER_IMAGE_URL, imageUrl);
                bundle.putString(ARGS_USER_BALANCE, balance);

                bundle.putString(ARGS_POST_ID, post.getId());
                bundle.putString(ARGS_POST_TITLE, post.getTitle());
                bundle.putString(ARGS_POST_PRICE, String.valueOf(post.getPrice()));
                bundle.putString(ARGS_POST_THUMBNAIL_URL, BASE_UPLOAD_URL + post.getThumbnail());
                bundle.putString(ARGS_POST_AREA, String.valueOf(post.getArea()));
                bundle.putString(ARGS_POST_ADDRESS, post.getRegionAddress());

                //set arguments
                BuyPlanFragment buyPlanFragment = new BuyPlanFragment();
                buyPlanFragment.setArguments(bundle);

                //to fragment
                FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
                transaction.replace(R.id.frame_container, buyPlanFragment);
                transaction.addToBackStack(null); // Allows back navigation
                transaction.commit();

                break;
            case BUTTON_TYPE_DELETE:
//                Toast.makeText(this, "Viewing: " + post, Toast.LENGTH_SHORT).show();
                break;
        }
    }
}