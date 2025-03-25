package com.example.findnest.ui;

import android.animation.ValueAnimator;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.findnest.R;
import com.example.findnest.adapter.FilterAdapter;
import com.example.findnest.adapter.ListPostAdapter;
import com.example.findnest.api.IPostService;
import com.example.findnest.api.IRegionService;
import com.example.findnest.api.RetrofitClient;
import com.example.findnest.model.DistrictDTO;
import com.example.findnest.model.Post;
import com.example.findnest.model.FilterRangeDTO;
import com.example.findnest.model.ProvinceDTO;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ListPostFragment extends Fragment {
    private RecyclerView rv_list_post;
    private TextView tv_num_result, tvPriceFilter, tvAreaFilter, tv_no_results;
    private EditText etSearch;
    private IRegionService regionService;
    private String selectedProvinceCode = null;
    private String selectedDistrictCode = null;
    private String selectedProvinceName = null; // Thêm để lưu tên tỉnh
    private String selectedDistrictName = null; // Thêm để lưu tên quận

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
    private PopupWindow filterPopup;
    private Double minPrice = null;
    private Double maxPrice = null;
    private Double minArea = null;
    private Double maxArea = null;
    private boolean isFirstLoad = true;

    private List<ProvinceDTO> provinceList = new ArrayList<>();
    private List<DistrictDTO> districtList = new ArrayList<>();

    // Dữ liệu fix cứng cho các bộ lọc
    private final List<FilterRangeDTO> priceList = Arrays.asList(
            new FilterRangeDTO("Tất cả khoảng giá", null, null),
            new FilterRangeDTO("Dưới 1 triệu", 0.0, 1000000.0),
            new FilterRangeDTO("Từ 1 - 2 triệu", 1000000.0, 2000000.0),
            new FilterRangeDTO("Từ 2 - 3 triệu", 2000000.0, 3000000.0),
            new FilterRangeDTO("Từ 3 - 5 triệu", 3000000.0, 5000000.0),
            new FilterRangeDTO("Từ 5 - 7 triệu", 5000000.0, 7000000.0),
            new FilterRangeDTO("Từ 7 - 10 triệu", 7000000.0, 10000000.0),
            new FilterRangeDTO("Từ 10 - 15 triệu", 10000000.0, 15000000.0),
            new FilterRangeDTO("Trên 15 triệu", 15000000.0, null)
    );

    private final List<FilterRangeDTO> areaList = Arrays.asList(
            new FilterRangeDTO("Tất cả diện tích", null, null),
            new FilterRangeDTO("Từ 10 - 20 m²", 10.0, 20.0),
            new FilterRangeDTO("Từ 20 - 30 m²", 20.0, 30.0),
            new FilterRangeDTO("Từ 30 - 50 m²", 30.0, 50.0),
            new FilterRangeDTO("Từ 50 - 100 m²", 50.0, 100.0),
            new FilterRangeDTO("Từ 100 - 200 m²", 100.0, 200.0),
            new FilterRangeDTO("Từ 200 - 500 m²", 200.0, 500.0)
    );

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.list_post, container, false);
        init(view);
        postService = RetrofitClient.getClient(null).create(IPostService.class);
        fetchPosts(currentPage);
        return view;
    }

    private void fetchPosts(int page) {
        if (isLoading) return;
        isLoading = true;

        // Xóa danh sách cũ trước khi gọi API
        if (page == 1) {
            postList.clear();
            adapter.notifyDataSetChanged();
        }

        // Kiểm tra xem có bộ lọc giá và diện tích được áp dụng hay không
        boolean isPriceFilterApplied = minPrice != null && maxPrice != null;
        boolean isAreaFilterApplied = minArea != null && maxArea != null;
        boolean isBothFiltersApplied = isPriceFilterApplied && isAreaFilterApplied;

        Call<List<Post>> call;
        if (isFirstLoad) {
            // Lần đầu tiên tải dữ liệu: Không áp dụng bộ lọc
            call = postService.getPosts(
                    null, // minPrice
                    null, // maxPrice
                    null, // isNegotiatedPrice
                    null, // isAllPrice
                    null, // minArea
                    null, // maxArea
                    null, // provinceCode
                    null, // districtCode
                    page, // pageNumber
                    PAGE_SIZE // pageSize
            );
        } else {
            // Xác định giá trị của isAllPrice dựa trên minPrice và maxPrice
            boolean isAllPrice = (minPrice == null && maxPrice == null); // true nếu cả minPrice và maxPrice là null

            // Sau khi người dùng chọn bộ lọc: Áp dụng các tham số bộ lọc
            call = postService.getPosts(
                    minPrice, // minPrice
                    maxPrice, // maxPrice
                    false,    // isNegotiatedPrice
                    isAllPrice, // isAllPrice
                    minArea,  // minArea
                    maxArea,  // maxArea
                    selectedProvinceCode, // provinceCode
                    selectedDistrictCode, // districtCode
                    page,     // pageNumber
                    PAGE_SIZE // pageSize
            );
        }

        call.enqueue(new Callback<List<Post>>() {
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
                        if (currentPage == 1 && (isBothFiltersApplied || selectedProvinceCode != null)) {
                            rv_list_post.setVisibility(View.GONE);
                            tv_no_results.setVisibility(View.VISIBLE);
                        }
                    } else {
                        rv_list_post.setVisibility(View.VISIBLE);
                        tv_no_results.setVisibility(View.GONE);
                        postList.addAll(newPosts);
                        adapter.notifyItemRangeInserted(postList.size(), newPosts.size());
                        currentPage++;
                    }
                } else {
                    if (currentPage == 1 && (isBothFiltersApplied || selectedProvinceCode != null)) {
                        rv_list_post.setVisibility(View.GONE);
                        tv_no_results.setVisibility(View.VISIBLE);
                    } else if (currentPage > 1) {
                        Toast.makeText(requireContext(), "Không thể tải thêm dữ liệu", Toast.LENGTH_SHORT).show();
                    }
                    Toast.makeText(requireContext(), "Lỗi tải dữ liệu: " + (response != null ? response.code() : "Không có phản hồi"), Toast.LENGTH_SHORT).show();
                }
                isFirstLoad = false;
            }

            @Override
            public void onFailure(Call<List<Post>> call, Throwable t) {
                isLoading = false;
                if (currentPage == 1 && (isBothFiltersApplied || selectedProvinceCode != null)) {
                    rv_list_post.setVisibility(View.GONE);
                    tv_no_results.setVisibility(View.VISIBLE);
                } else if (currentPage > 1) {
                    Toast.makeText(requireContext(), "Không thể tải thêm dữ liệu", Toast.LENGTH_SHORT).show();
                }
                Toast.makeText(requireContext(), "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                isFirstLoad = false;
            }
        });
    }

    private void init(View view) {
        headerLayout = view.findViewById(R.id.header_layout);
        contentLayout = view.findViewById(R.id.content_layout);
        tv_num_result = view.findViewById(R.id.tv_num_result);
        tv_no_results = view.findViewById(R.id.tv_no_results);
        rv_list_post = view.findViewById(R.id.rv_list_post);
        tvPriceFilter = view.findViewById(R.id.tv_price_filter);
        tvAreaFilter = view.findViewById(R.id.tv_area_filter);
        etSearch = view.findViewById(R.id.et_search);
        LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext());
        rv_list_post.setLayoutManager(layoutManager);

        postList = new ArrayList<>();
        adapter = new ListPostAdapter(requireContext(), postList);
        rv_list_post.setAdapter(adapter);

        // Initialize services
        postService = RetrofitClient.getClient(null).create(IPostService.class);
        regionService = RetrofitClient.getClient(null).create(IRegionService.class);

        // Set EditText click listener
        etSearch.setFocusable(false); // Prevent keyboard
        etSearch.setClickable(true);
        etSearch.setOnClickListener(v -> showProvincePopup());

        // Sự kiện nhấn cho các bộ lọc
        tvPriceFilter.setOnClickListener(v -> showFilterPopup(v, "Khoảng giá", priceList, FilterRangeDTO::getText, tvPriceFilter));
        tvAreaFilter.setOnClickListener(v -> showFilterPopup(v, "Diện tích", areaList, FilterRangeDTO::getText, tvAreaFilter));

        // Điều chỉnh phần filter khi cuộn
        rv_list_post.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
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

    private <T> void showFilterPopup(View anchorView, String title, List<T> options, java.util.function.Function<T, String> displayTextExtractor, TextView targetTextView) {
        View popupView = LayoutInflater.from(requireContext()).inflate(R.layout.list_province, null);
        TextView tvPopupTitle = popupView.findViewById(R.id.tv_popup_title);
        RecyclerView rvFilterList = popupView.findViewById(R.id.rv_province_list);
        ImageView ivClosePopup = popupView.findViewById(R.id.iv_close_popup);
        Button btnSearch = popupView.findViewById(R.id.btn_search);

        tvPopupTitle.setText(title);

        LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext());
        rvFilterList.setLayoutManager(layoutManager);
        FilterAdapter<T> filterAdapter = new FilterAdapter<>(requireContext(), options, displayTextExtractor, option -> {
            targetTextView.setText(displayTextExtractor.apply(option));

            if (option instanceof ProvinceDTO) {
                ProvinceDTO province = (ProvinceDTO) option;
                selectedProvinceCode = String.format("%02d", province.getCode());
                selectedProvinceName = province.getFullName();
                Toast.makeText(requireContext(), "Province Code: " + selectedProvinceCode, Toast.LENGTH_SHORT).show();
                // Reset district khi chọn lại tỉnh mới
                selectedDistrictCode = null;
                selectedDistrictName = null;
                etSearch.setText(selectedProvinceName); // Hiển thị chỉ tên tỉnh trong EditText
                // Tải bài đăng ngay sau khi chọn tỉnh
                postList.clear();
                adapter.notifyDataSetChanged();
                currentPage = 1;
                isLastPage = false;
                fetchPosts(currentPage);
                // Mở popup quận/huyện để người dùng có thể chọn tiếp
                showDistrictPopup(selectedProvinceCode);
            } else if (option instanceof DistrictDTO) {
                DistrictDTO district = (DistrictDTO) option;
                selectedDistrictCode = String.format("%03d", district.getCode());
                selectedDistrictName = district.getFullName();
                Toast.makeText(requireContext(), "District Code: " + selectedDistrictCode, Toast.LENGTH_SHORT).show();
                if (selectedProvinceName != null && selectedDistrictName != null) {
                    etSearch.setText(selectedDistrictName + ", " + selectedProvinceName);
                }
                postList.clear();
                adapter.notifyDataSetChanged();
                currentPage = 1;
                isLastPage = false;
                fetchPosts(currentPage);
            } else if (option instanceof FilterRangeDTO) {
                FilterRangeDTO range = (FilterRangeDTO) option;
                if (targetTextView == tvPriceFilter) {
                    minPrice = range.getMinValue();
                    maxPrice = range.getMaxValue();
                    if (range.getText().equals("Tất cả khoảng giá")) {
                        isFirstLoad = true;
                    }
                } else if (targetTextView == tvAreaFilter) {
                    minArea = range.getMinValue();
                    maxArea = range.getMaxValue();
                    if (range.getText().equals("Tất cả diện tích")) {
                        isFirstLoad = true;
                    }
                }
                postList.clear();
                adapter.notifyDataSetChanged();
                currentPage = 1;
                isLastPage = false;
                fetchPosts(currentPage);
            }

            filterPopup.dismiss();
        });
        rvFilterList.setAdapter(filterAdapter);

        filterPopup = new PopupWindow(popupView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
        filterPopup.setOutsideTouchable(true);
        filterPopup.setElevation(8f);

        android.view.Window window = requireActivity().getWindow();
        android.view.WindowManager.LayoutParams params = window.getAttributes();
        params.alpha = 0.2f;
        window.setAttributes(params);

        filterPopup.setAnimationStyle(R.style.PopupAnimation);
        filterPopup.showAtLocation(anchorView, android.view.Gravity.BOTTOM, 0, 0);

        ivClosePopup.setOnClickListener(v -> filterPopup.dismiss());

        btnSearch.setOnClickListener(v -> filterPopup.dismiss());

        filterPopup.setOnDismissListener(() -> {
            params.alpha = 1.0f;
            window.setAttributes(params);
        });
    }

    private void hideHeader() {
        if (headerLayout.getHeight() == 0 || contentLayout == null) return;
        if (currentAnimator != null && currentAnimator.isRunning()) return;

        currentAnimator = ValueAnimator.ofFloat(0, -headerLayout.getHeight());
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
                isHeaderHidden = true;
                currentAnimator = null;
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

    private void showProvincePopup() {
        regionService.getProvinces().enqueue(new Callback<List<ProvinceDTO>>() {
            @Override
            public void onResponse(Call<List<ProvinceDTO>> call, Response<List<ProvinceDTO>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    provinceList.clear();
                    provinceList.addAll(response.body());
                    showFilterPopup(
                            etSearch,
                            "Chọn tỉnh/thành phố",
                            provinceList,
                            ProvinceDTO::getFullName,
                            etSearch
                    );
                } else {
                    Toast.makeText(requireContext(), "Không thể tải danh sách tỉnh/thành phố", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<ProvinceDTO>> call, Throwable t) {
                Toast.makeText(requireContext(), "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showDistrictPopup(String provinceCode) {
        Log.d("DistrictPopup", "Fetching districts for provinceCode: " + provinceCode);
        regionService.getDistricts(provinceCode).enqueue(new Callback<List<DistrictDTO>>() {
            @Override
            public void onResponse(Call<List<DistrictDTO>> call, Response<List<DistrictDTO>> response) {
                Log.d("DistrictPopup", "Response code: " + response.code());
                Log.d("DistrictPopup", "Response body: " + (response.body() != null ? response.body().toString() : "null"));
                Log.d("DistrictPopup", "Is successful: " + response.isSuccessful());
                if (response.isSuccessful() && response.body() != null) {
                    districtList.clear();
                    districtList.addAll(response.body());
                    Log.d("DistrictPopup", "District list size: " + districtList.size());
                    showFilterPopup(
                            etSearch,
                            "Chọn quận/huyện",
                            districtList,
                            DistrictDTO::getFullName,
                            etSearch
                    );
                } else {
                    Toast.makeText(requireContext(), "Không thể tải danh sách quận/huyện", Toast.LENGTH_SHORT).show();
                    Log.e("DistrictPopup", "Error: Response not successful or body is null");
                }
            }

            @Override
            public void onFailure(Call<List<DistrictDTO>> call, Throwable t) {
                Toast.makeText(requireContext(), "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("DistrictPopup", "Failure: " + t.getMessage());
            }
        });
    }
}