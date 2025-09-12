package com.whatisit.gangwontripy.data.model;

public class FestivalItem {
    private String title;
    private String addr1;
    private String addr2;
    private String firstImage;   // firstimage
    private String firstImage2;  // firstimage2
    private String mapx;
    private String mapy;
    private String contentId;
    private String modifiedTime;
    private String eventStartDate; // yyyymmdd
    private String eventEndDate;   // yyyymmdd
    private String tel;            // <br> 포함 가능
    private String zipcode;

    public String getTitle() { return title; }
    public String getAddr1() { return addr1; }
    public String getAddr2() { return addr2; }
    public String getFirstImage() { return firstImage; }
    public String getFirstImage2() { return firstImage2; }
    public String getMapx() { return mapx; }
    public String getMapy() { return mapy; }
    public String getContentId() { return contentId; }
    public String getModifiedTime() { return modifiedTime; }
    public String getEventStartDate() { return eventStartDate; }
    public String getEventEndDate() { return eventEndDate; }
    public String getTel() { return tel; }
    public String getZipcode() { return zipcode; }

    public void setTitle(String title) { this.title = title; }
    public void setAddr1(String addr1) { this.addr1 = addr1; }
    public void setAddr2(String addr2) { this.addr2 = addr2; }
    public void setFirstImage(String firstImage) { this.firstImage = firstImage; }
    public void setFirstImage2(String firstImage2) { this.firstImage2 = firstImage2; }
    public void setMapx(String mapx) { this.mapx = mapx; }
    public void setMapy(String mapy) { this.mapy = mapy; }
    public void setContentId(String contentId) { this.contentId = contentId; }
    public void setModifiedTime(String modifiedTime) { this.modifiedTime = modifiedTime; }
    public void setEventStartDate(String eventStartDate) { this.eventStartDate = eventStartDate; }
    public void setEventEndDate(String eventEndDate) { this.eventEndDate = eventEndDate; }
    public void setTel(String tel) { this.tel = tel; }
    public void setZipcode(String zipcode) { this.zipcode = zipcode; }
}
