package com.example.findnest.ui;

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
import android.view.MotionEvent;
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.findnest.R;
import com.example.findnest.api.GeocodingService;
import com.example.findnest.api.IPostService; // Thêm import cho IPostService
import com.example.findnest.api.IRegionService;
import com.example.findnest.api.client.auth.AuthManager;
import com.example.findnest.api.client.retrofit.RetrofitClient;
import com.example.findnest.model.responsedtos.GeocodingResponse;
import com.example.findnest.model.responsedtos.PostDetailResponse;
import com.example.findnest.model.responsedtos.RegionResponse;
import com.example.findnest.ui.activity.MainActivity;
import com.example.findnest.ui.fragment.PostDetailFragment;
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
import java.util.List;
import java.util.stream.Collectors;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class CreatePostFragment extends Fragment {

    private static final int REQUEST_CODE_THUMBNAIL = 1;
    private static final int REQUEST_CODE_IMAGES = 2;
    private static final int REQUEST_PERMISSIONS = 3;

    private TextInputLayout tilTitle, tilPrice, tilArea, tilBedroomCount, tilBathroomCount, tilDescription, tilSpecificAddress, tilWard, tilDistrict, tilProvince;
    private TextView tvThumbnailError, tvImagesError, tvMapError;
    // Views
    private TextInputEditText editTitle, editPrice, editArea, editBedroomCount, editBathroomCount;
    private TextInputEditText editDescription, editSpecificAddress;
    private Spinner spinnerProvince, spinnerDistrict, spinnerWard;
    private SwitchMaterial switchNegotiablePrice, switchAiAutoFill;
    private MaterialButton btnUploadThumbnail, btnUploadImages, btnSubmit;
    private ImageView imgThumbnailPreview;
    private RecyclerView recyclerImages;
    private MapView mapView;
    private Marker selectedMarker;

    private ProgressBar progressBar;
    private View overlay;

    private FrameLayout loadingContainer;

    private GeocodingService geocodingService;
    private IPostService postService; // Thêm service cho API post

    // Data
    private Uri thumbnailUri;
    private List<File> selectedImageFiles = new ArrayList<>();
    private ImageAdapter imageAdapter;
    private IRegionService regionService;
    private List<RegionResponse> provinces = new ArrayList<>();
    private List<RegionResponse> districts = new ArrayList<>();
    private List<RegionResponse> wards = new ArrayList<>();
    private AuthManager authManager;

    private GeoPoint selectedLocation;

    private Call<List<GeocodingResponse>> geocodingCall;

//    private Call<Void> postCall;

    public CreatePostFragment() {
        // Required empty public constructor
    }

    public static CreatePostFragment newInstance(AuthManager authManager) {
        CreatePostFragment fragment = new CreatePostFragment();
        fragment.authManager = authManager;
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (authManager == null) {
            throw new IllegalStateException("AuthManager is null");
        }
        regionService = RetrofitClient.getClient(authManager).create(IRegionService.class);
        postService = RetrofitClient.getClient(authManager).create(IPostService.class); // Khởi tạo postService

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

        // Ánh xạ TextInputLayout
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
        tvImagesError = view.findViewById(R.id.tv_images_error);
        tvMapError = view.findViewById(R.id.tv_map_error);

        // Ánh xạ views
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
        btnUploadImages = view.findViewById(R.id.btn_upload_images);
        btnSubmit = view.findViewById(R.id.btn_submit);
        switchNegotiablePrice = view.findViewById(R.id.switch_negotiable_price);
        switchAiAutoFill = view.findViewById(R.id.switch_ai_auto_fill);
        imgThumbnailPreview = view.findViewById(R.id.img_thumbnail_preview);
        recyclerImages = view.findViewById(R.id.recycler_images);
        mapView = view.findViewById(R.id.map_view);
        progressBar = view.findViewById(R.id.progress_bar);
        overlay = view.findViewById(R.id.overlay);
        loadingContainer = view.findViewById(R.id.loading_container);

        // Thiết lập RecyclerView
        recyclerImages.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        imageAdapter = new ImageAdapter(selectedImageFiles);
        recyclerImages.setAdapter(imageAdapter);

        // Thiết lập bản đồ OSM
        setupMap();

        // Thiết lập Spinner và load dữ liệu
        setupSpinners();

        // Sự kiện nút
        btnUploadThumbnail.setOnClickListener(v -> pickImage(REQUEST_CODE_THUMBNAIL));
        btnUploadImages.setOnClickListener(v -> pickMultipleImages(REQUEST_CODE_IMAGES));
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

        mapView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                v.getParent().requestDisallowInterceptTouchEvent(true);
                return false;
            }
        });

        checkPermissions();

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Gọi updateMap sau khi view đã được tạo
        updateMap("Hà Nội, Vietnam");
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
        if (mapView == null) {
            Log.w("CreatePostFragment", "MapView null");
            return;
        }

        if (selectedMarker != null) {
            if (selectedMarker.isInfoWindowShown()) {
                selectedMarker.closeInfoWindow();
            }
            mapView.getOverlays().remove(selectedMarker);
        }

        selectedMarker = new Marker(mapView);
        selectedMarker.setPosition(location);
        selectedMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        selectedMarker.setTitle("Vị trí đã chọn");
        selectedMarker.setSnippet("Lat: " + location.getLatitude() + ", Lon: " + location.getLongitude());
        selectedMarker.setInfoWindow(new org.osmdroid.views.overlay.infowindow.BasicInfoWindow(org.osmdroid.library.R.layout.bonuspack_bubble, mapView));
        selectedLocation = location;

        mapView.getOverlays().add(selectedMarker);
        mapView.invalidate();

        Toast.makeText(getContext(), "Đã chọn: Lat " + location.getLatitude() + ", Lon " + location.getLongitude(), Toast.LENGTH_SHORT).show();
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

        // Kiểm tra quyền đọc ảnh dựa trên phiên bản Android
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // Android 13+
            if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.READ_MEDIA_IMAGES)
                    != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add(Manifest.permission.READ_MEDIA_IMAGES);
            }
        } else { // Android 12 trở xuống
            if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add(Manifest.permission.READ_EXTERNAL_STORAGE);
            }
        }

        // Yêu cầu quyền nếu có bất kỳ quyền nào chưa được cấp
        if (!permissionsNeeded.isEmpty()) {
            requestPermissions(permissionsNeeded.toArray(new String[0]), REQUEST_PERMISSIONS);
        }
    }


    private void setupSpinners() {
        loadProvinces();

        spinnerProvince.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position > 0) {
                    RegionResponse selectedProvince = provinces.get(position - 1);
                    loadDistricts(selectedProvince.getCode());
                    updateMap(selectedProvince.getFullName() + ", Vietnam");
                } else {
                    districts.clear();
                    updateDistrictSpinner();
                    wards.clear();
                    updateWardSpinner();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                districts.clear();
                updateDistrictSpinner();
            }
        });

        spinnerDistrict.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position > 0 && !districts.isEmpty()) {
                    RegionResponse selectedDistrict = districts.get(position - 1);
                    loadWards(selectedDistrict.getCode());
                    updateMap(selectedDistrict.getFullName() + ", " +
                            provinces.get(spinnerProvince.getSelectedItemPosition() - 1).getFullName() + ", Vietnam");
                } else {
                    wards.clear();
                    updateWardSpinner();
                }
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
                if (position > 0 && !wards.isEmpty()) {
                    RegionResponse selectedWard = wards.get(position - 1);
                    updateMap(selectedWard.getFullName() + ", " +
                            districts.get(spinnerDistrict.getSelectedItemPosition() - 1).getFullName() + ", " +
                            provinces.get(spinnerProvince.getSelectedItemPosition() - 1).getFullName() + ", Vietnam");
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void loadProvinces() {
        regionService.getProvinces().enqueue(new Callback<List<RegionResponse>>() {
            @Override
            public void onResponse(Call<List<RegionResponse>> call, Response<List<RegionResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    provinces.clear();
                    provinces.addAll(response.body());
                    updateProvinceSpinner();
                } else {
                    Toast.makeText(getContext(), "Lỗi load tỉnh/thành phố: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<RegionResponse>> call, Throwable t) {
                Toast.makeText(getContext(), "Lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("ERROR", t.getMessage());
            }
        });
    }

    private void loadDistricts(String provinceCode) {
        regionService.getDistricts(provinceCode).enqueue(new Callback<List<RegionResponse>>() {
            @Override
            public void onResponse(Call<List<RegionResponse>> call, Response<List<RegionResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    districts.clear();
                    districts.addAll(response.body());
                    updateDistrictSpinner();
                } else {
                    Toast.makeText(getContext(), "Lỗi load quận/huyện: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<RegionResponse>> call, Throwable t) {
                Toast.makeText(getContext(), "Lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("ERROR", t.getMessage());
            }
        });
    }

    private void loadWards(String districtCode) {
        regionService.getWards(districtCode).enqueue(new Callback<List<RegionResponse>>() {
            @Override
            public void onResponse(Call<List<RegionResponse>> call, Response<List<RegionResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    wards.clear();
                    wards.addAll(response.body());
                    updateWardSpinner();
                } else {
                    Toast.makeText(getContext(), "Lỗi load xã/phường: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<RegionResponse>> call, Throwable t) {
                Toast.makeText(getContext(), "Lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("ERROR", t.getMessage());
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
        spinnerProvince.setSelection(0);
    }

    private void updateDistrictSpinner() {
        List<String> districtNames = new ArrayList<>();
        districtNames.add("Chọn quận/huyện");
        districtNames.addAll(districts.stream().map(RegionResponse::getFullName).collect(Collectors.toList()));

        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, districtNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDistrict.setAdapter(adapter);
        spinnerDistrict.setSelection(0);
    }

    private void updateWardSpinner() {
        List<String> wardNames = new ArrayList<>();
        wardNames.add("Chọn phường/xã");
        wardNames.addAll(wards.stream().map(RegionResponse::getFullName).collect(Collectors.toList()));

        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, wardNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerWard.setAdapter(adapter);
        spinnerWard.setSelection(0);
    }

    private void updateMap(String locationQuery) {
        if (geocodingCall != null) {
            geocodingCall.cancel();
        }
        showLoading(true);
        geocodingCall = geocodingService.getCoordinates(locationQuery, "json", 1);
        geocodingCall.enqueue(new Callback<List<GeocodingResponse>>() {
            @Override
            public void onResponse(Call<List<GeocodingResponse>> call, Response<List<GeocodingResponse>> response) {
                showLoading(false);
                if (call.isCanceled()) {
                    Log.d("CreatePostFragment", "Geocoding call đã bị hủy, bỏ qua onResponse");
                    return;
                }
                if (!isAdded() || getView() == null || mapView == null) {
                    Log.w("CreatePostFragment", "Fragment không còn gắn hoặc view bị hủy, bỏ qua cập nhật bản đồ");
                    return;
                }
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    GeoPoint location = response.body().get(0).toGeoPoint();
                    mapView.getController().setCenter(location);
                    mapView.getController().setZoom(getZoomLevel(locationQuery));
                    addMarkerAtLocation(location);
                } else {
                    Toast.makeText(getContext(), "Không tìm thấy tọa độ cho khu vực này", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<GeocodingResponse>> call, Throwable t) {
                showLoading(false);
                if (call.isCanceled()) {
                    Log.d("CreatePostFragment", "Geocoding call đã bị hủy, bỏ qua onFailure");
                    return;
                }
                if (isAdded()) {
                    Toast.makeText(getContext(), "Lỗi lấy tọa độ: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e("ERROR", t.getMessage());
                }
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

    private void pickMultipleImages(int requestCode) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.setType("image/*");
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
            } else if (requestCode == REQUEST_CODE_IMAGES) {
                selectedImageFiles.clear();
                if (data.getClipData() != null) {
                    int count = data.getClipData().getItemCount();
                    for (int i = 0; i < count; i++) {
                        Uri imageUri = data.getClipData().getItemAt(i).getUri();
                        String path = getRealPathFromURI(imageUri);
                        if (path != null) {
                            selectedImageFiles.add(new File(path));
                        }
                    }
                } else if (data.getData() != null) {
                    Uri imageUri = data.getData();
                    String path = getRealPathFromURI(imageUri);
                    if (path != null) {
                        selectedImageFiles.add(new File(path));
                    }
                }
                imageAdapter.notifyDataSetChanged();
                if (selectedImageFiles.isEmpty()) {
                    Toast.makeText(getContext(), "Không thể tải hình ảnh", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "Đã chọn " + selectedImageFiles.size() + " hình ảnh", Toast.LENGTH_SHORT).show();
                }
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
        if (isLoading) {
            loadingContainer.setVisibility(View.VISIBLE);
            getView().setEnabled(false);
            // Tắt toàn bộ các mục trong BottomNavigationView
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).setAllMenuItemsEnabled(false);
            }
        } else {
            loadingContainer.setVisibility(View.GONE);
            getView().setEnabled(true);
            // Bật lại toàn bộ các mục trong BottomNavigationView
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).setAllMenuItemsEnabled(true);
            }
        }
    }

    private boolean validateInput(String title, String priceStr, String areaStr, String bedroomCountStr,
                                  String bathroomCountStr, String description, String specificAddress,
                                  boolean isNegotiablePrice) {
        boolean isValid = true;

        // Reset lỗi trước khi validate
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
        tvImagesError.setText("");
        tvImagesError.setVisibility(View.GONE);
        tvMapError.setText("");
        tvMapError.setVisibility(View.GONE);

        // 1. Validate tiêu đề (title)
        if (title.isEmpty()) {
            tilTitle.setError("Tiêu đề không được để trống");
            editTitle.requestFocus();
            isValid = false;
        } else if (title.length() > 500) {
            tilTitle.setError("Tiêu đề không được vượt quá 500 ký tự");
            editTitle.requestFocus();
            isValid = false;
        }

        // 2. Validate giá (price)
        if (!isNegotiablePrice) {
            if (priceStr.isEmpty()) {
                tilPrice.setError("Giá không được để trống");
                editPrice.requestFocus();
                isValid = false;
            } else {
                try {
                    double price = Double.parseDouble(priceStr);
                    if (price < 0) {
                        tilPrice.setError("Giá không được âm");
                        editPrice.requestFocus();
                        isValid = false;
                    } else if (price > 1_000_000_000_000.0) {
                        tilPrice.setError("Giá vượt quá giới hạn cho phép");
                        editPrice.requestFocus();
                        isValid = false;
                    }
                } catch (NumberFormatException e) {
                    tilPrice.setError("Giá phải là số hợp lệ");
                    editPrice.requestFocus();
                    isValid = false;
                }
            }
        }

        // 3. Validate diện tích (area)
        if (areaStr.isEmpty()) {
            tilArea.setError("Diện tích không được để trống");
            editArea.requestFocus();
            isValid = false;
        } else {
            try {
                int area = Integer.parseInt(areaStr);
                if (area <= 0) {
                    tilArea.setError("Diện tích phải lớn hơn 0");
                    editArea.requestFocus();
                    isValid = false;
                } else if (area > 10_000) {
                    tilArea.setError("Diện tích vượt quá giới hạn cho phép");
                    editArea.requestFocus();
                    isValid = false;
                }
            } catch (NumberFormatException e) {
                tilArea.setError("Diện tích phải là số nguyên hợp lệ");
                editArea.requestFocus();
                isValid = false;
            }
        }

        // 4. Validate số phòng ngủ (bedroomCount)
        if (bedroomCountStr.isEmpty()) {
            tilBedroomCount.setError("Số phòng ngủ không được để trống");
            editBedroomCount.requestFocus();
            isValid = false;
        } else {
            try {
                int bedroomCount = Integer.parseInt(bedroomCountStr);
                if (bedroomCount < 0) {
                    tilBedroomCount.setError("Số phòng ngủ không được âm");
                    editBedroomCount.requestFocus();
                    isValid = false;
                } else if (bedroomCount > 50) {
                    tilBedroomCount.setError("Số phòng ngủ vượt quá giới hạn");
                    editBedroomCount.requestFocus();
                    isValid = false;
                }
            } catch (NumberFormatException e) {
                tilBedroomCount.setError("Số phòng ngủ phải là số nguyên hợp lệ");
                editBedroomCount.requestFocus();
                isValid = false;
            }
        }

        // 5. Validate số phòng tắm (bathroomCount)
        if (bathroomCountStr.isEmpty()) {
            tilBathroomCount.setError("Số phòng tắm không được để trống");
            editBathroomCount.requestFocus();
            isValid = false;
        } else {
            try {
                int bathroomCount = Integer.parseInt(bathroomCountStr);
                if (bathroomCount < 0) {
                    tilBathroomCount.setError("Số phòng tắm không được âm");
                    editBathroomCount.requestFocus();
                    isValid = false;
                } else if (bathroomCount > 50) {
                    tilBathroomCount.setError("Số phòng tắm vượt quá giới hạn");
                    editBathroomCount.requestFocus();
                    isValid = false;
                }
            } catch (NumberFormatException e) {
                tilBathroomCount.setError("Số phòng tắm phải là số nguyên hợp lệ");
                editBathroomCount.requestFocus();
                isValid = false;
            }
        }

        // 6. Validate mô tả (description)
        if (description.length() > 1000) {
            tilDescription.setError("Mô tả không được vượt quá 1000 ký tự");
            editDescription.requestFocus();
            isValid = false;
        }

        // 7. Validate địa chỉ cụ thể (specificAddress)
        if (specificAddress.length() > 200) {
            tilSpecificAddress.setError("Địa chỉ không được vượt quá 200 ký tự");
            editSpecificAddress.requestFocus();
            isValid = false;
        }

        // 8. Validate ward (phường/xã)
        if (spinnerWard.getSelectedItemPosition() == 0) {
            tilWard.setError("Vui lòng chọn phường/xã");
            spinnerWard.requestFocus();
            isValid = false;
        }

        if (spinnerDistrict.getSelectedItemPosition() == 0) {
            tilDistrict.setError("Vui lòng chọn quận/huyện");
            spinnerDistrict.requestFocus();
            isValid = false;
        }

        if (spinnerProvince.getSelectedItemPosition() == 0) {
            tilProvince.setError("Vui lòng chọn tỉnh/thành phố");
            spinnerProvince.requestFocus();
            isValid = false;
        }

        // 9. Validate thumbnail
        if (thumbnailUri == null) {
            tvThumbnailError.setText("Vui lòng tải lên ảnh bìa");
            tvThumbnailError.setVisibility(View.VISIBLE);
            btnUploadThumbnail.requestFocus();
            isValid = false;
        }

        // 10. Validate danh sách ảnh (images)
        if (selectedImageFiles.isEmpty()) {
            tvImagesError.setText("Vui lòng tải lên ít nhất 1 ảnh");
            tvImagesError.setVisibility(View.VISIBLE);
            btnUploadImages.requestFocus();
            isValid = false;
        } else if (selectedImageFiles.size() > 10) {
            tvImagesError.setText("Chỉ được chọn tối đa 10 ảnh");
            tvImagesError.setVisibility(View.VISIBLE);
            btnUploadImages.requestFocus();
            isValid = false;
        }

        // 11. Validate vị trí trên bản đồ
        if (selectedLocation == null) {
            tvMapError.setText("Vui lòng chọn vị trí trên bản đồ");
            tvMapError.setVisibility(View.VISIBLE);
            mapView.requestFocus();
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
        // Chuyển đổi dữ liệu
        double price = isNegotiablePrice ? 0 : Double.parseDouble(priceStr);
        int area = Integer.parseInt(areaStr);
        int bedroomCount = Integer.parseInt(bedroomCountStr);
        int bathRoomCount = Integer.parseInt(bathroomCountStr);
        String wardCode = wards.get(spinnerWard.getSelectedItemPosition() - 1).getCode();
        String address = specificAddress ;
//                + ", " + wards.get(spinnerWard.getSelectedItemPosition() - 1).getFullName() + ", " +
//                districts.get(spinnerDistrict.getSelectedItemPosition() - 1).getFullName() + ", " +
//                provinces.get(spinnerProvince.getSelectedItemPosition() - 1).getFullName();

        // Chuẩn bị dữ liệu cho API
        RequestBody titleBody = RequestBody.create(MediaType.parse("text/plain"), title);
        RequestBody priceBody = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(price));
        RequestBody isNegotiatedPriceBody = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(isNegotiablePrice));
        RequestBody addressBody = RequestBody.create(MediaType.parse("text/plain"), address);
        RequestBody areaBody = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(area));
        RequestBody descriptionBody = RequestBody.create(MediaType.parse("text/plain"), description);
        RequestBody latitudeBody = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(selectedLocation.getLatitude()));
        RequestBody longitudeBody = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(selectedLocation.getLongitude()));
        RequestBody wardCodeBody = RequestBody.create(MediaType.parse("text/plain"), wardCode);
        RequestBody bedRoomCountBody = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(bedroomCount));
        RequestBody bathRoomCountBody = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(bathRoomCount));
        RequestBody isAiDescriptionBody = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(isAiDescription));

        // Chuẩn bị thumbnail
        File thumbnailFile = new File(getRealPathFromURI(thumbnailUri));
        RequestBody thumbnailRequestBody = RequestBody.create(MediaType.parse("image/*"), thumbnailFile);
        MultipartBody.Part thumbnailPart = MultipartBody.Part.createFormData("thumbnail", thumbnailFile.getName(), thumbnailRequestBody);

        // Chuẩn bị danh sách ảnh (images) với định dạng images[0].file, images[1].file, ...
        List<MultipartBody.Part> imageParts = new ArrayList<>();
        for (int i = 0; i < selectedImageFiles.size(); i++) {
            File imageFile = selectedImageFiles.get(i);
            RequestBody imageBody = RequestBody.create(MediaType.parse("image/*"), imageFile);
            // Đặt tên phần là "images[i].file"
            MultipartBody.Part imagePart = MultipartBody.Part.createFormData("images[" + i + "].file", imageFile.getName(), imageBody);
            imageParts.add(imagePart);
        }
        // Gửi yêu cầu API
        Call<PostDetailResponse> call = postService.createPost(
                titleBody, priceBody, isNegotiatedPriceBody, addressBody, areaBody, descriptionBody,
                latitudeBody, longitudeBody, wardCodeBody, bedRoomCountBody, bathRoomCountBody,
                imageParts, isAiDescriptionBody, thumbnailPart
        );

        call.enqueue(new Callback<PostDetailResponse>() {
            @Override
            public void onResponse(Call<PostDetailResponse> call, Response<PostDetailResponse> response) {
                showLoading(false);
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "Đăng bài thành công", Toast.LENGTH_SHORT).show();
                    Log.i("API_RESPONSE", response.body().getId());
                    String postId = response.body().getId();
                    PostDetailFragment postDetailFragment = PostDetailFragment.newInstance(postId);
                    getParentFragmentManager()
                            .beginTransaction()
                            .replace(R.id.frame_container, postDetailFragment)
                            .addToBackStack(null) // Thêm vào back stack để quay lại được
                            .commit();
                } else {
                    try {
                        String errorBody = response.errorBody() != null ? response.errorBody().string() : "Không có nội dung lỗi";
                        Log.e("API_RESPONSE", "Lỗi đăng bài: " + response.code() + " - " + errorBody);
                        // In toàn bộ lỗi cho người dùng qua Toast
                        Toast.makeText(getContext(), "Chi tiết lỗi: " + errorBody, Toast.LENGTH_LONG).show();
                    } catch (IOException e) {
                        Log.e("API_RESPONSE", "Không thể đọc errorBody: " + e.getMessage());
                        Toast.makeText(getContext(), "Không thể đọc chi tiết lỗi từ server", Toast.LENGTH_SHORT).show();
                    }
                    Toast.makeText(getContext(), "Lỗi đăng bài: " + response.code(), Toast.LENGTH_SHORT).show();

                }
            }

            @Override
            public void onFailure(Call<PostDetailResponse> call, Throwable t) {
                showLoading(false);
                Toast.makeText(getContext(), "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("ERROR", t.getMessage());
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

    public static class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ImageViewHolder> {
        private List<File> images;

        public ImageAdapter(List<File> images) {
            this.images = images;
        }

        @NonNull
        @Override
        public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_image_preview, parent, false);
            return new ImageViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
            File imageFile = images.get(position);
            Glide.with(holder.imageView.getContext())
                    .load(imageFile)
                    .centerCrop()
                    .into(holder.imageView);

            holder.btnRemove.setOnClickListener(v -> {
                images.remove(position);
                notifyItemRemoved(position);
                notifyItemRangeChanged(position, images.size());
            });
        }

        @Override
        public int getItemCount() {
            return images.size();
        }

        public static class ImageViewHolder extends RecyclerView.ViewHolder {
            ImageView imageView;
            MaterialButton btnRemove;

            public ImageViewHolder(@NonNull View itemView) {
                super(itemView);
                imageView = itemView.findViewById(R.id.img_preview);
                btnRemove = itemView.findViewById(R.id.btn_remove);
            }
        }
    }
}