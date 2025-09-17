// app/java/.../ui/auth/RegisterFragment.java
package com.whatisit.gangwontripy.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.*;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.whatisit.gangwontripy.R;
import com.whatisit.gangwontripy.data.api.AuthApi;
import com.whatisit.gangwontripy.data.api.ApiClient;
import com.whatisit.gangwontripy.data.model.LoginRes;
import com.whatisit.gangwontripy.data.model.SignupReq;

import org.json.JSONObject;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterFragment extends Fragment {

    private EditText etUsername, etPassword, etPassword2, etNickname;
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

        // (선택) 레이아웃에 ProgressBar 추가했다면 id 연결

        authApi = ApiClient.authApi();

        registAccountButton.setOnClickListener(view -> {
            String email = safe(etUsername.getText()); // = email
            String password = safe(etPassword.getText());
            String password2 = safe(etPassword2.getText());
            String nickname = safe(etNickname.getText());

            // 1) 간단 유효성
            if (TextUtils.isEmpty(email)) { toast("아이디(이메일)를 입력해 주세요."); return; }
            if (TextUtils.isEmpty(password)) { toast("비밀번호를 입력해 주세요."); return; }
            if (!password.equals(password2)) { toast("비밀번호가 일치하지 않습니다."); return; }
            if (password.length() < 6) { toast("비밀번호는 6자 이상 권장합니다."); return; }
            if (TextUtils.isEmpty(nickname)) { toast("닉네임을 입력해 주세요."); return; }

            // 2) 요청
            setLoading(true);
            SignupReq req = new SignupReq(email, password, nickname);
            authApi.signup(req).enqueue(new Callback<LoginRes>() {
                @Override
                public void onResponse(Call<LoginRes> call, Response<LoginRes> res) {
                    if (res.isSuccessful() && res.body() != null) {
                        LoginRes body = res.body();
                        // 성공 시 다음 화면으로 이동(기존 로직 유지)
                        if (getActivity() instanceof LoginActivity) {
                            ((LoginActivity) getActivity()).onLoginSuccess(body);
                        }
                        // 혹시 화면에 남아있더라도 버튼/로딩 복구
                        setLoading(false);
                    } else {
                        // ❗️실패 사유 상세 표시 + 버튼 재활성화
                        toast(extractErrorMessage(res));
                        setLoading(false);
                    }
                }

                @Override
                public void onFailure(Call<LoginRes> call, Throwable t) {
                    toast("네트워크 오류: " + (t.getMessage() == null ? "알 수 없음" : t.getMessage()));
                    setLoading(false); // ❗️버튼 재활성화
                }
            });
        });
    }

    private String extractErrorMessage(Response<?> res) {
        String fallback;
        switch (res.code()) {
            case 409: fallback = "이미 사용 중인 이메일입니다."; break;
            case 400: fallback = "요청 형식이 올바르지 않습니다."; break;
            case 401: fallback = "인증에 실패했습니다."; break;
            case 403: fallback = "권한이 없습니다."; break;
            case 500: fallback = "서버 오류가 발생했습니다."; break;
            default:  fallback = "회원가입 실패 (" + res.code() + ")"; break;
        }

        try {
            if (res.errorBody() != null) {
                String raw = res.errorBody().string();
                if (!TextUtils.isEmpty(raw)) {
                    // Spring 기본 에러 포맷: { "timestamp":..., "status":409, "error":"Conflict", "message":"...", "path":... }
                    try {
                        JSONObject obj = new JSONObject(raw);
                        // 우선순위: message > detail > error > raw
                        if (obj.has("message") && !obj.isNull("message")) {
                            String m = obj.optString("message", "");
                            if (!TextUtils.isEmpty(m)) return m;
                        }
                        if (obj.has("detail") && !obj.isNull("detail")) {
                            String d = obj.optString("detail", "");
                            if (!TextUtils.isEmpty(d)) return d;
                        }
                        if (obj.has("error") && !obj.isNull("error")) {
                            String e = obj.optString("error", "");
                            if (!TextUtils.isEmpty(e)) return e;
                        }
                    } catch (Exception ignore) {
                        // JSON 아니면 그대로 일부만 보여주기
                        if (raw.length() > 0) return raw.length() > 120 ? raw.substring(0, 120) + "…" : raw;
                    }
                }
            }
        } catch (IOException ignored) { }

        return fallback;
    }

    private void setLoading(boolean on) {
        if (registAccountButton != null) registAccountButton.setEnabled(!on);
        if (etUsername != null) etUsername.setEnabled(!on);
        if (etPassword != null) etPassword.setEnabled(!on);
        if (etPassword2 != null) etPassword2.setEnabled(!on);
        if (etNickname != null) etNickname.setEnabled(!on);
    }

    private void toast(String msg) {
        if (getContext() != null) Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
    }

    private static String safe(CharSequence cs) {
        return cs == null ? "" : cs.toString().trim();
    }
}
