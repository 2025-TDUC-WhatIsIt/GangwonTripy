package com.example.gangwontripy.ui.faq;

import android.os.Bundle;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.gangwontripy.R;
import com.example.gangwontripy.data.model.FaqItem;
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

    // 테스트를 위한 가짜 데이터 생성 메소드
    private List<FaqItem> createDummyData() {
        List<FaqItem> list = new ArrayList<>();
        list.add(new FaqItem("[시스템]", "강원 Tripy는 어떤 앱인가요?", "2025.08.13", "답변: 강원도 여행을 위한 최고의 앱입니다."));
        list.add(new FaqItem("[회원]", "회원 탈퇴를 하고 싶습니다.", "2025.08.13", "답변: 마이페이지 > 설정 > 회원 탈퇴 메뉴를 이용해주세요."));
        // ... (필요한 만큼 데이터 추가) ...
        return list;
    }
}