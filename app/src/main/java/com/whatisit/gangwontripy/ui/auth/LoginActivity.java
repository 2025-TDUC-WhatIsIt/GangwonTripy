package com.whatisit.gangwontripy.ui.auth;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.whatisit.gangwontripy.MainActivity;
import com.whatisit.gangwontripy.R;
import com.whatisit.gangwontripy.core.SessionManager;
import com.whatisit.gangwontripy.data.model.LoginRes;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
// 이미 로그인 상태면 메인으로 바로 이동
        if (SessionManager.getInstance(this).isLoggedIn()) {
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return;
        }
        Toolbar toolbar = findViewById(R.id.toolbar_login);
        setSupportActionBar(toolbar);

        getSupportFragmentManager().addOnBackStackChangedListener(() -> {
            // 백스택에 fragment가 하나라도 있으면 뒤로가기 버튼 표시
            // 비어있으면 숨김
            if (getSupportFragmentManager().getBackStackEntryCount() > 0 ) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            } else {
                getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            }
        });

        // 앱이 처음 실행될 때에만 LoginFragment 추가
        // 화면 회전 등 상태 변경 시에는 Fragment가 자동 복원
        if (savedInstanceState == null){
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new LoginFragment())
                    .commit();
        }
    }

    // 각 Fragment에서 호출할 화면 전환 메소드
    public void navigateToTerms() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new TermsFragment())
                .addToBackStack(null) // 뒤로가기 스택에 추가
                .commit();
    }

    public void navigateToRegister() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new RegisterFragment())
                .addToBackStack(null) // 뒤로가기 스택에 추가
                .commit();
    }

    // 뒤로가기 버튼 처리
    @Override
    public boolean onSupportNavigateUp() {
        // FragmentManager가 뒤로가기를 처리하도록 합니다.
        if (getSupportFragmentManager().popBackStackImmediate()) {
            return true;
        }
        return super.onSupportNavigateUp();
    }

    public void onLoginSuccess(LoginRes res) {
        SessionManager.getInstance(this).saveLogin(res);

        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    // SharedPreferences에 로그인 상태 저장하는 메소드
    private void saveLoginState(boolean isLoggedIn) {
        SharedPreferences sharedPreferences = getSharedPreferences("login_prefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("is_logged_in", isLoggedIn);
        editor.apply();
    }
}
