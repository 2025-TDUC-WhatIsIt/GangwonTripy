package com.whatisit.gangwontripy.data.model;

public class VisitItem implements TimelineItem {
    private String placeName;
    private String date;
    // 필요하다면 이미지 URL 등 다른 정보도 추가

    public VisitItem(String placeName, String date) {
        this.placeName = placeName;
        this.date = date;
    }

    public String getPlaceName() {
        return placeName;
    }

    public String getDate() {
        return date;
    }

    @Override
    public int getViewType() {
        return TYPE_VISIT;
    }
}