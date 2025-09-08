package com.example.gangwontripy.ui.notice;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gangwontripy.R;
import com.example.gangwontripy.data.model.NoticeItem;

import java.util.ArrayList;
import java.util.List;

public class NoticeFragment extends Fragment {

    public NoticeFragment() {super(R.layout.fragment_notice);}

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView recyclerView = view.findViewById(R.id.rv_notice);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // 가짜 데이터 생성
        List<NoticeItem> noticeList = createDummyNotice();

        NoticeAdapter adapter = new NoticeAdapter(noticeList);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onResume() {
        super.onResume();

        AppCompatActivity activity = (AppCompatActivity) getActivity();
        if (activity != null) {
            ActionBar actionBar = activity.getSupportActionBar();
            if (actionBar != null) {
                actionBar.setTitle("공지사항");
                actionBar.setDisplayHomeAsUpEnabled(true);
            }
        }
    }

    private List<NoticeItem> createDummyNotice() {
        List<NoticeItem> list = new ArrayList<>();
        list.add(new NoticeItem("주요공지", "이것은 공지제목", "2025.09.07", "이것은 공지 내용이다 어쩌구..."));
        return list;
    }
}
