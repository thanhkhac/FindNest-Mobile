package com.example.findnest.ui.fragment;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.findnest.R;

public class AccountFragment extends Fragment {

    LinearLayout historyPayment;

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