package com.example.findnest.api;

import com.example.findnest.model.DistrictDTO;
import com.example.findnest.model.ProvinceDTO;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Path;

public interface IRegionService {
    @Headers("accept: */*")
    @GET("api/region/province")
    Call<List<ProvinceDTO>> getProvinces();

    @GET("api/region/district/{provinceCode}")
    Call<List<DistrictDTO>> getDistricts(@Path("provinceCode") String provinceCode);

}
