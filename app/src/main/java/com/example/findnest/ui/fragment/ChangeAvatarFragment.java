package com.example.findnest.ui.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.findnest.R;
import com.example.findnest.api.IUserAPI;
import com.example.findnest.api.client.auth.AuthManager;
import com.example.findnest.api.client.retrofit.RetrofitClient;
import com.example.findnest.model.request.user.UserContactInfoReq;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChangeAvatarFragment extends Fragment {


    Button btn_upload_avatar, btn_save_change;
    ImageView iv_avatar_preview;
    private final String ARGS_IMAGE_URL = "imageUrl";
    private final int REQUEST_CODE_AVATAR = 1;


    private Uri avatarUri;

    private Uri originalUri;

    AuthManager authManager;
    IUserAPI userAPI;
    Context context;

    void Init(View view) {
        context = requireContext();
        authManager = new AuthManager(context);
        userAPI = RetrofitClient.getClient(authManager).create(IUserAPI.class);

        btn_upload_avatar = view.findViewById(R.id.btn_upload_avatar);
        btn_save_change = view.findViewById(R.id.btn_save_change);
        iv_avatar_preview = view.findViewById(R.id.iv_avatar_preview);

        Bundle args = getArguments();
        if (args != null) {
            avatarUri = Uri.parse(args.getString(ARGS_IMAGE_URL));
            originalUri = Uri.parse(args.getString(ARGS_IMAGE_URL));

            iv_avatar_preview.setVisibility(View.VISIBLE);

            Glide.with(this).load(avatarUri)
                    .placeholder(R.drawable.icon_avatar)
                    .error(R.drawable.icon_avatar)
                    .into(iv_avatar_preview);
        }


    }

    void InitEvents() {
        btn_upload_avatar.setOnClickListener(v -> {
            pickAvatar();
        });

        btn_save_change.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!avatarUri.equals(originalUri)) { //if new image -> send request

                    new AlertDialog.Builder(context)
                            .setTitle("Xác nhận")
                            .setMessage("Bạn có chắc chắn muốn thay đổi avatar ?")
                            .setPositiveButton("Xác nhận", (dialog, which) -> {

                                try {
                                    File file = getFileFromUri(avatarUri);
                                    updateAvatar(file);
                                } catch (IOException e) {
                                    Toast.makeText(getContext(), "Lỗi tải file!", Toast.LENGTH_SHORT).show();
                                }

                            }).setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss()).show();

                } else {
                    Toast.makeText(context, "Cập nhật thành công", Toast.LENGTH_SHORT).show();

                    FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
                    transaction.replace(R.id.frame_container, new AccountFragment());
                    transaction.addToBackStack(null); // Allows back navigation
                    transaction.commit();
                }
            }
        });
    }


    void updateAvatar(File avatarFile) {

        RequestBody reqestFile = RequestBody.create(MediaType.parse("image/"), avatarFile);
        MultipartBody.Part body = MultipartBody.Part.createFormData("avatarFile", avatarFile.getName(), reqestFile);

        userAPI.changeAvatar(body).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(context, "Cập nhật thành công", Toast.LENGTH_SHORT).show();

                    FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
                    transaction.replace(R.id.frame_container, new AccountFragment());
                    transaction.addToBackStack(null); // Allows back navigation
                    transaction.commit();
                } else {
                    Toast.makeText(context, "Lỗi " + response.code() + ": '" + response.message() + "' vui lòng thử lại sau", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(context, "Lỗi " + t.getMessage(), Toast.LENGTH_SHORT).show();
                new AlertDialog.Builder(context)
                        .setTitle("Xác nhận")
                        .setMessage(t.getMessage())
                        .setPositiveButton("Xác nhận", (dialog, which) -> {
                        }).setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss()).show();
            }
        });
    }

    //open pick image with intent
    private void pickAvatar() {
        Intent it = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(it, REQUEST_CODE_AVATAR);
    }

    //get image data from intent
    @Override
    public void onActivityResult(int requestCode, int resultCode,
                                 @Nullable
                                 Intent data) {

        if (requestCode == REQUEST_CODE_AVATAR && data != null && resultCode == Activity.RESULT_OK) {
            avatarUri = data.getData();
            iv_avatar_preview.setVisibility(View.VISIBLE);

            Glide.with(this).load(avatarUri)
                    .placeholder(R.drawable.icon_avatar)
                    .error(R.drawable.icon_avatar)
                    .into(iv_avatar_preview);
        }
    }

    private File getFileFromUri(Uri uri) throws IOException {
        File file = new File(context.getCacheDir(), "avatar.jpg");
        try (InputStream inputStream = context.getContentResolver().openInputStream(uri);
             OutputStream outputStream = new FileOutputStream(file)) {

            byte[] buffer = new byte[4 * 1024];
            int read;
            while ((read = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, read);
            }
            outputStream.flush();
        }
        return file;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_change_avatar, container, false);

        Init(view);
        InitEvents();
        return view;
    }
}