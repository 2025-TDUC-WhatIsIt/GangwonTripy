package com.whatisit.gangwontripy.data.model;

public class FaqItem {
    private String category;
    private String title;
    private String date;
    private String content; // 숨겨져 있을 답변 내용
    private boolean isExpanded = false; // 펼쳐짐 상태를 저장하는 변수, 기본값은 false

    public FaqItem(String category, String title, String date, String content) {
        this.category = category;
        this.title = title;
        this.date = date;
        this.content = content;
    }

    // --- 모든 필드에 대한 Getter와 Setter를 만들어주세요 ---
    public String getCategory() { return category; }
    public String getTitle() { return title; }
    public String getDate() { return date; }
    public String getContent() { return content; }
    public boolean isExpanded() { return isExpanded; }
    public void setExpanded(boolean expanded) { isExpanded = expanded; }
}