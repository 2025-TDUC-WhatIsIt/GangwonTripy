// app/java/.../data/model/SignupReq.java
package com.whatisit.gangwontripy.data.model;

public class SignupReq {
    private String email;
    private String password;
    private String nickname;

    public SignupReq(String email, String password, String nickname) {
        this.email = email;
        this.password = password;
        this.nickname = nickname;
    }
    // getter/setter 필요하면 추가
}
