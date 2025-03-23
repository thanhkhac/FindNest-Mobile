package com.example.findnest.ui;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.example.findnest.R;
import com.example.findnest.adapter.PostDetailPagerAdapter;
import com.example.findnest.api.APIPost;
import com.example.findnest.model.response.PostDto;
import com.example.findnest.retrofit.RetrofitClient;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PostDetailActivity extends AppCompatActivity {

    private static final String BASE_URL = "https://thanhkhac.id.vn/api/post/";
    private static final String BASE_UPLOAD_URL = "https://thanhkhac.id.vn";
    private TextView postTitle, postAddress, postArea, postRoom, postBathroom, postCreated, MoTa, Price, postCreatedPhone;
    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private PostDetailPagerAdapter pagerAdapter;
    String thumbnailUrl = "";
    List<String> slideModels = new ArrayList<>();
    PostDto postDetail;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail);
        init();
        fetchData();
    }

    void fetchData() {
        slideModels = new ArrayList<>();

        APIPost apiService = RetrofitClient.getClient(BASE_URL).create(APIPost.class);

        String postId = getIntent().getStringExtra("Id");

        if (postId == null || postId.isEmpty()) {
            Toast.makeText(this, "Lỗi: Không tìm thấy ID bài đăng!", Toast.LENGTH_SHORT).show();
            return;
        }
        Call<PostDto> call = apiService.getPostDetail(postId);

        call.enqueue(new Callback<PostDto>() {
            @Override
            public void onFailure(Call<PostDto> call, Throwable t) {
                Log.e("API_ERROR", t.getMessage());
                Toast.makeText(PostDetailActivity.this, "Không thể kết nối!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onResponse(Call<PostDto> call, Response<PostDto> response) {
                if (response.isSuccessful() && response.body() != null) {
                    postDetail = response.body();
                    importData();
                } else {

                    Toast.makeText(PostDetailActivity.this, "Lỗi lấy dữ liệu!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    void init() {
        tabLayout = findViewById(R.id.tab_layout);
        viewPager = findViewById(R.id.view_pager);
        postTitle = findViewById(R.id.postTitle);
        postAddress = findViewById(R.id.postAddress);
        postArea = findViewById(R.id.postArea);
        postRoom = findViewById(R.id.postRoom);
        postBathroom = findViewById(R.id.postBathroom);
        postCreated = findViewById(R.id.postCreated);
        MoTa = findViewById(R.id.MoTa);
        Price = findViewById(R.id.Price);
        postCreatedPhone = findViewById(R.id.postCreatedPhone);
    }

    @SuppressLint("SetTextI18n")
    void importData() {
        NumberFormat formatter = NumberFormat.getInstance(Locale.US);
        String formattedPrice = formatter.format(postDetail.getPrice()) + " VND";

        postTitle.setText(postDetail.getTitle());
        postAddress.setText(postDetail.getAddress());
        postRoom.setText(String.valueOf(postDetail.getBedRoomCount()));
        postCreated.setText(postDetail.getCreatedUser().fullName + "  ");
        MoTa.setText(postDetail.getDescription());
        Price.setText(formattedPrice);
        postArea.setText(String.valueOf(postDetail.getArea())+ "m²" );
        postCreatedPhone.setText(postDetail.getCreatedUser().contactPhoneNumber);
        postBathroom.setText(String.valueOf(postDetail.getBathRoomCount()));
        thumbnailUrl = BASE_UPLOAD_URL + postDetail.getThumbnail();

        postDetail.getImages().forEach(img -> {
            slideModels.add(BASE_UPLOAD_URL + img.getPath());
        });

        pagerAdapter = new PostDetailPagerAdapter(this, thumbnailUrl, slideModels);
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

}

