package com.example.findnest.ui;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.*;
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
import com.example.findnest.api.IRegionService;
import com.example.findnest.api.RetrofitClient;
import com.example.findnest.auth.AuthManager;
import com.example.findnest.model.responsedtos.GeocodingResponse;
import com.example.findnest.model.responsedtos.RegionResponse;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import org.osmdroid.config.Configuration;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Marker;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class CreatePostFragment extends Fragment {

    private static final int REQUEST_CODE_THUMBNAIL = 1;
    private static final int REQUEST_CODE_IMAGES = 2;
    private static final int REQUEST_PERMISSIONS = 3;

    // Views
    private TextInputEditText editTitle, editPrice, editArea, editBedroomCount, editBathroomCount;
    private Spinner spinnerProvince, spinnerDistrict, spinnerWard;
    private MaterialButton btnSelectLocation, btnUploadThumbnail, btnUploadImages, btnSubmit;
    private ImageView imgThumbnailPreview;
    private RecyclerView recyclerImages;
    private MapView mapView;
    private Marker selectedMarker; // Marker hiển thị vị trí người dùng chọn

    private GeocodingService geocodingService;

    // Data
    private Uri thumbnailUri;
    private List<File> selectedImageFiles = new ArrayList<>();
    private ImageAdapter imageAdapter;
    private IRegionService regionService;
    private List<RegionResponse> provinces = new ArrayList<>();
    private List<RegionResponse> districts = new ArrayList<>();
    private List<RegionResponse> wards = new ArrayList<>();
    private AuthManager authManager;

    private GeoPoint selectedLocation; // Lưu trữ tọa độ được chọn

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

        // Khởi tạo GeocodingService cho Nominatim
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://nominatim.openstreetmap.org/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        geocodingService = retrofit.create(GeocodingService.class);

        // Cấu hình osmdroid
        Configuration.getInstance().load(getContext(), getActivity().getPreferences(0));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_create_post, container, false);

        // Ánh xạ views
        editTitle = view.findViewById(R.id.edit_title);
        editPrice = view.findViewById(R.id.edit_price);
        editArea = view.findViewById(R.id.edit_area);
        editBedroomCount = view.findViewById(R.id.edit_bedroom_count);
        editBathroomCount = view.findViewById(R.id.edit_bathroom_count);
        spinnerProvince = view.findViewById(R.id.spinner_province);
        spinnerDistrict = view.findViewById(R.id.spinner_district);
        spinnerWard = view.findViewById(R.id.spinner_ward);
        btnSelectLocation = view.findViewById(R.id.btn_select_location);
        btnUploadThumbnail = view.findViewById(R.id.btn_upload_thumbnail);
        btnUploadImages = view.findViewById(R.id.btn_upload_images);
        btnSubmit = view.findViewById(R.id.btn_submit);
        imgThumbnailPreview = view.findViewById(R.id.img_thumbnail_preview);
        recyclerImages = view.findViewById(R.id.recycler_images);
        mapView = view.findViewById(R.id.map_view);

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
        btnSelectLocation.setOnClickListener(v -> selectLocation());
        btnSubmit.setOnClickListener(v -> submitPost());

        mapView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                v.getParent().requestDisallowInterceptTouchEvent(true); // Ngăn ScrollView nhận sự kiện cuộn
                return false;
            }
        });


        // Kiểm tra quyền
        checkPermissions();

        return view;
    }

    private void setupMap() {
        mapView.setTileSource(TileSourceFactory.MAPNIK);
//        mapView.setBuiltInZoomControls(true); // Giữ nút zoom
        mapView.setMultiTouchControls(true);  // Cho phép pinch-to-zoom
        mapView.getController().setZoom(10.0);

        MapEventsOverlay mapEventsOverlay = new MapEventsOverlay(new MapEventsReceiver() {
            @Override
            public boolean singleTapConfirmedHelper(GeoPoint p) {
                // Khi người dùng nhấn vào bản đồ
                addMarkerAtLocation(p);
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
        // Xóa marker cũ nếu đã tồn tại
        if (selectedMarker != null) {
            mapView.getOverlays().remove(selectedMarker);
        }

        // Tạo marker mới
        selectedMarker = new Marker(mapView);
        selectedMarker.setPosition(location);
        selectedMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        selectedMarker.setTitle("Vị trí đã chọn");
        selectedMarker.setSnippet("Lat: " + location.getLatitude() + ", Lon: " + location.getLongitude());
        selectedMarker.setInfoWindow(new org.osmdroid.views.overlay.infowindow.BasicInfoWindow(org.osmdroid.library.R.layout.bonuspack_bubble, mapView));

        // Lưu tọa độ được chọn
        selectedLocation = location;

        // Thêm marker vào bản đồ
        mapView.getOverlays().add(selectedMarker);
        mapView.invalidate(); // Cập nhật lại bản đồ

        // Thông báo cho người dùng
        Toast.makeText(getContext(), "Đã chọn: Lat " + location.getLatitude() + ", Lon " + location.getLongitude(), Toast.LENGTH_SHORT).show();
    }

    private void checkPermissions() {
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            }, REQUEST_PERMISSIONS);
        }
    }

    private void setupSpinners() {
        loadProvinces();

        spinnerProvince.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                RegionResponse selectedProvince = provinces.get(position);
                loadDistricts(selectedProvince.getCode());
                updateMap(selectedProvince.getFullName() + ", Vietnam");
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
                if (!districts.isEmpty()) {
                    RegionResponse selectedDistrict = districts.get(position);
                    loadWards(selectedDistrict.getCode());
                    updateMap(selectedDistrict.getFullName() + ", " + provinces.get(spinnerProvince.getSelectedItemPosition()).getFullName() + ", Vietnam");
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
                if (!wards.isEmpty()) {
                    RegionResponse selectedWard = wards.get(position);
                    updateMap(selectedWard.getFullName() + ", " + districts.get(spinnerDistrict.getSelectedItemPosition()).getFullName() + ", " +
                            provinces.get(spinnerProvince.getSelectedItemPosition()).getFullName() + ", Vietnam");
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
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item,
                provinces.stream().map(RegionResponse::getFullName).toArray(String[]::new));
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerProvince.setAdapter(adapter);
    }

    private void updateDistrictSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item,
                districts.stream().map(RegionResponse::getFullName).toArray(String[]::new));
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDistrict.setAdapter(adapter);
    }

    private void updateWardSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item,
                wards.stream().map(RegionResponse::getFullName).toArray(String[]::new));
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerWard.setAdapter(adapter);
    }

    private void updateMap(String locationQuery) {
        geocodingService.getCoordinates(locationQuery, "json", 1).enqueue(new Callback<List<GeocodingResponse>>() {
            @Override
            public void onResponse(Call<List<GeocodingResponse>> call, Response<List<GeocodingResponse>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    GeoPoint location = response.body().get(0).toGeoPoint();
                    mapView.getController().setCenter(location);
                    mapView.getController().setZoom(getZoomLevel(locationQuery));
                } else {
                    Toast.makeText(getContext(), "Không tìm thấy tọa độ cho khu vực này", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<GeocodingResponse>> call, Throwable t) {
                Toast.makeText(getContext(), "Lỗi lấy tọa độ: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("ERROR", t.getMessage());
            }
        });
    }

    private double getZoomLevel(String locationQuery) {
        if (locationQuery.contains("Ward")) return 20.0; // Xã/phường
        if (locationQuery.contains("District")) return 18.0; // Quận/huyện
        return 16.0; // Tỉnh/thành phố
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

    private void selectLocation() {
        Toast.makeText(getContext(), "Mở bản đồ để chọn tọa độ", Toast.LENGTH_SHORT).show();
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
                selectedImageFiles.clear(); // Xóa danh sách cũ để tránh trùng lặp
                if (data.getClipData() != null) {
                    // Trường hợp chọn nhiều hình ảnh
                    int count = data.getClipData().getItemCount();
                    for (int i = 0; i < count; i++) {
                        Uri imageUri = data.getClipData().getItemAt(i).getUri();
                        String path = getRealPathFromURI(imageUri);
                        if (path != null) {
                            selectedImageFiles.add(new File(path));
                        }
                    }
                } else if (data.getData() != null) {
                    // Trường hợp chọn một hình ảnh
                    Uri imageUri = data.getData();
                    String path = getRealPathFromURI(imageUri);
                    if (path != null) {
                        selectedImageFiles.add(new File(path));
                    }
                }
                // Cập nhật adapter sau khi thêm tất cả hình ảnh
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

    private void submitPost() {
        String title = editTitle.getText().toString().trim();
        String price = editPrice.getText().toString().trim();
        String area = editArea.getText().toString().trim();
        String bedroomCount = editBedroomCount.getText().toString().trim();
        String bathroomCount = editBathroomCount.getText().toString().trim();

        if (title.isEmpty() || price.isEmpty() || area.isEmpty() || bedroomCount.isEmpty() || bathroomCount.isEmpty()) {
            Toast.makeText(getContext(), "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(getContext(), "Dữ liệu hợp lệ, sẵn sàng gửi API", Toast.LENGTH_SHORT).show();
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

    // Adapter cho RecyclerView

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
                    .centerCrop() // Cắt ảnh để vừa khung
//                    .placeholder(R.drawable.placeholder) // Ảnh tạm khi đang load
//                    .error(R.drawable.error_image) // Ảnh lỗi nếu không load được
                    .into(holder.imageView);

            holder.btnRemove.setOnClickListener(v -> {
                images.remove(position); // Xóa ảnh khỏi danh sách
                notifyItemRemoved(position); // Cập nhật RecyclerView
                notifyItemRangeChanged(position, images.size()); // Cập nhật lại index
            });
        }


//        private Bitmap getScaledBitmap(Uri uri) {
//            try {
//                InputStream inputStream = getContext().getContentResolver().openInputStream(uri);
//                BitmapFactory.Options options = new BitmapFactory.Options();
//                options.inSampleSize = 4; // Giảm kích thước ảnh xuống 1/4
//                return BitmapFactory.decodeStream(inputStream, null, options);
//            } catch (Exception e) {
//                e.printStackTrace();
//                return null;
//            }
//        }

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