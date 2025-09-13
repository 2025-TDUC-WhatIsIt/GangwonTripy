package com.whatisit.gangwontripy.ui.auth;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.whatisit.gangwontripy.MainActivity;
import com.whatisit.gangwontripy.R;
import com.whatisit.gangwontripy.core.SessionManager;
import com.whatisit.gangwontripy.data.model.LoginRes;

public class LoginActivity extends AppCompatActivity {

    private NavController navController;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // 이미 로그인 상태면 메인으로 바로 이동
//        if (SessionManager.getInstance(this).isLoggedIn()) {
//            Intent intent = new Intent(this, MainActivity.class);
//            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//            startActivity(intent);
//            finish();
//            return;
//        }
        Toolbar toolbar = findViewById(R.id.toolbar_login);
        setSupportActionBar(toolbar);

        // NavHostFragment에서 NavController를 가져옵니다.
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.fragment_container);
        navController = navHostFragment.getNavController();

        // NavController를 Toolbar와 연결하여 제목과 뒤로가기 버튼을 자동으로 관리합니다.
        NavigationUI.setupActionBarWithNavController(this, navController);

//        getSupportFragmentManager().addOnBackStackChangedListener(() -> {
//            // 백스택에 fragment가 하나라도 있으면 뒤로가기 버튼 표시
//            // 비어있으면 숨김
//            if (getSupportFragmentManager().getBackStackEntryCount() > 0 ) {
//                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//            } else {
//                getSupportActionBar().setDisplayHomeAsUpEnabled(false);
//            }
//        });

        // 앱이 처음 실행될 때에만 LoginFragment 추가
        // 화면 회전 등 상태 변경 시에는 Fragment가 자동 복원
//        if (savedInstanceState == null){
//            getSupportFragmentManager().beginTransaction()
//                    .replace(R.id.fragment_container, new LoginFragment())
//                    .commit();
//        }

        // ↓↓↓ 시스템 뒤로가기 버튼 처리를 위한 콜백 추가 ↓↓↓
        OnBackPressedCallback callback = new OnBackPressedCallback(true /* enabled by default */) {
            @Override
            public void handleOnBackPressed() {
                // NavController가 뒤로 갈 수 있는 스택이 있으면, 뒤로 이동합니다.
                if (!navController.navigateUp()) {
                    // 더 이상 뒤로 갈 수 없으면 (LoginFragment 상태),
                    // 콜백을 비활성화하고 Activity의 기본 뒤로가기 동작을 수행합니다.
                    setEnabled(false);
                    onBackPressed();
                }
            }
        };
        getOnBackPressedDispatcher().addCallback(this, callback);
    }

    // 각 Fragment에서 호출할 화면 전환 메소드
//    public void navigateToTerms() {
//        getSupportFragmentManager().beginTransaction()
//                .replace(R.id.fragment_container, new TermsFragment())
//                .addToBackStack(null) // 뒤로가기 스택에 추가
//                .commit();
//    }
//
//    public void navigateToRegister() {
//        getSupportFragmentManager().beginTransaction()
//                .replace(R.id.fragment_container, new RegisterFragment())
//                .addToBackStack(null) // 뒤로가기 스택에 추가
//                .commit();
//    }

    // 뒤로가기 버튼 처리
//    @Override
//    public boolean onSupportNavigateUp() {
//        // FragmentManager가 뒤로가기를 처리하도록 합니다.
//        if (getSupportFragmentManager().popBackStackImmediate()) {
//            return true;
//        }
//        return super.onSupportNavigateUp();
//    }

    // 4. 툴바의 뒤로가기 버튼 클릭을 NavController가 처리하도록 위임
    @Override
    public boolean onSupportNavigateUp() {
        // NavController가 알아서 이전 Fragment로 이동하거나,
        // 더 이상 돌아갈 곳이 없으면 Activity를 종료합니다.
        return navController.navigateUp() || super.onSupportNavigateUp();
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
