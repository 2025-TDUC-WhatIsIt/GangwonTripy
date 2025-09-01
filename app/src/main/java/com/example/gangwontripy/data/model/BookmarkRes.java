package com.example.gangwontripy.data.model;

import java.util.Map;

public class BookmarkRes {
    public Long id;
    public String provider;      // "TOURAPI"
    public String externalId;    // contentid
    public String placeName;
    public String address;
    public String category;
    public String thumbnailUrl;
    public String createdAt;
    public Double lat;
    public Double lng;
    public Map<String,Object> snapshotJson;
}
