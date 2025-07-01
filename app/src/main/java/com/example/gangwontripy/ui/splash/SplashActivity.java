package com.example.gangwontripy.ui.splash;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import com.example.gangwontripy.MainActivity;
import com.example.gangwontripy.R;

public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DELAY_TIME = 2000; // 2초

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Handler를 사용하여 일정 시간 후에 다음 화면으로 넘어감
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                // MainActivity로 이동하는 Intent 생성
                Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                startActivity(intent);

                // SplashActivity 종료 (뒤로 가기 버튼으로 돌아오지 않도록)
                finish();
            }
        }, SPLASH_DELAY_TIME);
    }
}