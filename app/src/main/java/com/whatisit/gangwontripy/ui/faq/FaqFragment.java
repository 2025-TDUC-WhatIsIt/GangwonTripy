package com.whatisit.gangwontripy.ui.faq;

import android.os.Bundle;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.whatisit.gangwontripy.R;
import com.whatisit.gangwontripy.data.model.FaqItem;
import java.util.ArrayList;
import java.util.List;

public class FaqFragment extends Fragment {

    public FaqFragment() {
        super(R.layout.fragment_faq); // fragment_faq.xml 레이아웃 파일 지정
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView recyclerView = view.findViewById(R.id.rv_faq);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // 가짜 데이터 생성 (실제로는 서버에서 받아와야 함)
        List<FaqItem> faqList = createDummyData();

        FaqAdapter adapter = new FaqAdapter(faqList);
        recyclerView.setAdapter(adapter);

        // 구분선 추가 (XML의 View 대신 이 방법을 사용하면 더 깔끔)
        // recyclerView.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));
    }

    @Override
    public void onResume() {
        super.onResume();

        // 이 Fragment가 화면에 보일 때마다 툴바를 설정합니다.
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        if (activity != null) {
            ActionBar actionBar = activity.getSupportActionBar();
            if (actionBar != null) {
                // 1. 툴바 제목 설정
                actionBar.setTitle("자주 묻는 질문");

                // 2. 뒤로가기 버튼 표시 (NavigationUI가 자동으로 해주지만, 명시적으로 하는 것이 더 안전)
                actionBar.setDisplayHomeAsUpEnabled(true);
            }
        }
    }

    // 테스트를 위한 가짜 데이터 생성 메소드
    private List<FaqItem> createDummyData() {
        List<FaqItem> list = new ArrayList<>();
        list.add(new FaqItem("시스템", "강원 Tripy는 어떤 앱인가요?", "2025.08.13", getString(R.string.faq_content_system)));
        list.add(new FaqItem("회원", "회원 탈퇴를 하고싶습니다.", "2025.08.13", getString(R.string.faq_content_withdrawal)));
        list.add(new FaqItem("회원", "연결된 전화번호나 이메일을 변경하고 싶습니다.", "2025.08.13", getString(R.string.faq_content_change_info)));
        list.add(new FaqItem("오류", "QR 인식이 되지 않습니다.", "2025.08.13", getString(R.string.faq_content_qr_error)));
        list.add(new FaqItem("오류", "앱 사용 중 강제로 종료되거나 원활하게 접속되지 않습니다.", "2025.08.13", getString(R.string.faq_content_app_error)));
        list.add(new FaqItem("기타", "가맹점으로 등록되어있어 방문했는데, 지역화폐를 받아주지 않는 경우 어떻게 해야하나요?", "2025.08.13", getString(R.string.faq_content_payment_error)));

        return list;
    }
}