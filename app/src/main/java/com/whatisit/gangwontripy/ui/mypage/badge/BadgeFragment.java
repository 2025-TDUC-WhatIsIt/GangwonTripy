package com.whatisit.gangwontripy.ui.mypage.badge;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.whatisit.gangwontripy.data.model.BadgeItem;
import com.whatisit.gangwontripy.R;

import java.util.ArrayList;
import java.util.List;

public class BadgeFragment extends Fragment implements BadgeAdapter.OnBadgeClickListener {

    public BadgeFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_badge, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView recyclerView = view.findViewById(R.id.rv_badges);
        // GridLayoutManager를 사용하여 3열 격자 뷰 생성
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));

        List<BadgeItem> badgeList = createDummyBadges(); // TODO: 실제 데이터 가져오기
//        Log.d("BadgeFragment", "Dummy badge count: " + badgeList.size());

        BadgeAdapter adapter = new BadgeAdapter(badgeList, this); // this를 리스너로 전달
        recyclerView.setAdapter(adapter);
    }

    // 어댑터에서 배지가 클릭되면 이 메소드가 호출됨
    @Override
    public void onBadgeClick(BadgeItem badge) {
        BadgeDetailDialogFragment dialog = BadgeDetailDialogFragment.newInstance(badge);
        dialog.show(getParentFragmentManager(), "BadgeDetailDialog");
    }

    private List<BadgeItem> createDummyBadges() {
        List<BadgeItem> list = new ArrayList<>();
        // R.drawable.ic_badge_quest_mania는 res/drawable 폴더에 있는 이미지 파일의 ID입니다.
        list.add(new BadgeItem("퀘스트 매니아", "10개의 퀘스트 완료!", R.drawable.temp_ic_badge_dummy1, true));
        list.add(new BadgeItem("강릉 정복자", "강릉의 모든 핫플 방문!", R.drawable.temp_ic_badge_dummy2, false));
        list.add(new BadgeItem("미식가", "맛집 20곳 방문!", R.drawable.temp_ic_badge_dummy3, true));
        return list;
    }

}