package com.example.findnest.api.client.retrofit;
import com.example.findnest.api.client.auth.TrustAllCerts;
import com.example.findnest.api.client.auth.TrustAllHostnameVerifier;
import com.example.findnest.api.client.auth.AuthInterceptor;
import com.example.findnest.api.client.auth.AuthManager;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

public class RetrofitClient {
    private static final String BASE_URL =  "https://thanhkhac.id.vn/";
//    private static final String BASE_URL = "https://10.0.2.2:7011/";
    private static Retrofit retrofit = null;

    public static Retrofit getClient(AuthManager authManager) {
        if (retrofit == null) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY); // Logs request & response body

            TrustManager[] trustAllCerts = new TrustManager[]{new TrustAllCerts()};
            // Install the TrustManager
            SSLContext sslContext;
            try {
                sslContext = SSLContext.getInstance("SSL");
                sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            OkHttpClient client = new OkHttpClient.Builder()
                    .sslSocketFactory(sslContext.getSocketFactory(), new TrustAllCerts())
                    .hostnameVerifier(new TrustAllHostnameVerifier())
                    .addInterceptor(new AuthInterceptor(authManager)) //attach interceptor
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
}
