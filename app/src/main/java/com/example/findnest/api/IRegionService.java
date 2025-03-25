package com.example.findnest.api;

import com.example.findnest.model.responsedtos.RegionResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface IRegionService
{
    @GET("api/region/province")
    Call<List<RegionResponse>> getProvinces();

    @GET("api/region/district/{code}")
    Call<List<RegionResponse>> getDistricts(@Path("code") String provinceCode);

    @GET("api/region/ward/{code}")
    Call<List<RegionResponse>> getWards(@Path("code") String districtCode);
}
