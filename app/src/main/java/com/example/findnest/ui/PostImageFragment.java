package com.example.findnest.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.denzcoskun.imageslider.ImageSlider;
import com.denzcoskun.imageslider.constants.ScaleTypes;
import com.denzcoskun.imageslider.models.SlideModel;
import com.example.findnest.R;

import java.util.ArrayList;
import java.util.List;

public class PostImageFragment extends Fragment {

    private static final String ARG_IMAGE_URLS = "imageUrls";
    private List<SlideModel> slideModels;

    ImageSlider imageSlider;
    public static PostImageFragment newInstance(List<String> imageUrls) {
        PostImageFragment fragment = new PostImageFragment();
        Bundle args = new Bundle();
        args.putStringArrayList(ARG_IMAGE_URLS, new ArrayList<>(imageUrls));
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_post_image, container, false);
        imageSlider = view.findViewById(R.id.imageSlider);
        if (getArguments() != null) {
            List<String> imageUrls = getArguments().getStringArrayList(ARG_IMAGE_URLS);
            slideModels = new ArrayList<>();
            for (String url : imageUrls) {
                slideModels.add(new SlideModel(url, ScaleTypes.FIT));
            }
            imageSlider.setImageList(slideModels);
        }
        return view;
    }
}
