package com.example.findnest.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.findnest.R;

public class ThumbnailFragment extends Fragment {

    private static final String ARG_IMAGE_URL = "imageUrl";
    private String imageUrl;

    public static ThumbnailFragment newInstance(String imageUrl) {
        ThumbnailFragment fragment = new ThumbnailFragment();
        Bundle args = new Bundle();
        args.putString(ARG_IMAGE_URL, imageUrl);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_thumbnail, container, false);

        // Nhận dữ liệu từ Bundle
        if (getArguments() != null) {
            imageUrl = getArguments().getString(ARG_IMAGE_URL);
        }

        // Cập nhật hình ảnh trong ImageView
        ImageView imageView = view.findViewById(R.id.imageView);
        Glide.with(this)
                .load(imageUrl)
                .placeholder(R.drawable.close_24px) // Ảnh mặc định khi tải
                .error(R.drawable.close_24px) // Ảnh hiển thị khi lỗi
                .into(imageView);

        return view;
    }
}
