package com.example.gangwontripy.data.model;

public class MarketItem {
    private String name;
    private int imageUrl; // 실제로는 String imageUrl 이겠지만, 여기서는 drawable 리소스를 사용합니다.

    public MarketItem(String name, int imageUrl) {
        this.name = name;
        this.imageUrl = imageUrl;
    }

    public String getName() {
        return name;
    }

    public int getImageUrl() {
        return imageUrl;
    }
}
