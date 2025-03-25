package com.example.findnest.adapter;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.findnest.R;
import com.example.findnest.api.IUserAPI;
import com.example.findnest.api.client.retrofit.RetrofitClient;
import com.example.findnest.model.Post;
import com.example.findnest.model.response.user_for_public.UserForPublicDetailRes;
import com.example.findnest.ui.customEvents.OnMyPostButtonClickedListener;
import com.example.findnest.ui.fragment.AccountFragment;

import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyListPostAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_ITEM = 0;
    private static final int TYPE_LOADING = 1;
    private Context _context;
    private List<Post> list_post;
    private boolean isLoadingAdded = false;
    private OnMyPostButtonClickedListener listener;

    private final String BUTTON_TYPE_EDIT = "edit";
    private final String BUTTON_TYPE_BUY_PLAN = "buyPlan";
    private final String BUTTON_TYPE_DELETE = "delete";

    public MyListPostAdapter(Context context, List<Post> postList, OnMyPostButtonClickedListener listener) {
        this._context = context;
        this.list_post = postList;
        this.listener = listener;
    }

    // ViewHolder cho item bài đăng
    public static class MyListPostViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvPriority, tvAddress, tvCost, tvArea;
        ImageView iv_thumbnail;
        CardView parentLayout;

        Button btn_edit, btn_buy_plan, btn_delete;

        public MyListPostViewHolder(
                @NonNull
                View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_post_title);
            tvPriority = itemView.findViewById(R.id.tv_post_priority);
            tvAddress = itemView.findViewById(R.id.tv_address_value);
            tvCost = itemView.findViewById(R.id.tv_cost_value);
            tvArea = itemView.findViewById(R.id.tv_area);
            iv_thumbnail = itemView.findViewById(R.id.iv_thumbnail);
            parentLayout = itemView.findViewById(R.id.parentLayout);

            btn_edit = itemView.findViewById(R.id.btn_edit);
            btn_buy_plan = itemView.findViewById(R.id.btn_buy_plan);
            btn_delete = itemView.findViewById(R.id.btn_delete);

        }
    }

    @Override
    public int getItemViewType(int position) {
        return (position == list_post.size() - 1 && isLoadingAdded && list_post.get(position) == null) ? TYPE_LOADING : TYPE_ITEM;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(
            @NonNull
            ViewGroup parent, int viewType) {
        if (viewType == TYPE_ITEM) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_my_post, parent, false);
            return new MyListPostViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_loading, parent, false);
            return new LoadingViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(
            @NonNull
            RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == TYPE_ITEM) {
            Post post = list_post.get(position);

            MyListPostViewHolder myPostHolder = (MyListPostViewHolder) holder;
            // Gán dữ liệu từ đối tượng Post vào các View
            myPostHolder.tvTitle.setText(post.getTitle());
            myPostHolder.tvPriority.setText(String.format("VIP%d", post.getPlanPriority()));
            myPostHolder.tvAddress.setText(post.getRegionAddress());
            myPostHolder.tvCost.setText(String.format(Locale.getDefault(), "%,d", post.getPrice()) + " VND");
            myPostHolder.tvArea.setText(String.valueOf(post.getArea()) + " m²");

            //
            myPostHolder.btn_edit.setOnClickListener(v -> listener.onButtonClicked(position, BUTTON_TYPE_EDIT));
            myPostHolder.btn_buy_plan.setOnClickListener(v -> listener.onButtonClicked(position, BUTTON_TYPE_BUY_PLAN));
            myPostHolder.btn_delete.setOnClickListener(v -> listener.onButtonClicked(position, BUTTON_TYPE_DELETE));

//             Cập nhật màu sắc dựa trên priority và ẩn nếu không hợp lệ
            if (post.getPlanPriority() == 1 || post.getPlanPriority() == 2 || post.getPlanPriority() == 3) {
                int borderColor, priorityBgColor;
                switch (post.getPlanPriority()) {
                    case 1:
                        borderColor = _context.getResources().getColor(R.color.gray);
                        priorityBgColor = _context.getResources().getColor(R.color.red);
                        break;
                    case 2:
                        borderColor = _context.getResources().getColor(R.color.gray);
                        priorityBgColor = _context.getResources().getColor(R.color.orange);
                        break;
                    case 3:
                        borderColor = _context.getResources().getColor(R.color.gray);
                        priorityBgColor = _context.getResources().getColor(R.color.yellow);
                        break;
                    default:
                        borderColor = _context.getResources().getColor(R.color.gray);
                        priorityBgColor = _context.getResources().getColor(R.color.gray);
                        break;
                }

                myPostHolder.tvPriority.setBackgroundTintList(ColorStateList.valueOf(priorityBgColor));

//                GradientDrawable drawable = (GradientDrawable) myPostHolder.parentLayout.getBackground();
//                drawable.setStroke(3, borderColor);

                GradientDrawable drawable = new GradientDrawable();
                drawable.setColor(Color.WHITE);
                drawable.setCornerRadius(20);
                drawable.setStroke(3, borderColor);
                myPostHolder.parentLayout.setBackground(drawable);

            } else {
                myPostHolder.tvPriority.setVisibility(View.GONE);
            }

//             Tải ảnh từ URL bằng Glide
            Glide.with(holder.itemView.getContext())
                    .load("https://thanhkhac.id.vn" + post.getThumbnail())
                    .into(myPostHolder.iv_thumbnail);
        }
    }

    @Override
    public int getItemCount() {
        return list_post.size();
    }

    // ViewHolder cho item loading
    public static class LoadingViewHolder extends RecyclerView.ViewHolder {
        public LoadingViewHolder(
                @NonNull
                View itemView) {
            super(itemView);
        }
    }
}
