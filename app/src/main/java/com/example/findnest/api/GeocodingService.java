package com.example.findnest.api;

import com.example.findnest.model.responsedtos.GeocodingResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface GeocodingService {
    @GET("search")
    Call<List<GeocodingResponse>> getCoordinates(
            @Query("q") String query,
            @Query("format") String format,
            @Query("limit") int limit
    );
}