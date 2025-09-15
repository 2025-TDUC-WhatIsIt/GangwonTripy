package com.whatisit.gangwontripy.data.model;

public interface TimelineItem {
    int TYPE_YEAR = 0; // 연도 아이템 타입
    int TYPE_VISIT = 1; // 방문 기록 아이템 타입

    int getViewType(); // 자신의 뷰 타입을 반환하는 메소드
}