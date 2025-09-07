package com.example.gangwontripy.data.api;

import com.example.gangwontripy.data.model.LoginReq;
import com.example.gangwontripy.data.model.LoginRes;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface AuthApi {
    @POST("/api/auth/login")
    Call<LoginRes> login(@Body LoginReq req);
}