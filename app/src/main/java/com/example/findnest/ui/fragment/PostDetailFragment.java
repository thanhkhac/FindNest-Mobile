package com.example.findnest.ui.fragment;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.example.findnest.R;
import com.example.findnest.adapter.PostDetailPagerAdapter;
import com.example.findnest.api.IPostAPI;
import com.example.findnest.api.client.auth.AuthManager;
import com.example.findnest.model.response.post.PostDetailRes;
import com.example.findnest.api.client.retrofit.RetrofitClient;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PostDetailFragment extends Fragment {
    private static final String ARG_POST_ID = "post_id";
    private String postId;
    public static PostDetailFragment newInstance(String postId) {
        PostDetailFragment fragment = new PostDetailFragment();
        Bundle args = new Bundle();
        args.putString(ARG_POST_ID, postId);
        fragment.setArguments(args);
        return fragment;
    }
    public static final String BASE_URL = "https://thanhkhac.id.vn/api/post/";
    public static final String BASE_UPLOAD_URL = "https://thanhkhac.id.vn";
    public TextView postTitle, postAddress, postArea, postRoom, postBathroom, postCreated, MoTa, Price, postCreatedPhone;
    public TabLayout tabLayout;
    public ViewPager2 viewPager;
    public PostDetailPagerAdapter pagerAdapter;
    String thumbnailUrl = "";
    List<String> slideModels = new ArrayList<>();
    PostDetailRes postDetail;
    private IPostAPI iPostService;
    public WebView mapWebView;
    private AuthManager authManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            postId = getArguments().getString(ARG_POST_ID);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_post_detail, container, false);
        init(view);
        fetchData();
        return view;
    }

    void fetchData() {
        slideModels = new ArrayList<>();

        if (postId == null || postId.isEmpty()) {
            Toast.makeText(getContext(), "Lỗi: Không tìm thấy ID bài đăng!", Toast.LENGTH_SHORT).show();
            return;
        }
        authManager = new AuthManager(getContext());
        iPostService = RetrofitClient.getClient(authManager).create(IPostAPI.class);
        iPostService.getPostDetail(postId).enqueue(new Callback<PostDetailRes>() {
            @Override
            public void onFailure(Call<PostDetailRes> call, Throwable t) {
                Log.e("API_ERROR", t.getMessage());
                Toast.makeText(getContext(), "Không thể kết nối!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onResponse(Call<PostDetailRes> call, Response<PostDetailRes> response) {
                if (response.isSuccessful() && response.body() != null) {
                    postDetail = response.body();
                    importData();
                    loadMap(postDetail.getLatitude(), postDetail.getLongitude());
                } else {

                    Toast.makeText(getContext(), "Lỗi lấy dữ liệu!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    void init(View view) {
        tabLayout = view.findViewById(R.id.tab_layout);
        viewPager = view.findViewById(R.id.view_pager);
        postTitle = view.findViewById(R.id.postTitle);
        postAddress = view.findViewById(R.id.postAddress);
        postArea = view.findViewById(R.id.postArea);
        postRoom = view.findViewById(R.id.postRoom);
        postBathroom = view.findViewById(R.id.postBathroom);
        postCreated = view.findViewById(R.id.postCreated);
        MoTa = view.findViewById(R.id.MoTa);
        Price = view.findViewById(R.id.Price);
        postCreatedPhone = view.findViewById(R.id.postCreatedPhone);
    }

    @SuppressLint("SetTextI18n")
    void importData() {
        NumberFormat formatter = NumberFormat.getInstance(Locale.US);
        String formattedPrice = formatter.format(postDetail.getPrice()) + " VND";

        postTitle.setText(postDetail.getTitle());
        postAddress.setText(postDetail.getAddress());
        postRoom.setText(String.valueOf(postDetail.getBedRoomCount()));
        postCreated.setText(postDetail.getCreatedUser().getFullName() + "  ");
        MoTa.setText(postDetail.getDescription());
        Price.setText(formattedPrice);
        postArea.setText(String.valueOf(postDetail.getArea())+ "m²" );
        postCreatedPhone.setText(postDetail.getCreatedUser().getContactPhoneNumber());
        postBathroom.setText(String.valueOf(postDetail.getBathRoomCount()));
        thumbnailUrl = BASE_UPLOAD_URL + postDetail.getThumbnail();

        postDetail.getImages().forEach(img -> {
            slideModels.add(BASE_UPLOAD_URL + img.getPath());
        });

        pagerAdapter = new PostDetailPagerAdapter(requireActivity(), thumbnailUrl, slideModels);
        viewPager.setAdapter(pagerAdapter);

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText("Hình ảnh");
                    break;
                case 1:
                    tab.setText("Thumbnail");
                    break;
            }
        }).attach();
    }

    void loadMap(double latitude, double longitude){
        mapWebView = getView().findViewById(R.id.mapWebView);
        mapWebView.setOnTouchListener((v, event) -> {
            v.getParent().requestDisallowInterceptTouchEvent(true); // Ngăn ScrollView nhận sự kiện cuộn
            return false; // Cho phép WebView xử lý tiếp sự kiện chạm
        });
        WebSettings webSettings = mapWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setUseWideViewPort(true);

        mapWebView.setWebViewClient(new WebViewClient());
        mapWebView.setWebChromeClient(new WebChromeClient());

        // Load file HTML
        mapWebView.loadUrl("file:///android_asset/map.html");

        // Đợi WebView tải xong rồi mới gọi JavaScript
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            String jsCode = "javascript:updateMarker(" + latitude + ", " + longitude + ")";
            mapWebView.evaluateJavascript(jsCode, null);
        }, 3000);  // Đợi 3 giây để đảm bảo web đã load xong
    }

}

