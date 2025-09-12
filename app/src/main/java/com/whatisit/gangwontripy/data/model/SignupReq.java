// app/java/.../data/model/SignupReq.java
package com.whatisit.gangwontripy.data.model;

public class SignupReq {
    private String email;
    private String password;
    private String username;

    public SignupReq(String email, String password, String username) {
        this.email = email;
        this.password = password;
        this.username = username;
    }
    // getter/setter 필요하면 추가
}
