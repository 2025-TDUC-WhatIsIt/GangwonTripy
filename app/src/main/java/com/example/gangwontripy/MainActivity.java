package com.example.gangwontripy;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.kakao.vectormap.KakaoMapSdk;
import com.kakao.sdk.common.util.Utility;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        키 해시 확인용 코드
//        String keyHash = Utility.INSTANCE.getKeyHash(this);
//        Log.d("KeyHash", keyHash);
        setContentView(R.layout.activity_main);
        // 하단바가 있는 폰과 없는 폰에서 네비게이션 바 처리 부분
        // 엣지-투-엣지로 그리고 인셋은 우리가 처리
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        // 하단 네비게이션 바 구현
        // 1. 레이아웃에서 BottomNavigationView 찾기
        BottomNavigationView bottomNavView = findViewById(R.id.bottom_nav_view);
        // 2. FragmentContainerView에서 NavController 찾기
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);
        NavController navController = navHostFragment.getNavController();

        // 3. BottomNavigationView를 NavComtroller와 연결
        // -> 메뉴 아이템 클릭 시 자동으로 Fragment 교체해주는 코드임
        NavigationUI.setupWithNavController(bottomNavView, navController);
        //  시스템 바(상단/하단) 인셋 적용
        // ✅ 오직 바텀네비에만 인셋 적용 (하단 시스템바/IME 높이만큼)
        // ✅ 오직 BottomNavigationView에 시스템 하단 바(또는 IME) 높이만큼 "bottom 마진" 적용
        ViewCompat.setOnApplyWindowInsetsListener(bottomNavView, (v, insets) -> {
            int sysBottom = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom;
            int imeBottom = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom;
            int bottom = Math.max(sysBottom, imeBottom); // 키보드가 있으면 더 큰 쪽 사용

            ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
            if (lp.bottomMargin != bottom) {
                lp.bottomMargin = bottom;   // ← 뷰 전체를 위로 올림
                v.setLayoutParams(lp);
            }

            // 패딩은 0으로 유지 (아이콘만 올라가면 안 됨)
            v.setPadding(v.getPaddingLeft(), v.getPaddingTop(), v.getPaddingRight(), 0);

            return insets; // 다른 뷰로 인셋 전달
        });
    }
}