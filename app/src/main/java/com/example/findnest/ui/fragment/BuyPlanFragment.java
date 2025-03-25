package com.example.findnest.ui.fragment;

import android.app.AlertDialog;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.findnest.R;
import com.example.findnest.adapter.PlanAdapter;
import com.example.findnest.api.IPlanAPI;
import com.example.findnest.api.client.auth.AuthManager;
import com.example.findnest.api.client.retrofit.RetrofitClient;
import com.example.findnest.model.request.plan.BuyPlanReq;
import com.example.findnest.model.request.user.UserContactInfoReq;
import com.example.findnest.model.response.plan.PlanDetailRes;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class BuyPlanFragment extends Fragment implements PlanAdapter.OnPlanButtonClickedListener {

    private final String ARGS_USER_ID = "id";
    private final String ARGS_USER_FULLNAME = "fullName";
    private final String ARGS_USER_IMAGE_URL = "imageUrl";
    private final String ARGS_USER_BALANCE = "balance";

    private final String ARGS_POST_ID = "id";
    private final String ARGS_POST_TITLE = "title";
    private final String ARGS_POST_THUMBNAIL_URL = "thumbnailURL";
    private final String ARGS_POST_PRICE = "price";
    private final String ARGS_POST_AREA = "area";
    private final String ARGS_POST_ADDRESS = "regionAddress";

    private String user_id;
    private String user_fullName;
    private String user_imageUrl;
    private String user_balance;

    private String post_id;
    private String post_title;
    private String post_price;
    private String post_thumbnailUrl;
    private String post_area;
    private String post_address;

    ImageView iv_thumbnail, user_avatar;
    TextView tv_user_fullName, tv_user_balance, tv_post_title, tv_cost_value, tv_area, tv_address_value;

    RecyclerView rv_plan;
    private AuthManager authManager;
    private IPlanAPI planAPI;
    private List<PlanDetailRes> list_plan;
    private PlanAdapter planAdapter;

    void Init(View view) {
        iv_thumbnail = view.findViewById(R.id.iv_thumbnail);
        user_avatar = view.findViewById(R.id.user_avatar);
        tv_user_fullName = view.findViewById(R.id.tv_user_fullName);
        tv_user_balance = view.findViewById(R.id.tv_user_balance);

        tv_post_title = view.findViewById(R.id.tv_post_title);
        tv_cost_value = view.findViewById(R.id.tv_cost_value);
        tv_area = view.findViewById(R.id.tv_area);
        tv_address_value = view.findViewById(R.id.tv_address_value);
        rv_plan = view.findViewById(R.id.rv_plan);
        // retrieve arguments
        Bundle args = getArguments();
        if (args != null) {
            //get user data
            user_id = args.getString(ARGS_USER_ID);
            user_fullName = args.getString(ARGS_USER_FULLNAME);
            user_imageUrl = args.getString(ARGS_USER_IMAGE_URL);
            user_balance = args.getString(ARGS_USER_BALANCE);

            //get post data
            post_id = args.getString(ARGS_POST_ID);
            post_title = args.getString(ARGS_POST_TITLE);
            post_price = args.getString(ARGS_POST_PRICE);
            post_thumbnailUrl = args.getString(ARGS_POST_THUMBNAIL_URL);
            post_area = args.getString(ARGS_POST_AREA);
            post_address = args.getString(ARGS_POST_ADDRESS);

            //binding into components
            tv_user_fullName.setText(user_fullName);
            tv_user_balance.setText("Số dư: " + user_balance + " VND");

            Glide.with(this)
                    .load(user_imageUrl)
                    .placeholder(R.drawable.icon_avatar)
                    .error(R.drawable.icon_avatar)
                    .into(user_avatar);


            tv_post_title.setText(post_title);
            tv_address_value.setText(post_address);
            tv_cost_value.setText(String.format(Locale.getDefault(), "%s", post_price) + " VND");
            tv_area.setText(String.valueOf(post_area) + " m²");

            Glide.with(this)
                    .load(post_thumbnailUrl)
                    .placeholder(R.drawable.icon_avatar)
                    .error(R.drawable.icon_avatar)
                    .into(iv_thumbnail);
        }

        authManager = new AuthManager(requireContext());
        planAPI = RetrofitClient.getClient(authManager).create(IPlanAPI.class);
        list_plan = new ArrayList<>();
        planAdapter = new PlanAdapter(requireContext(), list_plan, this);

        LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext());
        rv_plan.setLayoutManager(layoutManager);
        rv_plan.setAdapter(planAdapter);
    }

    void getPlans() {
        planAPI.getPlan().enqueue(new Callback<List<PlanDetailRes>>() {
            @Override
            public void onResponse(Call<List<PlanDetailRes>> call, Response<List<PlanDetailRes>> response) {
                if (response.isSuccessful() || response.body() != null) {
                    List<PlanDetailRes> plans = response.body();
                    list_plan.addAll(plans);
                    planAdapter.notifyItemRangeInserted(list_plan.size(), plans.size());
                }
            }

            @Override
            public void onFailure(Call<List<PlanDetailRes>> call, Throwable t) {
                Toast.makeText(requireContext(), "Lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_buy_plan, container, false);
        Init(view);
        getPlans();
        return view;
    }

    @Override
    public void onPlanSelected(PlanDetailRes plan) {
        try {
            BigDecimal balance = new BigDecimal(user_balance);
            if (balance.compareTo(plan.getPrice()) < 0) {
                Toast.makeText(requireContext(), "Số dư không đủ để thực hiện hành động", Toast.LENGTH_SHORT).show();
                return;
            }
            new AlertDialog.Builder(requireContext())
                    .setTitle("Xác nhận")
                    .setMessage("Mua gói tin VIP" + plan.getPriorityLevel() +
                            "\n Hiệu lực: " + plan.getDuration() + " ngày" +
                            "\n Giá: " + plan.getPrice() + " VND" +
                            "\n Áp dụng cho bài đăng này ?")
                    .setPositiveButton("Xác nhận", (dialog, which) -> {

                        BuyPlanReq model = new BuyPlanReq();
                        model.setPlanId(plan.getId());
                        model.setPromotionCode("");
                        BigDecimal newBalance = balance.subtract(plan.getPrice());

                        BuyPlan(post_id, model, newBalance);

                    }).setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss()).show();
        } catch (Exception e) {
            Log.d("BUY_PLAN: ", e.getMessage());
        }
    }

    void BuyPlan(String postId, BuyPlanReq request, BigDecimal newBalance) {
        planAPI.buyPlan(postId, request).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(requireContext(), "Thao tác thành công.", Toast.LENGTH_SHORT).show();

                    Bundle bundle = new Bundle();
                    bundle.putString(ARGS_USER_ID, user_id);
                    bundle.putString(ARGS_USER_FULLNAME, user_fullName == null ? "" : user_fullName);
                    bundle.putString(ARGS_USER_IMAGE_URL, user_imageUrl);
                    bundle.putString(ARGS_USER_BALANCE, newBalance.toString());

                    MyPostFragment myPostFragment = new MyPostFragment();
                    myPostFragment.setArguments(bundle);

                    FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
                    transaction.replace(R.id.frame_container, myPostFragment);
                    transaction.commit();

                } else {
                    Toast.makeText(requireContext(), "Thao tác thất bại. " + response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.d("BUY_PLAN: ", t.getMessage());
                Toast.makeText(requireContext(), "Lỗi " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}