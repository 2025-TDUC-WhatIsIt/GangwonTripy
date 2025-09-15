package com.whatisit.gangwontripy.data.model;

public class BadgeItem {
    private String name;
    private String description;
    private int imageResourceId; // drawable 리소스 ID
    private boolean isAcquired; // 획득 여부

    public BadgeItem(String name, String description, int imageResourceId, boolean isAcquired) {
        this.name = name;
        this.description = description;
        this.imageResourceId = imageResourceId;
        this.isAcquired = isAcquired;
    }
    // 모든 필드에 대한 Getter 생성
    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public int getImageResourceId() {
        return imageResourceId;
    }

    public boolean isAcquired() {
        return isAcquired;
    }
}
