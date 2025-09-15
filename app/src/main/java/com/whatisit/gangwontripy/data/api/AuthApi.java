package com.whatisit.gangwontripy.data.api;

import com.whatisit.gangwontripy.data.model.LoginReq;
import com.whatisit.gangwontripy.data.model.LoginRes;
import com.whatisit.gangwontripy.data.model.SignupReq;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface AuthApi {
    @POST("/api/auth/login")
    Call<LoginRes> login(@Body LoginReq req);

    @POST("/api/auth/signup")
    Call<LoginRes> signup(@Body SignupReq req);
}