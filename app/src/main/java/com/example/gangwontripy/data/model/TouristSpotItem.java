package com.example.gangwontripy.data.model;

import java.util.Objects;

public class TouristSpotItem {
    private String title;
    private String addr1;
    private String addr2;
    private String firstImage;   // firstimage
    private String firstImage2;  // firstimage2
    private String mapx;
    private String mapy;
    private String contentId;
    private String modifiedTime;

    public String getTitle() { return title; }
    public String getAddr1() { return addr1; }
    public String getAddr2() { return addr2; }
    public String getFirstImage() { return firstImage; }
    public String getFirstImage2() { return firstImage2; }
    public String getMapx() { return mapx; }
    public String getMapy() { return mapy; }
    public String getContentId() { return contentId; }
    public String getModifiedTime() { return modifiedTime; }

    public void setTitle(String title) { this.title = title; }
    public void setAddr1(String addr1) { this.addr1 = addr1; }
    public void setAddr2(String addr2) { this.addr2 = addr2; }
    public void setFirstImage(String firstImage) { this.firstImage = firstImage; }
    public void setFirstImage2(String firstImage2) { this.firstImage2 = firstImage2; }
    public void setMapx(String mapx) { this.mapx = mapx; }
    public void setMapy(String mapy) { this.mapy = mapy; }
    public void setContentId(String contentId) { this.contentId = contentId; }
    public void setModifiedTime(String modifiedTime) { this.modifiedTime = modifiedTime; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TouristSpotItem that = (TouristSpotItem) o;
        return Objects.equals(title, that.title) &&
                Objects.equals(addr1, that.addr1) &&
                Objects.equals(addr2, that.addr2) &&
                Objects.equals(firstImage, that.firstImage) &&
                Objects.equals(firstImage2, that.firstImage2) &&
                Objects.equals(mapx, that.mapx) &&
                Objects.equals(mapy, that.mapy) &&
                Objects.equals(contentId, that.contentId) &&
                Objects.equals(modifiedTime, that.modifiedTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(title, addr1, addr2, firstImage, firstImage2, mapx, mapy, contentId, modifiedTime);
    }
}
