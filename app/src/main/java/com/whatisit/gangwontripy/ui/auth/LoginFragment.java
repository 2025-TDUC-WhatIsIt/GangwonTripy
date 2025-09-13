package com.whatisit.gangwontripy.ui.auth;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.whatisit.gangwontripy.R;
import com.whatisit.gangwontripy.data.api.ApiClient;
import com.whatisit.gangwontripy.data.model.LoginReq;
import com.whatisit.gangwontripy.data.model.LoginRes;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginFragment extends Fragment {
    private EditText etId, etPw;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState){
        // fragment_login.xml 레이아웃을 inflate하여 View 객체를 생성하고 반환함
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState){
        super.onViewCreated(view, savedInstanceState);

        etId = view.findViewById(R.id.login_id);          // ← 레이아웃 ID에 맞게 수정
        etPw = view.findViewById(R.id.login_pw);    // ← 레이아웃 ID에 맞게 수정

        Button loginButton = view.findViewById(R.id.login_btn);
        ImageButton kakaoLoginButton = view.findViewById(R.id.login_kakao);
        TextView registerButton = view.findViewById(R.id.register);
        TextView findAccountButton = view.findViewById(R.id.find_account);
        TextView inquiryButton = view.findViewById(R.id.inquiry);

        loginButton.setOnClickListener(v -> doLogin());

        kakaoLoginButton.setOnClickListener(v -> {
            // TODO: 카카오 로그인 연동
        });

        registerButton.setOnClickListener(v -> {
//            if (getActivity() instanceof LoginActivity){
//                ((LoginActivity) getActivity()).navigateToTerms();
//            }
            NavHostFragment.findNavController(LoginFragment.this)
                    .navigate(R.id.action_loginFragment_to_termsFragment);
        });
    }

    private void doLogin() {
        String id = etId != null ? etId.getText().toString().trim() : "";
        String pw = etPw != null ? etPw.getText().toString().trim() : "";

        if (id.isEmpty() || pw.isEmpty()) {
            Toast.makeText(requireContext(), "아이디/비밀번호를 입력하세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        ApiClient.authApi().login(new LoginReq(id, pw)).enqueue(new Callback<LoginRes>() {
            @Override
            public void onResponse(Call<LoginRes> call, Response<LoginRes> res) {
                if (res.isSuccessful() && res.body() != null) {
                    LoginRes body = res.body();
                    if (getActivity() instanceof LoginActivity) {
                        ((LoginActivity) getActivity()).onLoginSuccess(body);
                    }
                } else {
                    Toast.makeText(requireContext(), "로그인 실패", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<LoginRes> call, Throwable t) {
                Toast.makeText(requireContext(), "네트워크 오류: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
