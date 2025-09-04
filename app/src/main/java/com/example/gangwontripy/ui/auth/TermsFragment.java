package com.example.gangwontripy.ui.auth;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.gangwontripy.R;

public class TermsFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // fragment_terms.xml 레이아웃을 inflate하여 View 객체를 생성하고 반환함
        return inflater.inflate(R.layout.fragment_terms, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Button createAccountButton = view.findViewById(R.id.create_account_btn);

        // "계정 생성하기" Button 클릭 리스너
        createAccountButton.setOnClickListener(v -> {
            // TODO: 모든 필수 약관에 동의했는지 확인하는 로직 추가

            // 모든 약관에 동의했다면 다음 단계로 이동
            ((LoginActivity) getActivity()).navigateToRegister();
        });
    }

    @Override
    public void onResume() {
        super.onResume();

        // 1. 현재 Fragment를 포함하고 있는 Activity를 가져옵니다.
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        if (activity != null) {
            // 2. Activity의 ActionBar를 가져옵니다. (null일 수 있으므로 체크)
            ActionBar actionBar = activity.getSupportActionBar();
            if (actionBar != null) {
                // 3. ActionBar의 제목을 설정합니다.
                actionBar.setTitle("회원가입 약관 동의");
            }
        }
    }
}