package com.example.findnest.ui;

import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import com.example.findnest.R;
import com.example.findnest.auth.AuthManager;

public class AccountFragment extends Fragment {

    AuthManager authManager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_account, container, false);

        LinearLayout btn_go_to_profile = view.findViewById(R.id.btn_go_to_profile);
        btn_go_to_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
                transaction.replace(R.id.frame_container, new ProfileFragment());
                transaction.addToBackStack(null); // Allows back navigation
                transaction.commit();
            }
        });

        Context context = requireContext();
        authManager = new AuthManager(context);
        Button btnLogout = view.findViewById(R.id.logout_button);
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                authManager.clearTokens();
                Intent it = new Intent(context, LoginActivity.class);
                it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(it);
            }
        });

        return view;
    }
}