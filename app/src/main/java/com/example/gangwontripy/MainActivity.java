package com.example.gangwontripy;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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

    }
}