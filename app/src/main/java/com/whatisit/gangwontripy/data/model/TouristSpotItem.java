package com.whatisit.gangwontripy.data.model;

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
    private String cat1;
    private String cat2;
    private String cat3;
    private String zipcode;
    private String tel;
    private String contentTypeId;
    private String areaCode;
    private String sigunguCode;
    private String mlevel;

    public String getSigunguCode() {
        return sigunguCode;
    }

    public void setSigunguCode(String sigunguCode) {
        this.sigunguCode = sigunguCode;
    }

    public String getMlevel() {
        return mlevel;
    }

    public void setMlevel(String mLevel) {
        this.mlevel = mLevel;
    }

    public String getCat1() {
        return cat1;
    }

    public void setCat1(String cat1) {
        this.cat1 = cat1;
    }

    public String getCat2() {
        return cat2;
    }

    public void setCat2(String cat2) {
        this.cat2 = cat2;
    }

    public String getCat3() {
        return cat3;
    }

    public void setCat3(String cat3) {
        this.cat3 = cat3;
    }

    public String getZipcode() {
        return zipcode;
    }

    public void setZipcode(String zipcode) {
        this.zipcode = zipcode;
    }

    public String getTel() {
        return tel;
    }

    public void setTel(String tel) {
        this.tel = tel;
    }

    public String getContentTypeId() {
        return contentTypeId;
    }

    public void setContentTypeId(String contentTypeId) {
        this.contentTypeId = contentTypeId;
    }

    public String getAreaCode() {
        return areaCode;
    }

    public void setAreaCode(String areaCode) {
        this.areaCode = areaCode;
    }

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
