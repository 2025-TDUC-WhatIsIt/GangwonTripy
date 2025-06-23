package com.example.gangwontripy.ui.main;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.gangwontripy.R;

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


    }
}
