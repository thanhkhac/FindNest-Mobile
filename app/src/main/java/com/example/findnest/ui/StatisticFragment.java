package com.example.findnest.ui;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.findnest.R;
import com.example.findnest.model.ProductEntity;
import com.example.findnest.repository.Repository;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StatisticFragment extends Fragment {

    private Repository repository;
    private BarChart barChart;
    private PieChart pieChart;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_statistic, container, false);

        repository = new Repository(requireContext());
        barChart = view.findViewById(R.id.bar_chart);
        pieChart = view.findViewById(R.id.pie_chart);

        showPriceStatistics();
        showCategoryStatistics();

        return view;
    }

    private void showPriceStatistics() {
        List<ProductEntity> products = repository.getAll();
        if (products.isEmpty()) return;

        int minPrice = Integer.MAX_VALUE, maxPrice = Integer.MIN_VALUE;
        for (ProductEntity p : products) {
            if (p.getPrice() < minPrice) minPrice = p.getPrice();
            if (p.getPrice() > maxPrice) maxPrice = p.getPrice();
        }

        int range1 = minPrice + (maxPrice - minPrice) / 3;
        int range2 = minPrice + 2 * (maxPrice - minPrice) / 3;

        int countLow = 0, countMid = 0, countHigh = 0;
        for (ProductEntity p : products) {
            if (p.getPrice() <= range1) countLow++;
            else if (p.getPrice() <= range2) countMid++;
            else countHigh++;
        }

        List<BarEntry> entries = new ArrayList<>();
        entries.add(new BarEntry(1, countLow));
        entries.add(new BarEntry(2, countMid));
        entries.add(new BarEntry(3, countHigh));

        BarDataSet dataSet = new BarDataSet(entries, "Price Range");
        dataSet.setColors(Color.GREEN, Color.YELLOW, Color.RED);

        BarData barData = new BarData(dataSet);
        barChart.setData(barData);
        barChart.invalidate();
    }

    private void showCategoryStatistics() {
        List<ProductEntity> products = repository.getAll();
        if (products.isEmpty()) return;

        Map<String, Integer> categoryCount = new HashMap<>();
        for (ProductEntity p : products) {
            categoryCount.put(p.getCategory(), categoryCount.getOrDefault(p.getCategory(), 0) + 1);
        }

        List<PieEntry> pieEntries = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : categoryCount.entrySet()) {
            pieEntries.add(new PieEntry(entry.getValue(), entry.getKey()));
        }

        PieDataSet dataSet = new PieDataSet(pieEntries, "Product Categories");
        dataSet.setColors(Color.BLUE, Color.MAGENTA, Color.CYAN, Color.GRAY, Color.LTGRAY);

        PieData pieData = new PieData(dataSet);
        pieChart.setData(pieData);
        pieChart.invalidate();
    }
}