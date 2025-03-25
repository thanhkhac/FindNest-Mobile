package com.example.findnest.ui.fragment;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.findnest.R;
import com.example.findnest.api.IUserAPI;
import com.example.findnest.api.client.auth.AuthManager;
import com.example.findnest.api.client.retrofit.RetrofitClient;
import com.example.findnest.model.request.user.ChangePasswordReq;
import com.example.findnest.model.request.user.UserContactInfoReq;
import com.example.findnest.ui.activity.LoginActivity;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChangePasswordFragment extends Fragment {


    EditText edtCurrentPassword, edtNewPassword, edtNewPasswordConfirmation;
    Button btnSaveChange, btn_back_to_account;
    AuthManager authManager;
    IUserAPI userAPI;
    Context context;

    void Init(View view) {
        edtCurrentPassword = view.findViewById(R.id.edtCurentPassword);
        edtNewPassword = view.findViewById(R.id.edtNewPassword);
        edtNewPasswordConfirmation = view.findViewById(R.id.edtNewPasswordConfirmation);

        btnSaveChange = view.findViewById(R.id.btnSaveChange);
        btn_back_to_account = view.findViewById(R.id.btn_back_to_account);

        context = requireContext();
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
                String currentPassword = edtCurrentPassword.getText().toString().trim();
                String password = edtNewPassword.getText().toString().trim();
                String confirmPassword = edtNewPasswordConfirmation.getText().toString().trim();

                // Kiểm tra dữ liệu
                if (currentPassword.isEmpty()
                        || password.isEmpty()
                        || confirmPassword.isEmpty()) {
                    Toast.makeText(context, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                } else if (!password.equals(confirmPassword)) {
                    Toast.makeText(context, "Mật khẩu xác nhận không khớp", Toast.LENGTH_SHORT).show();
                } else if (password.length() < 8) {
                    Toast.makeText(context, "Mật khẩu cần có ít nhất 8 kí tự", Toast.LENGTH_SHORT).show();
                } else {
                    new AlertDialog.Builder(context)
                            .setTitle("Xác nhận")
                            .setMessage("Bạn có chắc chắn muốn thay đổi mật khẩu ?")
                            .setPositiveButton("Xác nhận", (dialog, which) -> {

                                ChangePasswordReq model = new ChangePasswordReq();
                                model.setOldPassword(currentPassword);
                                model.setNewPassword(password);

                                changePassword(model);

                            }).setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss()).show();
                }
            }
        });
    }

    void changePassword(ChangePasswordReq request) {
        authManager = new AuthManager(context);
        userAPI = RetrofitClient.getClient(authManager).create(IUserAPI.class);

        userAPI.changePassword(request).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(context, "Đổi mật khẩu thành công", Toast.LENGTH_SHORT).show();

                    authManager.clearTokens();
                    Intent it = new Intent(context, LoginActivity.class);
                    it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(it);
                } else {
                    Toast.makeText(context, "Lỗi: Vui lòng kiểm tra lại mật khẩu hiện tại" , Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(context, "Lỗi kết nối server vui lòng thử lại sau", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_change_password, container, false);

        Init(view);
        InitEvents();

        return view;
    }
}