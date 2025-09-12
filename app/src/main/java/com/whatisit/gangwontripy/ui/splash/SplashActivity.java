package com.whatisit.gangwontripy.ui.splash;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import com.whatisit.gangwontripy.MainActivity;
import com.whatisit.gangwontripy.R;
import com.whatisit.gangwontripy.ui.auth.LoginActivity;

public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DELAY_TIME = 2000; // 2초

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            // 로그인 상태 확인
            if (isLoggedIn()){
                // 로그인 상태라면 MainActivity로 이동
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
            } else {
                // 로그아웃 상태라면 LoginActivity로 이동
                Intent intent = new Intent(this, LoginActivity.class);
                startActivity(intent);
            }

            // SplashActivity 종료 (뒤로 가기 버튼으로 돌아오지 않도록)
            finish();
        }, SPLASH_DELAY_TIME);
    }

    private boolean isLoggedIn() {
        SharedPreferences sharedPreferences = getSharedPreferences("login_prefs", Context.MODE_PRIVATE);
        // is_logged_in 키의 값을 가져옴. 값이 없으면 기본값으로 false 반환
        return sharedPreferences.getBoolean("is_logged_in", false);
    }
}