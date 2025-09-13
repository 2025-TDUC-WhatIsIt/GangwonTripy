package com.whatisit.gangwontripy.ui.auth;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.whatisit.gangwontripy.R;

public class TermsFragment extends Fragment {

    private CheckBox checkboxAgreeAll;
    private CheckBox checkboxAgreeTerms;
    private CheckBox checkboxAgreePersonal;
    private CheckBox checkboxAgreeLbs;

    // 무한 루프 방지를 위한 리스너 변수
    private CompoundButton.OnCheckedChangeListener agreeAllListener;
    private CompoundButton.OnCheckedChangeListener individualListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // fragment_terms.xml 레이아웃을 inflate하여 View 객체를 생성하고 반환함
        return inflater.inflate(R.layout.fragment_terms, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        checkboxAgreeAll = view.findViewById(R.id.checkbox_agree_all);
        checkboxAgreeTerms = view.findViewById(R.id.checkbox_agree_terms);
        checkboxAgreePersonal = view.findViewById(R.id.checkbox_agree_personal);
        checkboxAgreeLbs = view.findViewById(R.id.checkbox_agree_lbs);

        ConstraintLayout layoutAgreeAll = view.findViewById(R.id.layout_agree_all);
        ConstraintLayout layoutAgreeTerms = view.findViewById(R.id.layout_agree_terms);
        ConstraintLayout layoutAgreePersonal = view.findViewById(R.id.layout_agree_personal);
        ConstraintLayout layoutAgreeLbs = view.findViewById(R.id.layout_agree_lbs);

        ImageButton btnViewTerms = view.findViewById(R.id.btn_view_terms); // '이용약관' 상세 보기 버튼
        ImageButton btnViewPrivacy = view.findViewById(R.id.btn_view_personal); // '개인정보' 상세 보기 버튼 ID
        ImageButton btnViewLbs = view.findViewById(R.id.btn_view_lbs);
        Button createAccountButton = view.findViewById(R.id.create_account_btn); // '계정 생성하기' 버튼

        // 1. "전체 동의" 레이아웃 클릭 시
        layoutAgreeAll.setOnClickListener(v -> {
            boolean isChecked = !checkboxAgreeAll.isChecked();
            checkboxAgreeAll.setChecked(isChecked);
            // 모든 자식 체크박스 상태를 '전체 동의'와 동일하게 설정
            checkboxAgreeTerms.setChecked(isChecked);
            checkboxAgreePersonal.setChecked(isChecked);
            checkboxAgreeLbs.setChecked(isChecked);
        });

        // 2. "이용약관" 레이아웃 클릭 시
        layoutAgreeTerms.setOnClickListener(v -> {
            checkboxAgreeTerms.toggle(); // toggle()은 현재 상태를 반전시킴
            updateAgreeAllCheckboxState();
        });

        // 3. "개인정보" 레이아웃 클릭 시
        layoutAgreePersonal.setOnClickListener(v -> {
            checkboxAgreePersonal.toggle();
            updateAgreeAllCheckboxState();
        });

        // 4. "위치기반" 레이아웃 클릭 시
        layoutAgreeLbs.setOnClickListener(v -> {
            checkboxAgreeLbs.toggle();
            updateAgreeAllCheckboxState();
        });

        // "이용약관 동의"의 상세 보기 버튼 클릭 리스너
        btnViewTerms.setOnClickListener(v -> {
            // '약관' 문서를 보여달라는 정보를 Bundle에 담습니다.
            Bundle bundle = new Bundle();
            bundle.putString("document_type", "TERMS");

            // nav_graph.xml에 정의한 action을 따라 Bundle과 함께 이동합니다.
            NavHostFragment.findNavController(TermsFragment.this)
                    .navigate(R.id.action_termsFragment_to_documentFragment, bundle);
        });

        // "개인정보 수집 및 이용 동의"의 상세 보기 버튼 클릭 리스너
        btnViewPrivacy.setOnClickListener(v -> {
            // '개인정보' 문서를 보여달라는 정보를 Bundle에 담습니다.
            Bundle bundle = new Bundle();
            bundle.putString("document_type", "PRIVACY");

            NavHostFragment.findNavController(TermsFragment.this)
                    .navigate(R.id.action_termsFragment_to_documentFragment, bundle);
        });

        // "위치기반 서비스 이용약관"의 상세 보기 버튼 클릭 리스너
        btnViewLbs.setOnClickListener(v -> {
            // '위치기반 서비스 이용약관' 문서를 보여달라는 정보를 Bundle에 담습니다.
            Bundle bundle = new Bundle();
            bundle.putString("document_type", "LBS");

            NavHostFragment.findNavController(TermsFragment.this)
                    .navigate(R.id.action_termsFragment_to_documentFragment, bundle);
        });

        // "계정 생성하기" Button 클릭 리스너
        createAccountButton.setOnClickListener(v -> {
            // 필수 약관에 모두 동의했는지 확인하는 메소드를 호출
            if (areAllRequiredTermsAgreed()) {
                // 모두 동의했다면 다음 화면으로 이동
                NavHostFragment.findNavController(TermsFragment.this)
                        .navigate(R.id.action_termsFragment_to_registerFragment);
            } else {
                // 하나라도 동의하지 않았다면 사용자에게 알림 (Toast 메시지)
                Toast.makeText(getContext(), "모든 필수 약관에 동의해주세요.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // '전체 동의' 체크박스의 상태를 업데이트하는 메소드
    private void updateAgreeAllCheckboxState() {
        // 모든 필수 항목이 체크되었는지 확인
        boolean allRequiredAgreed = areAllRequiredTermsAgreed();

        // '전체 동의'의 리스너를 잠시 해제 (무한 루프 방지)
        checkboxAgreeAll.setOnCheckedChangeListener(null);
        // 확인된 상태로 '전체 동의' 체크박스 상태 설정
        checkboxAgreeAll.setChecked(allRequiredAgreed);
        // '전체 동의' 리스너를 다시 연결
        checkboxAgreeAll.setOnCheckedChangeListener(agreeAllListener);
    }

    // 모든 '필수' 약관에 동의했는지 확인하는 메소드
    private boolean areAllRequiredTermsAgreed() {
        // isChecked() 메소드는 체크박스가 선택되었으면 true, 아니면 false를 반환합니다.
        // '&&' (AND 연산자)를 사용하여 모든 조건이 true일 때만 전체 결과가 true가 되도록 합니다.
        return checkboxAgreeTerms.isChecked() &&
                checkboxAgreePersonal.isChecked() &&
                checkboxAgreeLbs.isChecked();
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