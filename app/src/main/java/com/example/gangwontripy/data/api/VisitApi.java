package com.example.gangwontripy.data.api;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.*;

public interface VisitApi {
    @POST("/api/visits/claim")
    Call<VisitClaimResponse> claim(@Header("X-USER-ID") long userId, @Body VisitClaimRequest body);

    class VisitClaimRequest {
        public String token;
        public Double deviceLat;
        public Double deviceLng;
        public VisitClaimRequest(String token, Double lat, Double lng){
            this.token = token; this.deviceLat = lat; this.deviceLng = lng;
        }
    }
    class Tier {
        public String code, title, subtitle, imageUrl;
        public int minVisitCount;
    }
    class VisitClaimResponse {
        public boolean success;
        public long totalVisits;
        public Tier currentTier, nextTier;
        public List<String> newlyUnlocked;
        public String message;
    }
}
