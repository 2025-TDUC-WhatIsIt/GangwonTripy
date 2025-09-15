// app/java/.../ui/auth/RegisterFragment.java
package com.whatisit.gangwontripy.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.*;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.whatisit.gangwontripy.R;
import com.whatisit.gangwontripy.data.api.AuthApi;
import com.whatisit.gangwontripy.data.api.ApiClient; // ← 기존 프로젝트에 있는 Retrofit 빌더 사용
import com.whatisit.gangwontripy.data.model.LoginRes;
import com.whatisit.gangwontripy.data.model.SignupReq;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterFragment extends Fragment {

    private EditText etUsername, etPassword, etPassword2, etNickname;
    private View progress;
    private Button registAccountButton;
    private AuthApi authApi;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_register, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle s) {
        super.onViewCreated(v, s);

        etUsername = v.findViewById(R.id.edit_text_register_id);
        etPassword = v.findViewById(R.id.edit_text_register_pw);
        etPassword2 = v.findViewById(R.id.edit_text_register_pw2);
        etNickname = v.findViewById(R.id.edit_text_register_nickname);
        registAccountButton = v.findViewById(R.id.regist_account_btn);

        authApi = ApiClient.authApi();

        registAccountButton.setOnClickListener(view -> {
            String username = safe(etUsername.getText());
            String password = safe(etPassword.getText());
            String password2 = safe(etPassword2.getText());
            String nickname = safe(etNickname.getText());

            // 1) 간단 유효성
            if (TextUtils.isEmpty(username)) { toast("아이디를 입력해 주세요."); return; }
            if (TextUtils.isEmpty(password)) { toast("비밀번호를 입력해 주세요."); return; }
            if (!password.equals(password2)) { toast("비밀번호가 일치하지 않습니다."); return; }
            if (password.length() < 6) { toast("비밀번호는 6자 이상 권장합니다."); return; }
            if (TextUtils.isEmpty(nickname)) { toast("닉네임을 입력해 주세요."); return; }

            // 2) 요청
            setLoading(true);
            SignupReq req = new SignupReq(username, password, nickname);
            android.util.Log.d("SIGNUPREQ", "SignupReq" + req);
            authApi.signup(req).enqueue(new Callback<LoginRes>() {
                @Override
                public void onResponse(Call<LoginRes> call, Response<LoginRes> res) {
                    if (res.isSuccessful() && res.body() != null) {
                        LoginRes body = res.body();
                        if (getActivity() instanceof LoginActivity) {
                            ((LoginActivity) getActivity()).onLoginSuccess(body);
                        }
                    } else {
                        Toast.makeText(requireContext(), "회원가입 실패", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<LoginRes> call, Throwable t) {
                    Toast.makeText(requireContext(), "네트워크 오류: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private void goLoginAndFinish() {
        if (getActivity() == null) return;

        // 1) 같은 Activity 내에서 백스택으로 이동했다면: 뒤로가기처럼 로그인 화면으로 복귀
        if (getParentFragmentManager().getBackStackEntryCount() > 0) {
            getParentFragmentManager().popBackStack();
            getParentFragmentManager().popBackStack();
            return;
        }

        // 2) 백스택이 없다면: LoginActivity로 이동 (클래스명은 프로젝트에 맞게 변경)
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        // 이미 로그인 화면이 Task 뒤에 있다면 그 위의 화면을 정리하고 복귀
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);

        // 현재 화면 종료
        getActivity().finish();
    }
    private void setLoading(boolean on) {
        if (progress != null) progress.setVisibility(on ? View.VISIBLE : View.GONE);
        if (registAccountButton != null) registAccountButton.setEnabled(!on);
    }

    private void toast(String msg) {
        if (getContext() != null) Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
    }

    private static String safe(CharSequence cs) {
        return cs == null ? "" : cs.toString().trim();
    }

//    @Override
//    public void onResume() {
//        super.onResume();
//        AppCompatActivity activity = (AppCompatActivity) getActivity();
//        if (activity != null) {
//            ActionBar actionBar = activity.getSupportActionBar();
//            if (actionBar != null) actionBar.setTitle("회원정보 입력하기");
//        }
//    }
}
