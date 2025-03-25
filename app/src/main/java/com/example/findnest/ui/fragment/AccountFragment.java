package com.example.findnest.ui.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.findnest.R;
import com.example.findnest.api.IUserAPI;
import com.example.findnest.api.client.auth.AuthManager;
import com.example.findnest.api.client.retrofit.RetrofitClient;
import com.example.findnest.model.response.authentication.TokenModel;
import com.example.findnest.model.response.user_for_public.UserForPublicDetailRes;
import com.example.findnest.ui.activity.LoginActivity;

import org.w3c.dom.Text;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AccountFragment extends Fragment {
    AuthManager authManager;
    Context context;
    LinearLayout btn_go_to_profile, btn_go_to_change_password, btn_go_to_change_avatar, btn_go_to_my_post;
    Button btnLogout;
    IUserAPI userAPI;
    TextView user_fullName, user_contactNumber;
    ImageView user_avatar;
    String imageUrl;

    private final String ARGS_USER_ID = "id";
    private final String ARGS_USER_FULLNAME = "fullName";
    private final String ARGS_USER_CONTACTPHONENUMBER = "contactPhoneNumber";
    private final String ARGS_USER_ZALO = "zalo";
    private final String ARGS_IMAGE_URL = "imageUrl";
    public final String BASE_UPLOAD_URL = "https://thanhkhac.id.vn";

    LinearLayout historyPayment;
    UserForPublicDetailRes user = new UserForPublicDetailRes();

    void Init(View view) {
        context = requireContext();
        authManager = new AuthManager(context);
        btn_go_to_profile = view.findViewById(R.id.btn_go_to_profile);
        btn_go_to_change_password = view.findViewById(R.id.btn_go_to_change_password);
        btn_go_to_change_avatar = view.findViewById(R.id.btn_go_to_change_avatar);
        btn_go_to_my_post = view.findViewById(R.id.btn_go_to_my_post);
        btnLogout = view.findViewById(R.id.logout_button);

        user_avatar = view.findViewById(R.id.user_avatar);

        user_fullName = view.findViewById(R.id.user_fullName);
        user_contactNumber = view.findViewById(R.id.user_contactNumber);
    }

    void InitEvent() {

        btn_go_to_change_password.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
                transaction.replace(R.id.frame_container, new ChangePasswordFragment());
                transaction.addToBackStack(null); // Allows back navigation
                transaction.commit();
            }
        });

        btn_go_to_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ProfileFragment profileFragment = new ProfileFragment();

                //init bundle to pass data
                Bundle bundle = new Bundle();
                bundle.putString(ARGS_USER_ID, user.getId());
                bundle.putString(ARGS_USER_FULLNAME, user.getFullName() == null ? "" : user.getFullName());
                bundle.putString(ARGS_USER_CONTACTPHONENUMBER, user.getContactPhoneNumber() == null ? "" : user.getContactPhoneNumber());
                bundle.putString(ARGS_USER_ZALO, user.getZalo() == null ? "" : user.getZalo());
                bundle.putString(ARGS_IMAGE_URL, imageUrl);

                //set arguments
                profileFragment.setArguments(bundle);

                //to fragment
                FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
                transaction.replace(R.id.frame_container, profileFragment);
                transaction.addToBackStack(null); // Allows back navigation
                transaction.commit();
            }
        });

        btn_go_to_change_avatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ChangeAvatarFragment changeAvatarFragment = new ChangeAvatarFragment();

                //init bundle to pass data
                Bundle bundle = new Bundle();
                bundle.putString(ARGS_IMAGE_URL, imageUrl);

                //set arguments
                changeAvatarFragment.setArguments(bundle);

                //to fragment
                FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
                transaction.replace(R.id.frame_container, changeAvatarFragment);
                transaction.addToBackStack(null); // Allows back navigation
                transaction.commit();
            }
        });

        btn_go_to_my_post.setOnClickListener(v->{
            FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
            transaction.replace(R.id.frame_container, new MyPostFragment());
            transaction.addToBackStack(null); // Allows back navigation
            transaction.commit();
        });

        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                authManager.clearTokens();
                Intent it = new Intent(context, LoginActivity.class);
                it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(it);
            }
        });
    }

    void getInfo() {
        try {
            userAPI = RetrofitClient.getClient(authManager).create(IUserAPI.class);
            userAPI.getUser().enqueue(new Callback<UserForPublicDetailRes>() {
                @Override
                public void onResponse(Call<UserForPublicDetailRes> call, Response<UserForPublicDetailRes> response) {
                    if (response.isSuccessful()) {
                        user = response.body();
                        user_fullName.setText(response.body().getFullName() == null ? "Họ tên: Chưa cập nhật" : response.body().getFullName());
                        user_contactNumber.setText(response.body().getContactPhoneNumber() == null ? "SĐT: chưa cập nhật" : response.body().getContactPhoneNumber());

                        imageUrl = BASE_UPLOAD_URL + user.getAvatar();
                        Glide.with(AccountFragment.this)
                                .load(imageUrl)
                                .placeholder(R.drawable.icon_avatar)
                                .error(R.drawable.icon_avatar)
                                .into(user_avatar);
                    }
                }

                @Override
                public void onFailure(Call<UserForPublicDetailRes> call, Throwable t) {

                }
            });

        } catch (Exception e) {
            Toast.makeText(context, " ERROR: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_account, container, false);
        historyPayment = view.findViewById(R.id.historyPayment);
        historyPayment.setOnClickListener( h -> {
            Toast.makeText(getContext(), "Bạn đã nhấn vào payment", Toast.LENGTH_SHORT).show();
            getActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .addToBackStack(null)
                    .replace(AccountFragment.this.getId(), new PaymentFragment())
                    .commit();
        });

        return view;
    }
}