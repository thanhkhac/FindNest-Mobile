package com.example.findnest.adapter;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.content.res.ColorStateList;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.findnest.R;
import com.example.findnest.model.Post;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class ListPostAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_ITEM = 0;
    private static final int TYPE_LOADING = 1;

    private Context _context;
    private List<Post> list_post;
    private boolean isLoadingAdded = false;

    public ListPostAdapter(Context context, List<Post> postList) {
        this._context = context;
        this.list_post = postList;
    }

    @Override
    public int getItemViewType(int position) {
        return (position == list_post.size() - 1 && isLoadingAdded && list_post.get(position) == null) ? TYPE_LOADING : TYPE_ITEM;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_ITEM) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_post_item, parent, false);
            return new PostViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_loading, parent, false);
            return new LoadingViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == TYPE_ITEM) {
            Post post = list_post.get(position);

            PostViewHolder postHolder = (PostViewHolder) holder;
            // Gán dữ liệu từ đối tượng Post vào các View
            postHolder.tvTitle.setText(post.getTitle());
            postHolder.tvPriority.setText(String.format("VIP%d", post.getPlanPriority()));
            postHolder.tvAddress.setText(post.getRegionAddress());
            postHolder.tvCost.setText(String.format(Locale.getDefault(), "%,d", post.getPrice()) + " VND");
            postHolder.tvArea.setText(String.valueOf(post.getArea()) + " m²");
            postHolder.tvBedroom.setText(String.valueOf(post.getBedRoomCount()));
            postHolder.tvBathroom.setText(String.valueOf(post.getBathRoomCount()));
            postHolder.tvOwner.setText(post.getCreatedBy());

            // Định dạng ngày từ chuỗi
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

            try {
                if (post.getCreatedAt() != null && !post.getCreatedAt().isEmpty()) {
                    java.util.Date date = inputFormat.parse(post.getCreatedAt());
                    postHolder.tvCreatedAt.setText(outputFormat.format(date));
                } else {
                    postHolder.tvCreatedAt.setText("Không có ngày");
                }
            } catch (ParseException e) {
                e.printStackTrace();
                postHolder.tvCreatedAt.setText("Lỗi định dạng ngày");
            }

            // Cập nhật màu sắc dựa trên priority và ẩn nếu không hợp lệ
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

                postHolder.tvPriority.setBackgroundTintList(ColorStateList.valueOf(priorityBgColor));
                GradientDrawable drawable = (GradientDrawable) postHolder.parentLayout.getBackground();
                drawable.setStroke(5, borderColor);
            } else {
                postHolder.tvPriority.setVisibility(View.GONE);
            }

            // Tải ảnh từ URL bằng Glide
//            Log.d("Thumbnail URL", "URL: " + post.getThumbnail());
            Glide.with(holder.itemView.getContext())
                    .load("https://thanhkhac.id.vn" + post.getThumbnail())
                    .into(postHolder.imageView);
        }
    }

    @Override
    public int getItemCount() {
        return list_post.size();
    }
    // ViewHolder cho item bài đăng
    public static class PostViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvPriority, tvAddress, tvCost, tvArea, tvBedroom, tvBathroom, tvOwner, tvCreatedAt;
        ImageView imageView;
        LinearLayout parentLayout;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_post_title);
            tvPriority = itemView.findViewById(R.id.tv_post_priority);
            tvAddress = itemView.findViewById(R.id.tv_address_value);
            tvCost = itemView.findViewById(R.id.tv_cost_value);
            tvArea = itemView.findViewById(R.id.tv_area);
            tvBedroom = itemView.findViewById(R.id.tv_bedroom_number_value);
            tvBathroom = itemView.findViewById(R.id.tv_bath_num_value);
            tvOwner = itemView.findViewById(R.id.tv_post_owner);
            tvCreatedAt = itemView.findViewById(R.id.tv_createdAt);
            imageView = itemView.findViewById(R.id.iv_thumbnail);
            parentLayout = itemView.findViewById(R.id.parentLayout);
        }
    }

    // ViewHolder cho item loading
    public static class LoadingViewHolder extends RecyclerView.ViewHolder {
        public LoadingViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}