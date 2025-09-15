// MyPageFragment.java
package com.whatisit.gangwontripy.ui.mypage;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.whatisit.gangwontripy.R;
import com.whatisit.gangwontripy.core.SessionManager;
import com.whatisit.gangwontripy.data.api.ApiService;
import com.whatisit.gangwontripy.ui.splash.SplashActivity;
import com.bumptech.glide.Glide;

public class MyPageFragment extends Fragment {

    private TextView textBadgeCount;
    private TextView textMyTitleCount;
    private TextView textVisitLogCount;
    private TextView textName;
    private TextView textNickname;
    private ImageView imageProfile;

    private ApiService api;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState){
        return inflater.inflate(R.layout.fragment_my_page, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState){
        super.onViewCreated(view, savedInstanceState);

        LinearLayout layoutBadge = view.findViewById(R.id.layout_badge);
        LinearLayout layoutVisitLog = view.findViewById(R.id.layout_visit_log);

        TextView noticeTextView = view.findViewById(R.id.menu_notice);
        TextView faqTextView = view.findViewById(R.id.menu_faq);
        TextView termsTextView = view.findViewById(R.id.menu_terms);
        TextView privacyTextView = view.findViewById(R.id.menu_privacy);

        layoutBadge.setOnClickListener(v -> {
            NavHostFragment.findNavController(MyPageFragment.this)
                    .navigate(R.id.action_myPageFragment_to_badgeFragment);
        });

        layoutVisitLog.setOnClickListener(v -> {
            NavHostFragment.findNavController(MyPageFragment.this)
                    .navigate(R.id.action_myPageFragment_to_visitLogFragment);
        });
      
        noticeTextView.setOnClickListener(v -> {
            NavHostFragment.findNavController(MyPageFragment.this)
                    .navigate(R.id.action_myPageFragment_to_noticeFragment);
        });

        faqTextView.setOnClickListener(v -> {
            NavHostFragment.findNavController(MyPageFragment.this)
                    .navigate(R.id.action_myPageFragment_to_faqFragment);
        });
      
        termsTextView.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putString("document_type", "TERMS");
            NavHostFragment.findNavController(MyPageFragment.this)
                    .navigate(R.id.action_myPageFragment_to_documentFragment, bundle);
        });

        privacyTextView.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putString("document_type", "PRIVACY");
            NavHostFragment.findNavController(MyPageFragment.this)
                    .navigate(R.id.action_myPageFragment_to_documentFragment, bundle);
        });

        // ✅ 뷰 바인딩
        imageProfile     = view.findViewById(R.id.image_profile);
        textName         = view.findViewById(R.id.text_name);
        textNickname     = view.findViewById(R.id.text_nickname);
        textBadgeCount   = view.findViewById(R.id.text_badge_count);
        textMyTitleCount = view.findViewById(R.id.text_my_title_count);
        textVisitLogCount= view.findViewById(R.id.text_visit_log_count);

        // ✅ ApiService 준비
        api = new ApiService(requireContext());

        // ✅ 프로필(닉네임/이미지) 바인딩
        bindProfile();

        // ✅ 카운트 불러오기
        fetchCounts();

        // ✅ 로그아웃
        TextView logoutTextView = view.findViewById(R.id.menu_logout);
        logoutTextView.setOnClickListener(v -> showLogoutDialog());
    }

    private void bindProfile() {
        SessionManager sm = SessionManager.getInstance(requireContext());

        String nickname = sm.getNickname();
        if (TextUtils.isEmpty(nickname)) nickname = "게스트";
        textName.setText(nickname);
        // text_nickname은 “칭호” 영역처럼 쓰고 싶으면 서버에서 칭호명을 넣어주면 좋음.
        // 우선 닉네임 보조 표기로 둡니다.
        //textNickname.setText("(" + nickname + ")");

        String profileUrl = sm.getProfileImageUrl();
        if (!TextUtils.isEmpty(profileUrl)) {
            Glide.with(this)
                 .load(profileUrl)
                 .placeholder(R.drawable.image_gone)
                 .circleCrop()
                 .into(imageProfile);
        } else {
            imageProfile.setImageResource(R.drawable.image_gone);
        }
    }

    private void setTitle(String title) {
        textNickname.setText("("+ title + ")");
    }
    private void fetchCounts() {
        // 기본값 미리 0개로
        setCounts(0, 0, 0);
        setTitle("없음");
        api.fetchMyPageSummary(new ApiService.Callback<ApiService.MyPageSummary>() {
            @Override public void onSuccess(ApiService.MyPageSummary s) {
                setCounts(s.badgeCount, s.titleCount, s.visitCount);
                setTitle(s.currentTitle);
            }
            @Override public void onError(Exception e) {
                // 실패 시 0개 유지
                setCounts(0, 0, 0);
                setTitle("없음");
                // 필요하면 Toast로 알려도 됨
                // Toast.makeText(requireContext(), "요약 불러오기 실패", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setCounts(int badge, int title, int visit) {
        textBadgeCount.setText(badge + "개");
        textMyTitleCount.setText(title + "개");
        textVisitLogCount.setText(visit + "개");
    }

    private void showLogoutDialog() {
        new AlertDialog.Builder(getContext())
                .setTitle("로그아웃")
                .setMessage("로그아웃 하시겠습니까?")
                .setPositiveButton("예", (dialog, which) -> performLogout())
                .setNegativeButton("아니오", null)
                .show();
    }

    private void performLogout(){
        // a) 로그인 플래그
        SharedPreferences sp = requireActivity().getSharedPreferences("login_prefs", Context.MODE_PRIVATE);
        sp.edit().putBoolean("is_logged_in", false).apply();

        // b) 세션(서버 토큰/유저정보)도 정리
        SessionManager.getInstance(requireContext()).clear();

        // c) 스플래시로 이동
        Intent intent = new Intent(getActivity(), SplashActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);

        requireActivity().finish();
    }
}
