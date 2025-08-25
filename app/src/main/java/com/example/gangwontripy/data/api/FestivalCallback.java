package com.example.gangwontripy.data.api;

import com.example.gangwontripy.data.model.FestivalItem;

import java.util.List;

public interface FestivalCallback {
    void onSuccess(List<FestivalItem> items);
    void onError(Exception e);
}
