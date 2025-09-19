package com.whatisit.gangwontripy;

import android.app.Application;
import android.util.Log;

import com.kakao.sdk.common.util.Utility;
import com.kakao.vectormap.KakaoMapSdk;

public class MyApp extends Application {
    @Override public void onCreate() {
        super.onCreate();
        KakaoMapSdk.init(this, BuildConfig.KAKAO_MAP_KEY);
        String keyHash = Utility.INSTANCE.getKeyHash(this);
        Log.d("KeyHash", "release keyHash = " + keyHash);
    }
}