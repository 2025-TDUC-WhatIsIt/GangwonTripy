package com.whatisit.gangwontripy.data.api;

import com.whatisit.gangwontripy.data.model.TouristSpotItem;

import java.util.List;

public interface TourCallback {
    void onSuccess(List<TouristSpotItem> items);
    void onError(Exception e);
}

