package com.example.findnest.ui.fragment;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.findnest.R;
import com.example.findnest.adapter.PaymentAdapter;
import com.example.findnest.api.IMangeAPI;
import com.example.findnest.api.IPostAPI;
import com.example.findnest.api.client.IPaymentAPI;
import com.example.findnest.api.client.auth.AuthManager;
import com.example.findnest.api.client.retrofit.RetrofitClient;
import com.example.findnest.model.request.payment.CreateQRReq;
import com.example.findnest.model.response.post_plan.PostPlanRes;
import com.example.findnest.model.response.transaction.PaymentQRRes;
import com.example.findnest.model.response.transaction.PaymentRes;
import com.example.findnest.model.response.transaction.TransactionHistoryRes;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.android.material.button.MaterialButton;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PaymentFragment extends Fragment {

    private AuthManager authManager;
    private IMangeAPI iManageAPI;
    private IPaymentAPI iPaymentAPI;
    private MaterialButton btnSelectPayment, btnStatics;
    private List<TransactionHistoryRes> transactionHistoryRes;
    private List<PostPlanRes> postPlanRes;
    TextView tvBalance;
    private List<PaymentRes> paymentRes;
    private RecyclerView recyclerView;
    private PaymentAdapter paymentAdapter;
    private ImageView imageQR;
    TextView txtConfirmQR;
    TextView txtBalance;
    BarChart barChart;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_payment, container, false);
        final Dialog dialog = new Dialog(getContext());
        final Dialog dialogChart = new Dialog(getContext());
        initDialog(dialog);
        initDialogChart(dialogChart);
        init(view);
        fetchDataTransaction();
        btnSelectPayment.setOnClickListener(v -> {
            openPaymentDialog(Gravity.CENTER, dialog);
        });
        btnStatics.setOnClickListener(v -> {
            openChartDialog(Gravity.CENTER, dialogChart);
        });
        return view;
    }

    void initDialog(Dialog dialog){
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_payment);
        txtBalance = dialog.findViewById(R.id.txtBalance);
    }

    void initDialogChart(Dialog dialog){
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_chart);
        barChart = dialog.findViewById(R.id.barChart);
    }

    public void openChartDialog(int gravity, Dialog dialog){
        Window window = dialog.getWindow();
        if(window == null){
            return;
        }
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        WindowManager.LayoutParams windowAttributes = window.getAttributes();
        windowAttributes.gravity = gravity;
        window.setAttributes(windowAttributes);

        if(Gravity.BOTTOM == gravity){
            dialog.setCancelable(true);
        }else {
            dialog.setCancelable(false);
        }
        ImageButton btnClose = dialog.findViewById(R.id.btnCloseChart);
        btnClose.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }
    public void openPaymentDialog(int gravity, Dialog dialog){
        DecimalFormat formatter = new DecimalFormat("#,###");
        Window window = dialog.getWindow();
        if(window == null){
            return;
        }

        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        WindowManager.LayoutParams windowAttributes = window.getAttributes();
        windowAttributes.gravity = gravity;
        window.setAttributes(windowAttributes);

        if(Gravity.BOTTOM == gravity){
            dialog.setCancelable(true);
        }else {
            dialog.setCancelable(false);
        }

        ImageButton btnClose = dialog.findViewById(R.id.btnClose);
        EditText edtMoney = dialog.findViewById(R.id.edtMoney);
        MaterialButton btnCreateQR = dialog.findViewById(R.id.btnCreateQR);
        MaterialButton btnConfirmQR = dialog.findViewById(R.id.btnConfirmQR);
        txtConfirmQR = dialog.findViewById(R.id.txtConfirmQR);
        imageQR = dialog.findViewById(R.id.imageQR);
        LinearLayout layoutQR = dialog.findViewById(R.id.layoutQR);
        layoutQR.setVisibility(View.GONE);

        btnClose.setOnClickListener(v -> dialog.dismiss());
        btnCreateQR.setOnClickListener(v ->{
            if(edtMoney.getText().toString().isEmpty() || Long.parseLong(edtMoney.getText().toString()) < 20000){{
                Toast.makeText(getContext(), "Vui lòng nhập số tiền lớn hơn 20000!", Toast.LENGTH_SHORT).show();
                return;
            }}
            txtConfirmQR.setText("Quét mã QR để nạp tiền:" + formatter.format(Float.parseFloat(edtMoney.getText().toString())) + "VNĐ");
            CreateQR(CreateQRReq.builder()
                    .money(Long.parseLong(edtMoney.getText().toString()))
                    .build(), dialog);
            layoutQR.setVisibility(View.VISIBLE);
        });

        btnConfirmQR.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Nạp tiền thành công!", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
            getActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .addToBackStack(null)
                    .replace(PaymentFragment.this.getId(), new PaymentFragment())
                    .commit();
        });

        dialog.show();
    }
    public void init(View view){
        btnSelectPayment = view.findViewById(R.id.btn_select_payment);
        btnStatics = view.findViewById(R.id.btnStatics);
        btnStatics = view.findViewById(R.id.btnStatics);
        tvBalance = view.findViewById(R.id.tvBalance);
        recyclerView = view.findViewById(R.id.recyclerView);
    }

    void fetchDataTransaction() {
        transactionHistoryRes = new ArrayList<>();
        authManager = new AuthManager(getContext());
        iManageAPI = RetrofitClient.getClient(authManager).create(IMangeAPI.class);
        iManageAPI.getTransactionHistory().enqueue(new Callback<List<TransactionHistoryRes>>() {
            @Override
            public void onResponse(Call<List<TransactionHistoryRes>> call, Response<List<TransactionHistoryRes>> response) {
                if(response.isSuccessful() && response.body() != null){
                    transactionHistoryRes = response.body();
                    fetchDataPostPlan();
                }else{
                    Toast.makeText(getContext(), "Lỗi lấy dữ liệu!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<TransactionHistoryRes>> call, Throwable t) {
                Log.e("API_ERROR", t.getMessage());
                Toast.makeText(getContext(), "Không thể kết nối!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    void fetchDataPostPlan(){
        postPlanRes = new ArrayList<>();
        iManageAPI.getPostPlanDetail().enqueue(new Callback<List<PostPlanRes>>() {

            @Override
            public void onResponse(Call<List<PostPlanRes>> call, Response<List<PostPlanRes>> response) {
                if(response.isSuccessful() && response.body() != null){
                    postPlanRes = response.body();
                    try {
                        importData();
                    } catch (ParseException e) {
                        throw new RuntimeException(e);
                    }
                }else{
                    Toast.makeText(getContext(), "Lỗi lấy dữ liệu!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<PostPlanRes>> call, Throwable t) {
                Log.e("API_ERROR", t.getMessage());
                Toast.makeText(getContext(), "Không thể kết nối!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    void CreateQR(CreateQRReq rq, Dialog dialog){
        authManager = new AuthManager(getContext());
        iPaymentAPI = RetrofitClient.getClient(authManager).create(IPaymentAPI.class);
        iPaymentAPI.createQR(rq).enqueue(new Callback<PaymentQRRes>() {
            @Override
            public void onResponse(Call<PaymentQRRes> call, Response<PaymentQRRes> response) {
                if(response.isSuccessful() && response.body() != null){
                    Toast.makeText(getContext(), "Tạo mã QR thành công!", Toast.LENGTH_SHORT).show();
                    PaymentQRRes payment = response.body();
                    Glide.with(dialog.getContext())
                            .load(payment.getUrl())
                            .placeholder(R.drawable.close_24px) // Ảnh mặc định khi tải
                            .error(R.drawable.close_24px) // Ảnh hiển thị khi lỗi
                            .into(imageQR);
                }else{
                    Toast.makeText(getContext(), "Lỗi tạo mã QR!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<PaymentQRRes> call, Throwable t) {
                Log.e("API_ERROR", t.getMessage());
                Toast.makeText(getContext(), "Không thể kết nối!", Toast.LENGTH_SHORT).show();
            }
        });
    }
    void importData() throws ParseException {
        paymentRes = new ArrayList<>();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        DecimalFormat formatter = new DecimalFormat("#,###");
        float total = 0;
        for (TransactionHistoryRes transaction : transactionHistoryRes) {
            total += transaction.getTransferAmount().floatValue();
            paymentRes.add(PaymentRes.builder()
                    .action("Nạp tiền")
                    .date(dateFormat.parse(transaction.getTransactionDate()))
                    .price(transaction.getTransferAmount().floatValue())
                    .build());
        }
        for (PostPlanRes plan : postPlanRes) {
            total -= plan.getPlan().getPrice().floatValue();
            paymentRes.add(PaymentRes.builder()
                    .action("Mua gói tin")
                    .date(dateFormat.parse(plan.getStartDate()))
                    .price(-plan.getPlan().getPrice().floatValue())
                    .build());
        }
        paymentRes.sort(Comparator.comparing(PaymentRes::getDate).reversed());
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        paymentAdapter = new PaymentAdapter(paymentRes);
        recyclerView.setAdapter(paymentAdapter);
        tvBalance.setText(formatter.format(total)+" VNĐ");
        txtBalance.setText("Số dư hiện tại\n"+formatter.format(total) + " VNĐ");
        setupChart(paymentRes);
    }

    void setupChart(List<PaymentRes> paymentRes){
        barChart.getAxisRight().setDrawLabels(false);

        ArrayList<String> months = new ArrayList<>(Arrays.asList(
                "Jan", "Feb", "Mar", "Apr", "May", "Jun",
                "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"));

        float[] totalNap = new float[12];
        float[] totalMua = new float[12];

        for (PaymentRes payment : paymentRes) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(payment.getDate());
            int month = calendar.get(Calendar.MONTH);

            if ("Nạp tiền".equalsIgnoreCase(payment.getAction())) {
                totalNap[month] += payment.getPrice();
            } else if ("Mua gói tin".equalsIgnoreCase(payment.getAction())) {
                totalMua[month] += -payment.getPrice();
            }
        }

        ArrayList<BarEntry> entriesNap = new ArrayList<>();
        ArrayList<BarEntry> entriesMua = new ArrayList<>();

        for (int i = 0; i < 12; i++) {
            entriesNap.add(new BarEntry(i, totalNap[i])); // Dữ liệu Nạp
            entriesMua.add(new BarEntry(i, totalMua[i])); // Dữ liệu Mua
        }

        // Tạo 2 dataset
        BarDataSet dataSetNap = new BarDataSet(entriesNap, "Số tiền nạp");
        dataSetNap.setColor(Color.BLUE);

        BarDataSet dataSetMua = new BarDataSet(entriesMua, "Số tiền mua");
        dataSetMua.setColor(Color.RED);

        BarData barData = new BarData(dataSetNap, dataSetMua);
        barData.setBarWidth(0.4f); // Độ rộng của cột

        XAxis xAxis = barChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(months));
        xAxis.setGranularity(1f);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularityEnabled(true);
        xAxis.setCenterAxisLabels(true);

        YAxis leftAxis = barChart.getAxisLeft();
        leftAxis.setAxisMinimum(0);

        barChart.setData(barData);
        barChart.groupBars(-0.5f, 0.2f, 0.05f);
        barChart.getDescription().setEnabled(false);
        barChart.invalidate();
    }
}