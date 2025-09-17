package com.whatisit.gangwontripy.data.api;

import com.whatisit.gangwontripy.data.model.MagazineRes;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;

public interface MagazineApi {
    @GET("/api/magazines")
    Call<List<MagazineRes>> list();
}