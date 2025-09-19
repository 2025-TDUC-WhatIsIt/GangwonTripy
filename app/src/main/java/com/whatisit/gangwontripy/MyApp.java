package com.whatisit.gangwontripy;

import android.app.Application;

import com.kakao.vectormap.KakaoMapSdk;

public class MyApp extends Application {
    @Override public void onCreate() {
        super.onCreate();
        KakaoMapSdk.init(this, BuildConfig.KAKAO_MAP_KEY);
    }
}