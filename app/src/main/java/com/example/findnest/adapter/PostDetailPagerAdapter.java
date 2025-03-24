package com.example.findnest.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.findnest.ui.fragment.PostImageFragment;
import com.example.findnest.ui.fragment.ThumbnailFragment;

import java.util.List;

public class PostDetailPagerAdapter extends FragmentStateAdapter {

    private final String imageUrl;
    private List<String> slideModels;
    public PostDetailPagerAdapter(@NonNull androidx.fragment.app.FragmentActivity fragmentActivity,
                                  String imageUrl,
                                  List<String> slideModels) {
        super(fragmentActivity);
        this.imageUrl = imageUrl;
        this.slideModels = slideModels;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return  PostImageFragment.newInstance(slideModels);
            case 1:
                return  ThumbnailFragment.newInstance(imageUrl);
        }
        return null;
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}
