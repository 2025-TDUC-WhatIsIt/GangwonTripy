package com.whatisit.gangwontripy.ui.mypage.visitlog;
import com.whatisit.gangwontripy.R;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.whatisit.gangwontripy.data.model.TimelineItem;
import com.whatisit.gangwontripy.data.model.VisitItem;
import com.whatisit.gangwontripy.data.model.YearItem;

import java.util.ArrayList;
import java.util.List;

public class VisitLogFragment extends Fragment {

    public VisitLogFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_visit_log, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView recyclerView = view.findViewById(R.id.rv_visit_log);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // TODO: 실제로는 서버에서 방문 기록 데이터를 받아와서
        //       연도별로 그룹화하는 로직이 필요합니다.
        List<TimelineItem> timelineItems = createDummyData();
//        Log.d("VisitLogFragment", "Dummy data count: " + timelineItems.size());

        VisitLogAdapter adapter = new VisitLogAdapter(timelineItems);
        recyclerView.setAdapter(adapter);
    }

    // 서버 데이터를 받아와서 타임라인 형태로 가공하는 메소드 (예시)
    private List<TimelineItem> createDummyData() {
        List<TimelineItem> list = new ArrayList<>();
        // 실제 데이터 가공 로직: 날짜 순으로 정렬 -> 연도가 바뀔 때마다 YearItem 추가
        list.add(new VisitItem("장소 이름 (큐알코드 이름?)", "2025.08.10"));
        list.add(new YearItem("2024"));
        list.add(new VisitItem("또 다른 장소", "2024.12.25"));
        list.add(new VisitItem("옛날 장소", "2024.01.01"));
        return list;
    }

    @Override
    public void onResume() {
        super.onResume();
    }
}