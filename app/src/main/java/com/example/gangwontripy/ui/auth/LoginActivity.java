package com.example.gangwontripy.ui.auth;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.gangwontripy.MainActivity;
import com.example.gangwontripy.R;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Button loginButton = findViewById(R.id.login_btn);
        loginButton.setOnClickListener(v -> {
            // TODO 실제 아이디/비밀번호 확인 로직

            onLoginSuccess();
        });
    }

    private void onLoginSuccess() {
        saveLoginState(true);

        Intent intent = new Intent(this, MainActivity.class);

        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);

        // LoginActivity 종료
        finish();
    }

    // SharedPreferences에 로그인 상태 저장하는 메소드
    private void saveLoginState(boolean isLoggedIn) {
        SharedPreferences sharedPreferences = getSharedPreferences("login_prefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("is_logged_in", isLoggedIn);
        editor.apply();
    }
    // TODO 로그아웃 기능 구현 시 saveLoginState(false) 호출하면 됨 (프론트)
}
