package com.example.findnest.ui.fragment;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.example.findnest.R;
import com.example.findnest.api.GeocodingService;
import com.example.findnest.api.IPostService;
import com.example.findnest.api.IRegionService;
import com.example.findnest.api.client.auth.AuthManager;
import com.example.findnest.api.client.retrofit.RetrofitClient;
import com.example.findnest.model.responsedtos.GeocodingResponse;
import com.example.findnest.model.responsedtos.PostDetailResponse;
import com.example.findnest.model.responsedtos.RegionResponse;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.osmdroid.config.Configuration;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Marker;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class UpdatePostFragment extends Fragment {

    private static final int REQUEST_CODE_THUMBNAIL = 1;
    private static final int REQUEST_PERMISSIONS = 3;
    private static final String TAG = "UpdatePostFragment";

    private TextInputLayout tilTitle, tilPrice, tilArea, tilBedroomCount, tilBathroomCount,
            tilDescription, tilSpecificAddress, tilWard, tilDistrict, tilProvince;
    private TextView tvThumbnailError, tvMapError;
    private TextInputEditText editTitle, editPrice, editArea, editBedroomCount, editBathroomCount;
    private TextInputEditText editDescription, editSpecificAddress;
    private Spinner spinnerProvince, spinnerDistrict, spinnerWard;
    private SwitchMaterial switchNegotiablePrice, switchAiAutoFill;
    private MaterialButton btnUploadThumbnail, btnSubmit;
    private ImageView imgThumbnailPreview;
    private MapView mapView;
    private Marker selectedMarker;
    private FrameLayout loadingContainer;

    private GeocodingService geocodingService;
    private IPostService postService;
    private IRegionService regionService;
    private AuthManager authManager;
    private GeoPoint selectedLocation;
    private Uri thumbnailUri;
    private List<RegionResponse> provinces = new ArrayList<>();
    private List<RegionResponse> districts = new ArrayList<>();
    private List<RegionResponse> wards = new ArrayList<>();
    private Call<List<GeocodingResponse>> geocodingCall;

    private String postId;
    private boolean isLoadingInitialData = false; // Biến để kiểm soát trạng thái tải dữ liệu ban đầu

    public UpdatePostFragment() {
        // Required empty public constructor
    }

    public static UpdatePostFragment newInstance(AuthManager authManager, String postId) {
        UpdatePostFragment fragment = new UpdatePostFragment();
        fragment.authManager = authManager;
        fragment.postId = postId;
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (authManager == null) {
            throw new IllegalStateException("AuthManager is null");
        }
        regionService = RetrofitClient.getClient(authManager).create(IRegionService.class);
        postService = RetrofitClient.getClient(authManager).create(IPostService.class);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://nominatim.openstreetmap.org/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        geocodingService = retrofit.create(GeocodingService.class);

        Configuration.getInstance().load(getContext(), getActivity().getPreferences(0));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_create_post, container, false);

        // Ánh xạ views
        tilTitle = view.findViewById(R.id.til_title);
        tilPrice = view.findViewById(R.id.til_price);
        tilArea = view.findViewById(R.id.til_area);
        tilBedroomCount = view.findViewById(R.id.til_bedroom_count);
        tilBathroomCount = view.findViewById(R.id.til_bathroom_count);
        tilDescription = view.findViewById(R.id.til_description);
        tilSpecificAddress = view.findViewById(R.id.til_specific_address);
        tilWard = view.findViewById(R.id.til_ward);
        tilDistrict = view.findViewById(R.id.til_district);
        tilProvince = view.findViewById(R.id.til_province);
        tvThumbnailError = view.findViewById(R.id.tv_thumbnail_error);
        tvMapError = view.findViewById(R.id.tv_map_error);
        editTitle = view.findViewById(R.id.edit_title);
        editPrice = view.findViewById(R.id.edit_price);
        editArea = view.findViewById(R.id.edit_area);
        editBedroomCount = view.findViewById(R.id.edit_bedroom_count);
        editBathroomCount = view.findViewById(R.id.edit_bathroom_count);
        editDescription = view.findViewById(R.id.edit_description);
        editSpecificAddress = view.findViewById(R.id.edit_specific_address);
        spinnerProvince = view.findViewById(R.id.spinner_province);
        spinnerDistrict = view.findViewById(R.id.spinner_district);
        spinnerWard = view.findViewById(R.id.spinner_ward);
        btnUploadThumbnail = view.findViewById(R.id.btn_upload_thumbnail);
        btnSubmit = view.findViewById(R.id.btn_submit);
        switchNegotiablePrice = view.findViewById(R.id.switch_negotiable_price);
        switchAiAutoFill = view.findViewById(R.id.switch_ai_auto_fill);
        imgThumbnailPreview = view.findViewById(R.id.img_thumbnail_preview);
        mapView = view.findViewById(R.id.map_view);
        loadingContainer = view.findViewById(R.id.loading_container);

        // Ẩn các thành phần liên quan đến images
        view.findViewById(R.id.btn_upload_images).setVisibility(View.GONE);
        view.findViewById(R.id.recycler_images).setVisibility(View.GONE);
        view.findViewById(R.id.tv_images_error).setVisibility(View.GONE);

        // Thiết lập bản đồ OSM
        setupMap();

        // Gán adapter mặc định cho các Spinner
        setupDefaultSpinners();

        // Sự kiện nút
        btnUploadThumbnail.setOnClickListener(v -> pickImage(REQUEST_CODE_THUMBNAIL));
        btnSubmit.setOnClickListener(v -> submitPost());

        // Sự kiện switch "Giá thỏa thuận"
        switchNegotiablePrice.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                editPrice.setEnabled(false);
                editPrice.setText("0");
            } else {
                editPrice.setEnabled(true);
            }
        });

        mapView.setOnTouchListener((v, event) -> {
            v.getParent().requestDisallowInterceptTouchEvent(true);
            return false;
        });

        checkPermissions();
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        loadPostData(); // Tải dữ liệu bài đăng sau khi view được tạo
    }

    private void setupMap() {
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setMultiTouchControls(true);
        mapView.getController().setZoom(10.0);

        MapEventsOverlay mapEventsOverlay = new MapEventsOverlay(new MapEventsReceiver() {
            @Override
            public boolean singleTapConfirmedHelper(GeoPoint p) {
                addMarkerAtLocation(p);
                selectedLocation = p;
                return true;
            }

            @Override
            public boolean longPressHelper(GeoPoint p) {
                return false;
            }
        });
        mapView.getOverlays().add(mapEventsOverlay);
    }

    private void addMarkerAtLocation(GeoPoint location) {
        if (selectedMarker != null) {
            mapView.getOverlays().remove(selectedMarker);
        }
        selectedMarker = new Marker(mapView);
        selectedMarker.setPosition(location);
        selectedMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        selectedMarker.setTitle("Vị trí đã chọn");
        selectedMarker.setSnippet("Lat: " + location.getLatitude() + ", Lon: " + location.getLongitude());
        mapView.getOverlays().add(selectedMarker);
        mapView.invalidate();
    }

    private void checkPermissions() {
        List<String> permissionsNeeded = new ArrayList<>();

        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            permissionsNeeded.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }

        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            permissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.READ_MEDIA_IMAGES)
                    != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add(Manifest.permission.READ_MEDIA_IMAGES);
            }
        } else {
            if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add(Manifest.permission.READ_EXTERNAL_STORAGE);
            }
        }

        if (!permissionsNeeded.isEmpty()) {
            requestPermissions(permissionsNeeded.toArray(new String[0]), REQUEST_PERMISSIONS);
        }
    }

    private void setupDefaultSpinners() {
        ArrayAdapter<String> provinceAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item,
                new ArrayList<>(Collections.singletonList("Chọn thành phố")));
        provinceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerProvince.setAdapter(provinceAdapter);

        ArrayAdapter<String> districtAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item,
                new ArrayList<>(Collections.singletonList("Chọn quận/huyện")));
        districtAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDistrict.setAdapter(districtAdapter);

        ArrayAdapter<String> wardAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item,
                new ArrayList<>(Collections.singletonList("Chọn phường/xã")));
        wardAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerWard.setAdapter(wardAdapter);
    }

    private void setupSpinners() {
        spinnerProvince.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (isLoadingInitialData || position == 0 || provinces.isEmpty()) {
                    districts.clear();
                    updateDistrictSpinner();
                    wards.clear();
                    updateWardSpinner();
                    return;
                }
                RegionResponse selectedProvince = provinces.get(position - 1);
                loadDistricts(selectedProvince.getCode());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                districts.clear();
                updateDistrictSpinner();
                wards.clear();
                updateWardSpinner();
            }
        });

        spinnerDistrict.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (isLoadingInitialData || position == 0 || districts.isEmpty()) {
                    wards.clear();
                    updateWardSpinner();
                    return;
                }
                RegionResponse selectedDistrict = districts.get(position - 1);
                loadWards(selectedDistrict.getCode());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                wards.clear();
                updateWardSpinner();
            }
        });

        spinnerWard.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (isLoadingInitialData || position == 0 || wards.isEmpty()) {
                    return; // Không làm gì khi đang tải dữ liệu ban đầu
                }
                if (spinnerProvince.getSelectedItemPosition() > 0 && spinnerDistrict.getSelectedItemPosition() > 0 &&
                        !provinces.isEmpty() && !districts.isEmpty()) {
                    RegionResponse selectedWard = wards.get(position - 1);
                    String locationQuery = selectedWard.getFullName() + ", " +
                            districts.get(spinnerDistrict.getSelectedItemPosition() - 1).getFullName() + ", " +
                            provinces.get(spinnerProvince.getSelectedItemPosition() - 1).getFullName() + ", Vietnam";
                    updateMap(locationQuery);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void loadProvinces() {
        showLoading(true);
        regionService.getProvinces().enqueue(new Callback<List<RegionResponse>>() {
            @Override
            public void onResponse(Call<List<RegionResponse>> call, Response<List<RegionResponse>> response) {
                showLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    provinces.clear();
                    provinces.addAll(response.body());
                    updateProvinceSpinner();
                } else {
                    Log.e(TAG, "Failed to load provinces: " + response.code());
                    Toast.makeText(getContext(), "Không thể tải danh sách tỉnh/thành phố", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<RegionResponse>> call, Throwable t) {
                showLoading(false);
                Log.e(TAG, "Error loading provinces: " + t.getMessage());
                Toast.makeText(getContext(), "Lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadDistricts(String provinceCode) {
        showLoading(true);
        regionService.getDistricts(provinceCode).enqueue(new Callback<List<RegionResponse>>() {
            @Override
            public void onResponse(Call<List<RegionResponse>> call, Response<List<RegionResponse>> response) {
                showLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    districts.clear();
                    districts.addAll(response.body());
                    updateDistrictSpinner();
                } else {
                    Log.e(TAG, "Failed to load districts: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<RegionResponse>> call, Throwable t) {
                showLoading(false);
                Log.e(TAG, "Error loading districts: " + t.getMessage());
                Toast.makeText(getContext(), "Lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadWards(String districtCode) {
        showLoading(true);
        regionService.getWards(districtCode).enqueue(new Callback<List<RegionResponse>>() {
            @Override
            public void onResponse(Call<List<RegionResponse>> call, Response<List<RegionResponse>> response) {
                showLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    wards.clear();
                    wards.addAll(response.body());
                    updateWardSpinner();
                } else {
                    Log.e(TAG, "Failed to load wards: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<RegionResponse>> call, Throwable t) {
                showLoading(false);
                Log.e(TAG, "Error loading wards: " + t.getMessage());
                Toast.makeText(getContext(), "Lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateProvinceSpinner() {
        List<String> provinceNames = new ArrayList<>();
        provinceNames.add("Chọn thành phố");
        provinceNames.addAll(provinces.stream().map(RegionResponse::getFullName).collect(Collectors.toList()));
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, provinceNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerProvince.setAdapter(adapter);
    }

    private void updateDistrictSpinner() {
        List<String> districtNames = new ArrayList<>();
        districtNames.add("Chọn quận/huyện");
        districtNames.addAll(districts.stream().map(RegionResponse::getFullName).collect(Collectors.toList()));
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, districtNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDistrict.setAdapter(adapter);
    }

    private void updateWardSpinner() {
        List<String> wardNames = new ArrayList<>();
        wardNames.add("Chọn phường/xã");
        wardNames.addAll(wards.stream().map(RegionResponse::getFullName).collect(Collectors.toList()));
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, wardNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerWard.setAdapter(adapter);
    }

    private long lastMapUpdateTime = 0;
    private void updateMap(String locationQuery) {

        long currentTime = System.currentTimeMillis();
        if (currentTime - lastMapUpdateTime < 2000) { // Đảm bảo ít nhất 1 giây giữa các yêu cầu
            Log.w(TAG, "Skipping map update due to rate limit");
            return;
        }
        lastMapUpdateTime = currentTime;
        Log.e(TAG, "CALL MAP " + locationQuery);
        if (geocodingCall != null) {
            geocodingCall.cancel();
        }
        showLoading(true);
        geocodingCall = geocodingService.getCoordinates(locationQuery, "json", 1);
        geocodingCall.enqueue(new Callback<List<GeocodingResponse>>() {
            @Override
            public void onResponse(Call<List<GeocodingResponse>> call, Response<List<GeocodingResponse>> response) {
                showLoading(false);
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    GeoPoint location = response.body().get(0).toGeoPoint();
                    mapView.getController().setCenter(location);
                    mapView.getController().setZoom(getZoomLevel(locationQuery));
                    addMarkerAtLocation(location);
                    selectedLocation = location;
                } else {
                    Log.e(TAG, "Failed to get coordinates for: " + locationQuery);
                }
            }

            @Override
            public void onFailure(Call<List<GeocodingResponse>> call, Throwable t) {
                showLoading(false);
                Log.e(TAG, "Error updating map: " + t.getMessage());
                Toast.makeText(getContext(), "Lỗi lấy tọa độ: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private double getZoomLevel(String locationQuery) {
        if (locationQuery.contains("Xã") || locationQuery.contains("Phường")) return 18.0;
        if (locationQuery.contains("Quận") || locationQuery.contains("Huyện")) return 15.0;
        return 13.0;
    }

    private void pickImage(int requestCode) {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, requestCode);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && data != null) {
            if (requestCode == REQUEST_CODE_THUMBNAIL) {
                thumbnailUri = data.getData();
                imgThumbnailPreview.setImageURI(thumbnailUri);
                imgThumbnailPreview.setVisibility(View.VISIBLE);
            }
        }
    }

    private String getRealPathFromURI(Uri uri) {
        String[] projection = {MediaStore.Images.Media.DATA};
        android.database.Cursor cursor = getActivity().getContentResolver().query(uri, projection, null, null, null);
        if (cursor != null) {
            int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            String path = cursor.getString(columnIndex);
            cursor.close();
            return path;
        }
        return uri.getPath();
    }

    private void showLoading(boolean isLoading) {
        if (loadingContainer != null) {
            loadingContainer.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        }
        View view = getView();
        if (view != null) {
            view.setEnabled(!isLoading);
        }
    }

    private void loadPostData() {
        showLoading(true);
        isLoadingInitialData = true; // Bật cờ khi bắt đầu tải dữ liệu
        postService.getPost(UUID.fromString(postId)).enqueue(new Callback<PostDetailResponse>() {
            @Override
            public void onResponse(Call<PostDetailResponse> call, Response<PostDetailResponse> response) {
                showLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    PostDetailResponse post = response.body();
                    editTitle.setText(post.getTitle());
                    editPrice.setText(post.getPrice().toString());
                    switchNegotiablePrice.setChecked(post.isNegotiatedPrice());
                    editSpecificAddress.setText(post.getAddress());
                    editArea.setText(String.valueOf(post.getArea()));
                    editDescription.setText(post.getDescription());
                    editBedroomCount.setText(String.valueOf(post.getBedRoomCount()));
                    editBathroomCount.setText(String.valueOf(post.getBathRoomCount()));
                    selectedLocation = new GeoPoint(post.getLatitude(), post.getLongitude());
                    addMarkerAtLocation(selectedLocation);
                    mapView.getController().setCenter(selectedLocation);
                    mapView.getController().setZoom(18.0); // Zoom mặc định khi tải dữ liệu ban đầu

                    // Tải dữ liệu tỉnh, quận, phường
                    loadProvincesForPost(post);
                } else {
                    Log.e(TAG, "Failed to load post data: " + response.code());
                    Toast.makeText(getContext(), "Lỗi tải dữ liệu bài đăng", Toast.LENGTH_SHORT).show();
                    isLoadingInitialData = false; // Tắt cờ nếu thất bại
                }
            }

            @Override
            public void onFailure(Call<PostDetailResponse> call, Throwable t) {
                showLoading(false);
                Log.e(TAG, "Error loading post data: " + t.getMessage());
                Toast.makeText(getContext(), "Lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                isLoadingInitialData = false; // Tắt cờ nếu thất bại
            }
        });
    }

    private void loadProvincesForPost(PostDetailResponse post) {
        showLoading(true);
        regionService.getProvinces().enqueue(new Callback<List<RegionResponse>>() {
            @Override
            public void onResponse(Call<List<RegionResponse>> call, Response<List<RegionResponse>> response) {
                showLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    provinces.clear();
                    provinces.addAll(response.body());
                    updateProvinceSpinner();

                    int provincePosition = -1;
                    for (int i = 0; i < provinces.size(); i++) {
                        if (provinces.get(i).getCode().equals(post.getProvince().getCode())) {
                            provincePosition = i + 1; // +1 vì có mục "Chọn thành phố" ở đầu
                            break;
                        }
                    }
                    if (provincePosition != -1) {
                        spinnerProvince.setSelection(provincePosition);
                        loadDistrictsForPost(post, provinces.get(provincePosition - 1).getCode());
                    } else {
                        Log.w(TAG, "Province code not found: " + post.getProvince().getCode());
                        isLoadingInitialData = false; // Tắt cờ nếu không tìm thấy tỉnh
                    }
                    setupSpinners();
                } else {
                    Log.e(TAG, "Failed to load provinces: " + response.code());
                    Toast.makeText(getContext(), "Không thể tải danh sách tỉnh/thành phố", Toast.LENGTH_SHORT).show();
                    isLoadingInitialData = false; // Tắt cờ nếu thất bại
                }
            }

            @Override
            public void onFailure(Call<List<RegionResponse>> call, Throwable t) {
                showLoading(false);
                Log.e(TAG, "Error loading provinces: " + t.getMessage());
                Toast.makeText(getContext(), "Lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                isLoadingInitialData = false; // Tắt cờ nếu thất bại
            }
        });
    }

    private void loadDistrictsForPost(PostDetailResponse post, String provinceCode) {
        showLoading(true);
        regionService.getDistricts(provinceCode).enqueue(new Callback<List<RegionResponse>>() {
            @Override
            public void onResponse(Call<List<RegionResponse>> call, Response<List<RegionResponse>> response) {
                showLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    districts.clear();
                    districts.addAll(response.body());
                    updateDistrictSpinner();

                    int districtPosition = -1;
                    for (int i = 0; i < districts.size(); i++) {
                        if (districts.get(i).getCode().equals(post.getDistrict().getCode())) {
                            districtPosition = i + 1; // +1 vì có mục "Chọn quận/huyện" ở đầu
                            break;
                        }
                    }
                    if (districtPosition != -1) {
                        spinnerDistrict.setSelection(districtPosition);
                        loadWardsForPost(post, districts.get(districtPosition - 1).getCode());
                    } else {
                        Log.w(TAG, "District code not found: " + post.getDistrict().getCode());
                        isLoadingInitialData = false; // Tắt cờ nếu không tìm thấy quận
                    }
                } else {
                    Log.e(TAG, "Failed to load districts: " + response.code());
                    Toast.makeText(getContext(), "Không thể tải danh sách quận/huyện", Toast.LENGTH_SHORT).show();
                    isLoadingInitialData = false; // Tắt cờ nếu thất bại
                }
            }

            @Override
            public void onFailure(Call<List<RegionResponse>> call, Throwable t) {
                showLoading(false);
                Log.e(TAG, "Error loading districts: " + t.getMessage());
                Toast.makeText(getContext(), "Lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                isLoadingInitialData = false; // Tắt cờ nếu thất bại
            }
        });
    }

    private void loadWardsForPost(PostDetailResponse post, String districtCode) {
        showLoading(true);
        regionService.getWards(districtCode).enqueue(new Callback<List<RegionResponse>>() {
            @Override
            public void onResponse(Call<List<RegionResponse>> call, Response<List<RegionResponse>> response) {
                showLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    wards.clear();
                    wards.addAll(response.body());
                    updateWardSpinner();

                    int wardPosition = -1;
                    for (int i = 0; i < wards.size(); i++) {
                        if (wards.get(i).getCode().equals(post.getWard().getCode())) {
                            wardPosition = i + 1;
                            break;
                        }
                    }
                    if (wardPosition != -1) {
                        spinnerWard.setSelection(wardPosition);

                        int provincePos = spinnerProvince.getSelectedItemPosition();
                        int districtPos = spinnerDistrict.getSelectedItemPosition();
                        int wardPos = spinnerWard.getSelectedItemPosition();

                        if (provincePos > 0 && districtPos > 0 && wardPos > 0 &&
                                !provinces.isEmpty() && !districts.isEmpty() && !wards.isEmpty()) {
                            try {
                                String locationQuery = wards.get(wardPos - 1).getFullName() + ", " +
                                        districts.get(districtPos - 1).getFullName() + ", " +
                                        provinces.get(provincePos - 1).getFullName() + ", Vietnam";
                                updateMap(locationQuery);
                            } catch (IndexOutOfBoundsException e) {
                                Log.e(TAG, "Index out of bounds when creating locationQuery: " + e.getMessage());
                                Toast.makeText(getContext(), "Lỗi tải địa chỉ", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Log.w(TAG, "Spinner data not fully loaded yet");
                        }
                    } else {
                        Log.w(TAG, "Ward code not found: " + post.getWard().getCode());
                    }
                    isLoadingInitialData = false; // Tắt cờ SAU KHI updateMap hoàn tất
                } else {
                    Log.e(TAG, "Failed to load wards: " + response.code());
                    Toast.makeText(getContext(), "Không thể tải danh sách phường/xã", Toast.LENGTH_SHORT).show();
                    isLoadingInitialData = false;
                }
            }

            @Override
            public void onFailure(Call<List<RegionResponse>> call, Throwable t) {
                showLoading(false);
                Log.e(TAG, "Error loading wards: " + t.getMessage());
                Toast.makeText(getContext(), "Lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                isLoadingInitialData = false;
            }
        });
    }

    private boolean validateInput(String title, String priceStr, String areaStr, String bedroomCountStr,
                                  String bathroomCountStr, String description, String specificAddress,
                                  boolean isNegotiablePrice) {
        boolean isValid = true;

        tilTitle.setError(null);
        tilPrice.setError(null);
        tilArea.setError(null);
        tilBedroomCount.setError(null);
        tilBathroomCount.setError(null);
        tilDescription.setError(null);
        tilSpecificAddress.setError(null);
        tilWard.setError(null);
        tilDistrict.setError(null);
        tilProvince.setError(null);
        tvThumbnailError.setText("");
        tvThumbnailError.setVisibility(View.GONE);
        tvMapError.setText("");
        tvMapError.setVisibility(View.GONE);

        if (title.isEmpty()) {
            tilTitle.setError("Tiêu đề không được để trống");
            isValid = false;
        } else if (title.length() > 500) {
            tilTitle.setError("Tiêu đề không được vượt quá 500 ký tự");
            isValid = false;
        }

        if (!isNegotiablePrice) {
            if (priceStr.isEmpty()) {
                tilPrice.setError("Giá không được để trống");
                isValid = false;
            } else {
                try {
                    double price = Double.parseDouble(priceStr);
                    if (price < 0) {
                        tilPrice.setError("Giá không được âm");
                        isValid = false;
                    } else if (price > 99999999999999.98) {
                        tilPrice.setError("Giá vượt quá giới hạn");
                        isValid = false;
                    }
                } catch (NumberFormatException e) {
                    tilPrice.setError("Giá phải là số hợp lệ");
                    isValid = false;
                }
            }
        }

        if (areaStr.isEmpty()) {
            tilArea.setError("Diện tích không được để trống");
            isValid = false;
        } else {
            try {
                int area = Integer.parseInt(areaStr);
                if (area <= 0) {
                    tilArea.setError("Diện tích phải lớn hơn 0");
                    isValid = false;
                } else if (area > 2147483647) {
                    tilArea.setError("Diện tích vượt quá giới hạn");
                    isValid = false;
                }
            } catch (NumberFormatException e) {
                tilArea.setError("Diện tích phải là số nguyên hợp lệ");
                isValid = false;
            }
        }

        if (bedroomCountStr.isEmpty()) {
            tilBedroomCount.setError("Số phòng ngủ không được để trống");
            isValid = false;
        } else {
            try {
                int bedroomCount = Integer.parseInt(bedroomCountStr);
                if (bedroomCount < 0) {
                    tilBedroomCount.setError("Số phòng ngủ không được âm");
                    isValid = false;
                } else if (bedroomCount > 2147483647) {
                    tilBedroomCount.setError("Số phòng ngủ vượt quá giới hạn");
                    isValid = false;
                }
            } catch (NumberFormatException e) {
                tilBedroomCount.setError("Số phòng ngủ phải là số nguyên hợp lệ");
                isValid = false;
            }
        }

        if (bathroomCountStr.isEmpty()) {
            tilBathroomCount.setError("Số phòng tắm không được để trống");
            isValid = false;
        } else {
            try {
                int bathroomCount = Integer.parseInt(bathroomCountStr);
                if (bathroomCount < 0) {
                    tilBathroomCount.setError("Số phòng tắm không được âm");
                    isValid = false;
                } else if (bathroomCount > 2147483647) {
                    tilBathroomCount.setError("Số phòng tắm vượt quá giới hạn");
                    isValid = false;
                }
            } catch (NumberFormatException e) {
                tilBathroomCount.setError("Số phòng tắm phải là số nguyên hợp lệ");
                isValid = false;
            }
        }

        if (description.length() > 5000) {
            tilDescription.setError("Mô tả không được vượt quá 5000 ký tự");
            isValid = false;
        }

        if (specificAddress.isEmpty()) {
            tilSpecificAddress.setError("Địa chỉ không được để trống");
            isValid = false;
        }

        if (spinnerWard.getSelectedItemPosition() == 0) {
            tilWard.setError("Vui lòng chọn phường/xã");
            isValid = false;
        }
        if (spinnerDistrict.getSelectedItemPosition() == 0) {
            tilDistrict.setError("Vui lòng chọn quận/huyện");
            isValid = false;
        }
        if (spinnerProvince.getSelectedItemPosition() == 0) {
            tilProvince.setError("Vui lòng chọn tỉnh/thành phố");
            isValid = false;
        }

        if (selectedLocation == null) {
            tvMapError.setText("Vui lòng chọn vị trí trên bản đồ");
            tvMapError.setVisibility(View.VISIBLE);
            isValid = false;
        }

        return isValid;
    }

    private void submitPost() {
        String title = editTitle.getText().toString().trim();
        String priceStr = editPrice.getText().toString().trim();
        String areaStr = editArea.getText().toString().trim();
        String bedroomCountStr = editBedroomCount.getText().toString().trim();
        String bathroomCountStr = editBathroomCount.getText().toString().trim();
        String description = editDescription.getText().toString().trim();
        String specificAddress = editSpecificAddress.getText().toString().trim();
        boolean isNegotiablePrice = switchNegotiablePrice.isChecked();
        boolean isAiDescription = switchAiAutoFill.isChecked();

        if (!validateInput(title, priceStr, areaStr, bedroomCountStr, bathroomCountStr, description, specificAddress, isNegotiablePrice)) {
            Snackbar.make(getView(), "Vui lòng kiểm tra và sửa lỗi", Snackbar.LENGTH_LONG).show();
            return;
        }

        showLoading(true);
        double price = isNegotiablePrice ? 0 : Double.parseDouble(priceStr);
        int area = Integer.parseInt(areaStr);
        int bedroomCount = Integer.parseInt(bedroomCountStr);
        int bathRoomCount = Integer.parseInt(bathroomCountStr);
        String wardCode = wards.get(spinnerWard.getSelectedItemPosition() - 1).getCode();

        RequestBody titleBody = RequestBody.create(MediaType.parse("text/plain"), title);
        RequestBody priceBody = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(price));
        RequestBody isNegotiatedPriceBody = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(isNegotiablePrice));
        RequestBody addressBody = RequestBody.create(MediaType.parse("text/plain"), specificAddress);
        RequestBody areaBody = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(area));
        RequestBody descriptionBody = RequestBody.create(MediaType.parse("text/plain"), description);
        RequestBody latitudeBody = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(selectedLocation.getLatitude()));
        RequestBody longitudeBody = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(selectedLocation.getLongitude()));
        RequestBody wardCodeBody = RequestBody.create(MediaType.parse("text/plain"), wardCode);
        RequestBody bedRoomCountBody = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(bedroomCount));
        RequestBody bathRoomCountBody = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(bathRoomCount));
        RequestBody isAiDescriptionBody = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(isAiDescription));

        MultipartBody.Part thumbnailPart = null;
        if (thumbnailUri != null) {
            File thumbnailFile = new File(getRealPathFromURI(thumbnailUri));
            RequestBody thumbnailRequestBody = RequestBody.create(MediaType.parse("image/*"), thumbnailFile);
            thumbnailPart = MultipartBody.Part.createFormData("thumbnail", thumbnailFile.getName(), thumbnailRequestBody);
        }

        Call<PostDetailResponse> call = postService.updatePost(
                UUID.fromString(postId),
                titleBody,
                priceBody,
                isNegotiatedPriceBody,
                addressBody,
                areaBody,
                descriptionBody,
                latitudeBody,
                longitudeBody,
                wardCodeBody,
                bedRoomCountBody,
                bathRoomCountBody,
                isAiDescriptionBody,
                thumbnailPart
        );

        call.enqueue(new Callback<PostDetailResponse>() {
            @Override
            public void onResponse(Call<PostDetailResponse> call, Response<PostDetailResponse> response) {
                showLoading(false);
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "Cập nhật bài đăng thành công", Toast.LENGTH_SHORT).show();
                    getParentFragmentManager().popBackStack();
                } else {
                    try {
                        String errorBody = response.errorBody() != null ? response.errorBody().string() : "Không có nội dung lỗi";
                        Toast.makeText(getContext(), "Lỗi cập nhật: " + errorBody, Toast.LENGTH_LONG).show();
                        Log.e("Hello", errorBody);
                    } catch (IOException e) {
                        Toast.makeText(getContext(), "Không thể đọc chi tiết lỗi", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<PostDetailResponse> call, Throwable t) {
                showLoading(false);
                Toast.makeText(getContext(), "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }
}

