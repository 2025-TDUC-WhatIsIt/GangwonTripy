package com.example.gangwontripy.data.api;

import com.example.gangwontripy.data.model.FestivalItem;
import com.example.gangwontripy.data.model.TouristSpotItem;

import java.util.List;

public interface TourCallback {
    void onSuccess(List<TouristSpotItem> items);
    void onError(Exception e);
}

