package com.example.gangwontripy.data.model;

public class LoginReq {
    public String email;   // 서버의 LoginReq 필드명에 맞추세요 (email이면 email로)
    public String password;

    public LoginReq(String email, String password) {
        this.email = email;
        this.password = password;
    }
}