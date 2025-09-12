package com.whatisit.gangwontripy.data.api;

import com.whatisit.gangwontripy.data.model.FestivalItem;

import java.util.List;

public interface FestivalCallback {
    void onSuccess(List<FestivalItem> items);
    void onError(Exception e);
}
