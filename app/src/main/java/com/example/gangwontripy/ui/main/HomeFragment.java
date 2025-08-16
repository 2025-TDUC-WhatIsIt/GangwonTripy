package com.example.gangwontripy.ui.main;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gangwontripy.R;
import com.example.gangwontripy.data.model.MarketItem;
import com.example.gangwontripy.ui.main.home.MarketAdapter;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment{

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState){
        // 레이아웃 파일을 View 객체로 변환(inflate)
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        // view 객체 반환 -> 시스템이 view를 화면에 그려줌
        return view;
    }

    // onCreateView 이후에 호출되는 메서드
    // 버튼 클릭 리스너 등 설정하면 됨
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState){
        super.onViewCreated(view, savedInstanceState);

        // --- 전통시장 섹션 초기화 ---
        RecyclerView marketRecyclerView = view.findViewById(R.id.recycler_view_market);

        // 1. 임시 데이터 생성
        List<MarketItem> marketDataList = new ArrayList<>();
        marketDataList.add(new MarketItem("강릉중앙시장", R.drawable.img_rectangle)); // R.drawable... 은 예시 이미지
        marketDataList.add(new MarketItem("속초관광수산시장", R.drawable.img_rectangle));
        marketDataList.add(new MarketItem("정선아리랑시장", R.drawable.img_rectangle));
        marketDataList.add(new MarketItem("동해동쪽바다중앙시장", R.drawable.img_rectangle));
        marketDataList.add(new MarketItem("원주중앙시장", R.drawable.img_rectangle));
        marketDataList.add(new MarketItem("춘천중앙시장", R.drawable.img_rectangle));

        // 2. 어댑터 생성 및 연결
        MarketAdapter marketAdapter = new MarketAdapter(marketDataList);
        marketRecyclerView.setAdapter(marketAdapter);

        // 3. 레이아웃 매니저 설정 (가로 스크롤)
        marketRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

        // 4. 페이지 단위 스크롤을 위한 PagerSnapHelper 부착 (🌟 핵심!)
        // 이 한 줄만으로 아이템들이 카드처럼 페이지 단위로 딱딱 맞게 스크롤됩니다.
        PagerSnapHelper snapHelper = new PagerSnapHelper();
        snapHelper.attachToRecyclerView(marketRecyclerView);

        // 5. 페이지 표시 점들(Dots Indicator) 설정은 고급 기능으로,
        // CircleIndicator 라이브러리를 사용하거나 직접 구현할 수 있습니다.
        // 여기서는 RecyclerView까지만 먼저 완성합니다.

    }
}
