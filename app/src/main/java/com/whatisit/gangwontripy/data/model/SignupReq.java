package com.whatisit.gangwontripy.data.model;

import com.google.gson.annotations.SerializedName;
// (선택) proguard 안심용
import androidx.annotation.Keep;

@Keep
public class SignupReq {
    @SerializedName("email")
    private String email;

    @SerializedName("password")
    private String password;

    @SerializedName("nickname")
    private String nickname;

    public SignupReq(String email, String password, String nickname) {
        this.email = email;
        this.password = password;
        this.nickname = nickname;
    }
}
