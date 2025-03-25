package com.example.findnest.ui.fragment;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.findnest.R;
import com.example.findnest.api.IUserAPI;
import com.example.findnest.api.client.auth.AuthManager;
import com.example.findnest.api.client.retrofit.RetrofitClient;
import com.example.findnest.model.request.user.UserContactInfoReq;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class ProfileFragment extends Fragment {

    EditText edtZalo, edtContactPhoneNumber, edtFullName;
    Button btnSaveChange, btn_back_to_account;
    AuthManager authManager;
    IUserAPI userAPI;

    ImageView ivAvatar;

    Context context;

    private final String ARGS_USER_ID = "id";
    private final String ARGS_USER_FULLNAME = "fullName";
    private final String ARGS_USER_CONTACTPHONENUMBER = "contactPhoneNumber";
    private final String ARGS_USER_ZALO = "zalo";
    private final String ARGS_IMAGE_URL = "imageUrl";
    private final String VALID_CONTACT_NUMBER_REGEX = "^(?:\\+84|0)(3|5|7|8|9)[0-9]{8}$";
    private String imageUrl;
    String id;
    void Init(View view) {
        edtZalo = view.findViewById(R.id.edtZalo);
        edtContactPhoneNumber = view.findViewById(R.id.edtContactPhoneNumber);
        edtFullName = view.findViewById(R.id.edtFullName);
        ivAvatar = view.findViewById(R.id.ivAvatar);
        context = requireContext();

        btnSaveChange = view.findViewById(R.id.btnSaveChange);
        btn_back_to_account = view.findViewById(R.id.btn_back_to_account);

        // retrieve arguments
        Bundle args = getArguments();
        if (args != null) {
            id = args.getString(ARGS_USER_ID);
            String fullName = args.getString(ARGS_USER_FULLNAME);
            String contactPhoneNumber = args.getString(ARGS_USER_CONTACTPHONENUMBER);
            String zalo = args.getString(ARGS_USER_ZALO);
            imageUrl = args.getString(ARGS_IMAGE_URL);

            edtFullName.setText(fullName);
            edtContactPhoneNumber.setText(contactPhoneNumber);
            edtZalo.setText(zalo);

            Glide.with(this).load(imageUrl).placeholder(R.drawable.icon_avatar).error(R.drawable.icon_avatar).into(ivAvatar);
        }
    }

    void InitEvents() {
        btn_back_to_account.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
                transaction.replace(R.id.frame_container, new AccountFragment());
                transaction.commit();
            }
        });

        btnSaveChange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String fullName = edtFullName.getText().toString().trim();
                String contactPhoneNumber = edtContactPhoneNumber.getText().toString().trim();
                String zalo = edtZalo.getText().toString().trim();

                if (fullName.isEmpty() || contactPhoneNumber.isEmpty()) {
                    Toast.makeText(context, "Họ tên và số điện thoại không được bỏ trống", Toast.LENGTH_SHORT).show();
                } else if (!contactPhoneNumber.matches(VALID_CONTACT_NUMBER_REGEX)) {
                    Toast.makeText(context, "Số điện thoại không đúng định dạng", Toast.LENGTH_SHORT).show();
                } else if (!zalo.isEmpty() && !zalo.matches(VALID_CONTACT_NUMBER_REGEX)) {
                    Toast.makeText(context, "Số zalo không đúng định dạng", Toast.LENGTH_SHORT).show();
                } else {
                    new AlertDialog.Builder(context)
                            .setTitle("Xác nhận")
                            .setMessage("Bạn có chắc chắn muốn thay đổi thông tin ?")
                            .setPositiveButton("Xác nhận", (dialog, which) -> {

                                UserContactInfoReq model = new UserContactInfoReq();
                                model.setFullName(fullName);
                                model.setContactPhoneNumber(contactPhoneNumber);
                                model.setZalo(zalo);

                                updateUserContactInfo(model);

                            }).setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss()).show();
                }
            }
        });
    }

    void updateUserContactInfo(UserContactInfoReq request) {

        authManager = new AuthManager(context);
        userAPI = RetrofitClient.getClient(authManager).create(IUserAPI.class);

        userAPI.updateContactInfo(request).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(context, "Cập nhật thông tin thành công", Toast.LENGTH_SHORT).show();

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
                Toast.makeText(context, "Lỗi kết nối server vui lòng thử lại sau", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        Init(view);
        InitEvents();

        return view;
    }
}