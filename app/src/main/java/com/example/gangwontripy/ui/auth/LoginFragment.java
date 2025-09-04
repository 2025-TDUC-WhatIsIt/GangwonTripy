package com.example.gangwontripy.ui.auth;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.gangwontripy.MainActivity;
import com.example.gangwontripy.R;

public class LoginFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState){
        // fragment_login.xml 레이아웃을 inflate하여 View 객체를 생성하고 반환함
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState){
        super.onViewCreated(view, savedInstanceState);

        Button loginButton = view.findViewById(R.id.login_btn);
        ImageButton kakaoLoginButton = view.findViewById(R.id.login_kakao);
        TextView registerButton = view.findViewById(R.id.register);
        TextView findAccountButton = view.findViewById(R.id.find_account);
        TextView inquiryButton = view.findViewById(R.id.inquiry);

        // 로그인 버튼
        loginButton.setOnClickListener(v -> {
            // TODO 실제 아이디/비밀번호 확인 로직

            if (getActivity() instanceof LoginActivity){
                ((LoginActivity) getActivity()).onLoginSuccess();
            }
        });

        // 카카오 로그인 버튼
        kakaoLoginButton.setOnClickListener(v -> {
            // TODO 카카오로그인
        });

        // 회원가입
        registerButton.setOnClickListener(v -> {
            if (getActivity() instanceof LoginActivity){
                ((LoginActivity) getActivity()).navigateToTerms();
            }
        });

        // 계정찾기
        findAccountButton.setOnClickListener(v -> {

        });

        // 문의하기
        inquiryButton.setOnClickListener(v -> {

        });
    }

    @Override
    public void onResume() {
        super.onResume();

        AppCompatActivity activity = (AppCompatActivity) getActivity();
        if (activity != null) {
            ActionBar actionBar = activity.getSupportActionBar();
            if (actionBar != null) {
                // LoginFragment에서는 제목을 비우거나 앱 이름으로 설정
                actionBar.setTitle("");
                // 첫 화면이므로 뒤로가기 버튼을 숨깁니다.
                actionBar.setDisplayHomeAsUpEnabled(false);
            }
        }
    }
}
