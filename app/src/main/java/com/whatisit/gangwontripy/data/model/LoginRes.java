// app/.../data/model/LoginRes.java
package com.whatisit.gangwontripy.data.model;
import androidx.annotation.Keep;
import com.google.gson.annotations.SerializedName;

@Keep
public class LoginRes {
    @SerializedName("userId")         public Long userId;
    @SerializedName("nickname")       public String nickname;
    @SerializedName("profileImageUrl")public String profileImageUrl;
}
