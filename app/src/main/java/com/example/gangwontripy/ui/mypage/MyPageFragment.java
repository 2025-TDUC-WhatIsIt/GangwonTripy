package com.example.gangwontripy.ui.mypage;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.gangwontripy.R;
import com.example.gangwontripy.ui.splash.SplashActivity;

public class MyPageFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState){
        // 레이아웃 파일을 View 객체로 변환(inflate)
        View view = inflater.inflate(R.layout.fragment_my_page, container, false);
        // view 객체 반환 -> 시스템이 view를 화면에 그려줌
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState){
        super.onViewCreated(view, savedInstanceState);

        TextView logoutTextView = view.findViewById(R.id.menu_logout);
        logoutTextView.setOnClickListener(v -> {
            showLogoutDialog();
        });
    }

    private void showLogoutDialog() {
        new AlertDialog.Builder(getContext())
                .setTitle("로그아웃") // 팝업 제목
                .setMessage("로그아웃 하시겠습니까?")
                .setPositiveButton("예", (dialog, which) -> {
                    performLogout();
                })
                .setNegativeButton("아니오", null)
                .show();
    }

    private void performLogout(){
        // 1. SharedPreferences의 로그인 상태를 false로 변경
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("login_prefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("is_logged_in", false);
        // 필요하다면 사용자 토큰, ID 등 다른 정보도 합께 삭제
        editor.apply();

        // 2. 앱 재시작 -> 로그인 상태 다시 확인
        Intent intent = new Intent(getActivity(), SplashActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);

        // 3. 현재 Activity(MainActivity) 종료
        getActivity().finish();
    }
}
