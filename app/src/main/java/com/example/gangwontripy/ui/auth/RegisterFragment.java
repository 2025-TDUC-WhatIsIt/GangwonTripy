package com.example.gangwontripy.ui.auth;

import android.content.Intent;
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

import com.example.gangwontripy.MainActivity;
import com.example.gangwontripy.R;

public class RegisterFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // fragment_terms.xml 레이아웃을 inflate하여 View 객체를 생성하고 반환함
        return inflater.inflate(R.layout.fragment_register, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Button registAccountButton = view.findViewById(R.id.regist_account_btn);

        // "회원가입하기" Button 클릭 리스너
        registAccountButton.setOnClickListener(v -> {
            // TODO: 아이디, 비밀번호, 닉네임 유효성 검사 및 서버에 회원가입 요청

            // 회원가입 성공 시
            // MainActivity로 이동하고 현재 LoginActivity는 종료
            Intent intent = new Intent(getActivity(), MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            getActivity().finish();
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
                actionBar.setTitle("회원정보 입력하기");
            }
        }
    }
}
