package com.whatisit.gangwontripy.data.model;

import com.google.gson.annotations.SerializedName;

public class LoginReq {
    @SerializedName("email") public String email;   // 서버의 LoginReq 필드명에 맞추세요 (email이면 email로)
    @SerializedName("password") public String password;

    public LoginReq(String email, String password) {
        this.email = email;
        this.password = password;
    }
}