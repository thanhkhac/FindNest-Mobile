package com.example.findnest.ui.fragment;

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
import android.widget.ProgressBar;
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
import com.example.findnest.api.client.retrofit.RetrofitClient;
import com.example.findnest.model.Post;
import com.example.findnest.model.FilterRangeDTO;
import com.example.findnest.model.responsedtos.RegionResponse;

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
    private ProgressBar progressBar;
    private RelativeLayout loadingOverlay; // Thêm overlay
    private IRegionService regionService;
    private IPostService postService;
    private String selectedProvinceCode = null;
    private String selectedWardCode = null; // Thêm ward code
    private String selectedDistrictCode = null;
    private String selectedProvinceName = null;
    private String selectedDistrictName = null;
    private String selectedWardName = null; // Thêm ward name

    private LinearLayout headerLayout;
    private LinearLayout contentLayout;
    private ListPostAdapter adapter;
    private List<Post> postList;
    private int currentPage = 1;
    private final int PAGE_SIZE = 10;
    private boolean isLoading = false;
    private boolean isLastPage = false;
    private boolean isHeaderHidden = false;
    private ValueAnimator currentAnimator;
    private PopupWindow filterPopup;
    private Double minPrice = null;
    private Double maxPrice = null;
    private Boolean isNegotiatedPrice = null;
    private Integer minArea = null; // Thay từ Double sang Integer
    private Integer maxArea = null; // Thay từ Double sang Integer
    private boolean isFirstLoad = true;

    private List<RegionResponse> provinceList = new ArrayList<>();
    private List<RegionResponse> districtList = new ArrayList<>();
    private List<RegionResponse> wardList = new ArrayList<>(); // Thêm danh sách ward

    // Bộ lọc giá với "Giá thỏa thuận"
    private final List<FilterRangeDTO> priceList = Arrays.asList(
            new FilterRangeDTO("Tất cả khoảng giá", null, null, null),
            new FilterRangeDTO("Giá thỏa thuận", null, null, true),
            new FilterRangeDTO("Dưới 1 triệu", 0.0, 1000000.0, false),
            new FilterRangeDTO("Từ 1 - 2 triệu", 1000000.0, 2000000.0, false),
            new FilterRangeDTO("Từ 2 - 3 triệu", 2000000.0, 3000000.0, false),
            new FilterRangeDTO("Từ 3 - 5 triệu", 3000000.0, 5000000.0, false),
            new FilterRangeDTO("Từ 5 - 7 triệu", 5000000.0, 7000000.0, false),
            new FilterRangeDTO("Từ 7 - 10 triệu", 7000000.0, 10000000.0, false),
            new FilterRangeDTO("Từ 10 - 15 triệu", 10000000.0, 15000000.0, false),
            new FilterRangeDTO("Trên 15 triệu", 15000000.0, null, false)
    );

    // Bộ lọc diện tích
    private final List<FilterRangeDTO> areaList = Arrays.asList(
            new FilterRangeDTO("Tất cả diện tích", null, null, null),
            new FilterRangeDTO("Từ 10 - 20 m²", 10.0, 20.0, null),
            new FilterRangeDTO("Từ 20 - 30 m²", 20.0, 30.0, null),
            new FilterRangeDTO("Từ 30 - 50 m²", 30.0, 50.0, null),
            new FilterRangeDTO("Từ 50 - 100 m²", 50.0, 100.0, null),
            new FilterRangeDTO("Từ 100 - 200 m²", 100.0, 200.0, null),
            new FilterRangeDTO("Từ 200 - 500 m²", 200.0, 500.0, null)
    );

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.list_post, container, false);
        init(view);
        postService = RetrofitClient.getClient(null).create(IPostService.class);
        regionService = RetrofitClient.getClient(null).create(IRegionService.class);
        fetchPosts(currentPage);
        return view;
    }

    private void fetchPosts(int page) {
        if (isLoading || isLastPage) return;
        isLoading = true;
        showLoading(true);

        if (page == 1) {
            postList.clear();
            adapter.notifyDataSetChanged();
        }

        boolean isAllPrice = (minPrice == null && maxPrice == null && isNegotiatedPrice == null);


        Call<List<Post>> call = postService.getPosts(
                minPrice,
                maxPrice,
                isNegotiatedPrice,
                isAllPrice,
                minArea,
                maxArea,
                selectedProvinceCode,
                selectedDistrictCode,
                selectedWardCode,
                page,
                PAGE_SIZE
        );

        call.enqueue(new Callback<List<Post>>() {
            @Override
            public void onResponse(Call<List<Post>> call, Response<List<Post>> response) {
                isLoading = false;
                showLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    List<Post> newPosts = response.body();

                    String paginationHeader = response.headers().get("x-pagination");
                    if (paginationHeader != null) {
                        try {
                            JSONObject json = new JSONObject(paginationHeader);
                            int totalCount = json.getInt("TotalCount");
                            tv_num_result.setText(totalCount + " kết quả");
                        } catch (JSONException e) {
                            Log.e("JSON_ERROR", "Lỗi parse JSON: " + e.getMessage());
                            tv_num_result.setText("Lỗi đếm kết quả");
                        }
                    }

                    if (newPosts.isEmpty()) {
                        isLastPage = true;
                        if (page == 1) {
                            rv_list_post.setVisibility(View.GONE);
                            tv_no_results.setVisibility(View.VISIBLE);
                        }
                    } else {
                        rv_list_post.setVisibility(View.VISIBLE);
                        tv_no_results.setVisibility(View.GONE);
                        postList.addAll(newPosts);
                        adapter.notifyItemRangeInserted(postList.size() - newPosts.size(), newPosts.size());
                        currentPage++;
                    }
                } else {
                    if (page == 1) {
                        rv_list_post.setVisibility(View.GONE);
                        tv_no_results.setVisibility(View.VISIBLE);
                    }
                    Toast.makeText(requireContext(), "Lỗi tải dữ liệu: " + response.code(), Toast.LENGTH_SHORT).show();
                }
                isFirstLoad = false;
            }

            @Override
            public void onFailure(Call<List<Post>> call, Throwable t) {
                isLoading = false;
                showLoading(false);
                if (page == 1) {
                    rv_list_post.setVisibility(View.GONE);
                    tv_no_results.setVisibility(View.VISIBLE);
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
        progressBar = view.findViewById(R.id.progress_bar);
        loadingOverlay = view.findViewById(R.id.loading_overlay); // Khởi tạo overlay

        LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext());
        rv_list_post.setLayoutManager(layoutManager);

        postList = new ArrayList<>();
        adapter = new ListPostAdapter(requireContext(), postList);
        rv_list_post.setAdapter(adapter);

        etSearch.setFocusable(false);
        etSearch.setClickable(true);
        etSearch.setOnClickListener(v -> showProvincePopup());

        tvPriceFilter.setOnClickListener(v -> showFilterPopup(v, "Khoảng giá", priceList, FilterRangeDTO::getText, tvPriceFilter));
        tvAreaFilter.setOnClickListener(v -> showFilterPopup(v, "Diện tích", areaList, FilterRangeDTO::getText, tvAreaFilter));

        rv_list_post.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                if (dy > 0 && !isHeaderHidden) {
                    hideHeader();
                } else if (dy < 0 && isHeaderHidden) {
                    showHeader();
                }

                int totalItemCount = layoutManager.getItemCount();
                int lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition();
                if (!isLoading && !isLastPage && lastVisibleItemPosition + 1 >= totalItemCount) {
                    fetchPosts(currentPage);
                }
            }
        });
    }

    private void showLoading(boolean show) {
        loadingOverlay.setVisibility(show ? View.VISIBLE : View.GONE);
        // Không cần vô hiệu hóa rv_list_post nữa vì overlay đã chặn tương tác
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

            if (option instanceof RegionResponse && "Chọn tỉnh/thành phố".equals(title)) {
                RegionResponse province = (RegionResponse) option;
                selectedProvinceCode = province.getCode();
                selectedProvinceName = province.getFullName();
                selectedDistrictCode = null;
                selectedDistrictName = null;
                selectedWardCode = null; // Reset ward
                selectedWardName = null; // Reset ward
                etSearch.setText(selectedProvinceName);
                postList.clear();
                adapter.notifyDataSetChanged();
                currentPage = 1;
                isLastPage = false;
                fetchPosts(currentPage);
                filterPopup.dismiss();
                if (selectedProvinceCode != null && !selectedProvinceCode.isEmpty()) {
                    showDistrictPopup(selectedProvinceCode);
                }
            } else if (option instanceof RegionResponse && "Chọn quận/huyện".equals(title)) {
                RegionResponse district = (RegionResponse) option;
                selectedDistrictCode = district.getCode();
                selectedDistrictName = district.getFullName();
                selectedWardCode = null; // Reset ward
                selectedWardName = null; // Reset ward
                etSearch.setText(selectedDistrictName + ", " + selectedProvinceName);
                postList.clear();
                adapter.notifyDataSetChanged();
                currentPage = 1;
                isLastPage = false;
                fetchPosts(currentPage);
                filterPopup.dismiss();
                if (selectedDistrictCode != null && !selectedDistrictCode.isEmpty()) {
                    showWardPopup(selectedDistrictCode);
                }
            } else if (option instanceof RegionResponse && "Chọn phường/xã".equals(title)) {
                RegionResponse ward = (RegionResponse) option;
                selectedWardCode = ward.getCode();
                selectedWardName = ward.getFullName();
                etSearch.setText(selectedWardName + ", " + selectedDistrictName + ", " + selectedProvinceName);
                postList.clear();
                adapter.notifyDataSetChanged();
                currentPage = 1;
                isLastPage = false;
                fetchPosts(currentPage); // Gọi lại fetchPosts với wardCode nếu API hỗ trợ
                filterPopup.dismiss();
            } else if (option instanceof FilterRangeDTO) {
                FilterRangeDTO range = (FilterRangeDTO) option;
                if (targetTextView == tvPriceFilter) {
                    minPrice = range.getMinValue();
                    maxPrice = range.getMaxValue();
                    isNegotiatedPrice = range.getIsNegotiatedPrice();
                } else if (targetTextView == tvAreaFilter) {
                    minArea = range.getMinValue() != null ? range.getMinValue().intValue() : null;
                    maxArea = range.getMaxValue() != null ? range.getMaxValue().intValue() : null;
                }
                postList.clear();
                adapter.notifyDataSetChanged();
                currentPage = 1;
                isLastPage = false;
                fetchPosts(currentPage);
                filterPopup.dismiss();
            }
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
        showLoading(true); // Hiển thị loading khi gọi API
        regionService.getProvinces().enqueue(new Callback<List<RegionResponse>>() {
            @Override
            public void onResponse(Call<List<RegionResponse>> call, Response<List<RegionResponse>> response) {
                showLoading(false); // Ẩn loading sau khi nhận phản hồi
                if (response.isSuccessful() && response.body() != null) {
                    provinceList.clear();
                    provinceList.addAll(response.body());
                    showFilterPopup(
                            etSearch,
                            "Chọn tỉnh/thành phố",
                            provinceList,
                            RegionResponse::getFullName,
                            etSearch
                    );
                } else {
                    Toast.makeText(requireContext(), "Không thể tải danh sách tỉnh/thành phố", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<RegionResponse>> call, Throwable t) {
                showLoading(false); // Ẩn loading nếu lỗi
                Toast.makeText(requireContext(), "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showDistrictPopup(String provinceCode) {
        showLoading(true); // Hiển thị loading khi gọi API
        regionService.getDistricts(provinceCode).enqueue(new Callback<List<RegionResponse>>() {
            @Override
            public void onResponse(Call<List<RegionResponse>> call, Response<List<RegionResponse>> response) {
                showLoading(false); // Ẩn loading sau khi nhận phản hồi
                if (response.isSuccessful() && response.body() != null) {
                    districtList.clear();
                    districtList.addAll(response.body());
                    showFilterPopup(
                            etSearch,
                            "Chọn quận/huyện",
                            districtList,
                            RegionResponse::getFullName,
                            etSearch
                    );
                } else {
                    Toast.makeText(requireContext(), "Không thể tải danh sách quận/huyện", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<RegionResponse>> call, Throwable t) {
                showLoading(false); // Ẩn loading nếu lỗi
                Toast.makeText(requireContext(), "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showWardPopup(String districtCode) {
        showLoading(true);
        regionService.getWards(districtCode).enqueue(new Callback<List<RegionResponse>>() {
            @Override
            public void onResponse(Call<List<RegionResponse>> call, Response<List<RegionResponse>> response) {
                showLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    wardList.clear();
                    wardList.addAll(response.body());
                    showFilterPopup(
                            etSearch,
                            "Chọn phường/xã",
                            wardList,
                            RegionResponse::getFullName,
                            etSearch
                    );
                } else {
                    Toast.makeText(requireContext(), "Không thể tải danh sách phường/xã", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<RegionResponse>> call, Throwable t) {
                showLoading(false);
                Toast.makeText(requireContext(), "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}