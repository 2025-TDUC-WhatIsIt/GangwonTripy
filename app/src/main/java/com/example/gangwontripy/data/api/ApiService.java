package com.example.gangwontripy.data.api;

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.Nullable;

import com.example.gangwontripy.data.model.BookmarkRes;
import com.example.gangwontripy.data.model.FestivalItem;
import com.example.gangwontripy.data.model.TouristSpotItem;

import org.json.JSONArray;
import org.json.JSONObject;
import org.xmlpull.v1.*;
import com.example.gangwontripy.BuildConfig;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.*;

import okhttp3.*;
import okhttp3.logging.HttpLoggingInterceptor;

public class ApiService {

    // 공공데이터 서버가 아니라 스프링 서버를 베이스
    private static final String API_BASE = BuildConfig.API_BASE;
    public static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    private final OkHttpClient ok = new OkHttpClient();
    // 앱에서 쓸 사용자 ID (임시 하드코딩 / 로그인 붙이면 교체)
    private static final String USER_ID = "1";

    // 서버 프록시 엔드포인트 사용
    public static String buildFestivalUrl(Integer sigunguCode) {
        String today = new SimpleDateFormat("yyyyMMdd", Locale.KOREA).format(new Date());
        StringBuilder sb = new StringBuilder(API_BASE)
                .append("/api/tour/searchFestival")
                .append("?areaCode=32")
                .append("&eventStartDate=").append(today)
                .append("&arrange=Q&numOfRows=30&pageNo=1");
        if (sigunguCode != null) sb.append("&sigunguCode=").append(sigunguCode);
        return sb.toString();
    }

    // 자연/관광(관광지 cat1=A01) — 군별 고정 URL들
    public static final String HOENGSEONG_NATURAL_URL =
            BuildConfig.API_BASE + "/api/tour/areaBasedList?areaCode=32&sigunguCode=18&numOfRows=30&pageNo=1&arrange=Q&cat1=A01";

    public static final String HONGCHEON_NATURAL_URL =
            BuildConfig.API_BASE + "/api/tour/areaBasedList?areaCode=32&sigunguCode=16&numOfRows=30&pageNo=1&arrange=Q&cat1=A01";

    public static final String INJE_NATURAL_URL =
            BuildConfig.API_BASE + "/api/tour/areaBasedList?areaCode=32&sigunguCode=10&numOfRows=30&pageNo=1&arrange=Q&cat1=A01";

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public void fetchTourSpotsAsync(String url, TourCallback callback) {
        executor.execute(() -> {
            try {
                List<TouristSpotItem> list = fetchTourSpots(url);
                mainHandler.post(() -> callback.onSuccess(list));
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError(e));
            }
        });
    }

    private List<TouristSpotItem> fetchTourSpots(String reqUrl) throws Exception {
        HttpURLConnection conn = null;
        InputStream is = null;
        try {
            android.util.Log.d("ApiService", "GET " + reqUrl);
            URL url = new URL(reqUrl);
            conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(15000);
            conn.setConnectTimeout(15000);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);

            int code = conn.getResponseCode();
            android.util.Log.d("ApiService", "HTTP " + code);
            if (code != HttpURLConnection.HTTP_OK) {
                throw new RuntimeException("HTTP " + code);
            }

            is = conn.getInputStream();

            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            XmlPullParser parser = factory.newPullParser();
            parser.setInput(new InputStreamReader(is, StandardCharsets.UTF_8));

            List<TouristSpotItem> result = new ArrayList<>();
            TouristSpotItem current = null;

            int event = parser.getEventType();
            while (event != XmlPullParser.END_DOCUMENT) {
                String tagName;
                switch (event) {
                    case XmlPullParser.START_TAG:
                        tagName = parser.getName();
                        if ("item".equalsIgnoreCase(tagName)) {
                            current = new TouristSpotItem();
                        } else if (current != null) {
                            if ("title".equalsIgnoreCase(tagName)) {
                                current.setTitle(parser.nextText());
                            } else if ("addr1".equalsIgnoreCase(tagName)) {
                                current.setAddr1(parser.nextText());
                            } else if ("addr2".equalsIgnoreCase(tagName)) {
                                current.setAddr2(parser.nextText());
                            } else if ("firstimage".equalsIgnoreCase(tagName)) {
                                current.setFirstImage(parser.nextText());
                            } else if ("firstimage2".equalsIgnoreCase(tagName)) {
                                current.setFirstImage2(parser.nextText());
                            } else if ("mapx".equalsIgnoreCase(tagName)) {
                                current.setMapx(parser.nextText());
                            } else if ("mapy".equalsIgnoreCase(tagName)) {
                                current.setMapy(parser.nextText());
                            } else if ("contentid".equalsIgnoreCase(tagName)) {
                                current.setContentId(parser.nextText());
                            } else if ("modifiedtime".equalsIgnoreCase(tagName)) {
                                current.setModifiedTime(parser.nextText());
                            }
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        tagName = parser.getName();
                        if ("item".equalsIgnoreCase(tagName) && current != null) {
                            result.add(current);
                            current = null;
                        }
                        break;
                }
                event = parser.next();
            }
            return result;
        } finally {
            if (is != null) try { is.close(); } catch (Exception ignore) {}
            if (conn != null) conn.disconnect();
        }
    }

    public void fetchFestivalsAsync(String url, FestivalCallback callback) {
        executor.execute(() -> {
            try {
                List<FestivalItem> list = fetchFestivalSpots(url); // ⬅️ 그대로
                mainHandler.post(() -> callback.onSuccess(list));
            } catch (Exception e) {
                android.util.Log.e("ApiService", "error", e);
                mainHandler.post(() -> callback.onError(e));
            }
        });
    }

    private List<FestivalItem> fetchFestivalSpots(String reqUrl) throws Exception {
        HttpURLConnection conn = null;
        InputStream is = null;
        try {
            URL url = new URL(reqUrl);
            conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(15000);
            conn.setConnectTimeout(15000);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);

            if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
                throw new RuntimeException("HTTP " + conn.getResponseCode());
            }

            is = conn.getInputStream();
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            XmlPullParser parser = factory.newPullParser();
            parser.setInput(new InputStreamReader(is, StandardCharsets.UTF_8));

            List<FestivalItem> result = new ArrayList<>();
            FestivalItem current = null;

            int event = parser.getEventType();
            while (event != XmlPullParser.END_DOCUMENT) {
                switch (event) {
                    case XmlPullParser.START_TAG:
                        String tag = parser.getName();
                        if ("item".equalsIgnoreCase(tag)) {
                            current = new FestivalItem();
                        } else if (current != null) {
                            switch (tag) {
                                case "title": current.setTitle(parser.nextText()); break;
                                case "addr1": current.setAddr1(parser.nextText()); break;
                                case "addr2": current.setAddr2(parser.nextText()); break;
                                case "firstimage": current.setFirstImage(parser.nextText()); break;
                                case "firstimage2": current.setFirstImage2(parser.nextText()); break;
                                case "mapx": current.setMapx(parser.nextText()); break;
                                case "mapy": current.setMapy(parser.nextText()); break;
                                case "contentid": current.setContentId(parser.nextText()); break;
                                case "modifiedtime": current.setModifiedTime(parser.nextText()); break;
                                case "eventstartdate": current.setEventStartDate(parser.nextText()); break;
                                case "eventenddate": current.setEventEndDate(parser.nextText()); break;
                                case "tel":
                                    String tel = parser.nextText();
                                    tel = tel.replace("&lt;br&gt;", "\n")
                                            .replace("<br>", "\n")
                                            .replace("<BR>", "\n");
                                    current.setTel(tel.trim());
                                    break;
                                case "zipcode": current.setZipcode(parser.nextText()); break;
                            }
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        if ("item".equalsIgnoreCase(parser.getName()) && current != null) {
                            result.add(current);
                            current = null;
                        }
                        break;
                }
                event = parser.next();
            }
            return result;
        } finally {
            if (is != null) try { is.close(); } catch (Exception ignore) {}
            if (conn != null) conn.disconnect();
        }
    }
    // ApiService.java (클래스 안 아무 위치에 추가)
    public static String buildSearchKeywordUrl(String keyword,
                                               @Nullable Integer sigunguCode,
                                               @Nullable Integer page,
                                               @Nullable Integer size) {
        try {
            String enc = URLEncoder.encode(keyword, "UTF-8");
            int p = (page == null || page < 1) ? 1 : page;
            int s = (size == null || size < 1) ? 30 : size;

            StringBuilder sb = new StringBuilder(API_BASE)
                    .append("/api/tour/searchKeyword")
                    .append("?areaCode=32")
                    .append("&keyword=").append(enc)
                    .append("&arrange=Q")
                    .append("&numOfRows=").append(s)
                    .append("&pageNo=").append(p);
            if (sigunguCode != null) sb.append("&sigunguCode=").append(sigunguCode);
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    public void addBookmarkFromTourItem(TouristSpotItem item,
                                        Callback<BookmarkRes> callback) {
        android.util.Log.d("API_SERVICE", "POST /api/bookmarks/tourapi contentId=" + item.getContentId());
        executor.execute(() -> {
            try {
                JSONObject body = new JSONObject();
                body.put("contentid", item.getContentId());
                body.put("title", item.getTitle());
                body.put("addr1", item.getAddr1());
                body.put("addr2", item.getAddr2());
                body.put("firstimage", item.getFirstImage());
                body.put("firstimage2", item.getFirstImage2());
                body.put("mapx", item.getMapx());
                body.put("mapy", item.getMapy());
                body.put("cat1", item.getCat1());
                body.put("cat2", item.getCat2());
                body.put("cat3", item.getCat3());
                body.put("zipcode", item.getZipcode());
                body.put("tel", item.getTel());
                body.put("modifiedtime", item.getModifiedTime());
                body.put("contenttypeid", item.getContentTypeId());
                body.put("areacode", item.getAreaCode());
                body.put("sigungucode", item.getSigunguCode());
                body.put("mlevel", item.getMlevel());

                Request req = new Request.Builder()
                        .url(API_BASE + "/api/bookmarks/tourapi")
                        .addHeader("X-User-Id", USER_ID)
                        .post(RequestBody.create(body.toString(), JSON))
                        .build();

                try (Response resp = ok.newCall(req).execute()) {
                    if (!resp.isSuccessful()) throw new IOException("HTTP " + resp.code());
                    BookmarkRes res = parseBookmarkRes(resp.body().string());
                    mainHandler.post(() -> callback.onSuccess(res));
                }
            } catch (Exception e) {
                android.util.Log.e("API_SERVICE", "Save bookmark FAILED", e);
            }
        });
    }

    public interface Callback<T> {
        void onSuccess(T data);
        void onError(Exception e);
    }

    private BookmarkRes parseBookmarkRes(String json) throws Exception {
        JSONObject o = new JSONObject(json);
        BookmarkRes r = new BookmarkRes();
        r.id = o.optLong("id");
        r.provider = o.optString("provider", null);
        r.externalId = o.optString("externalId", null);
        r.placeName = o.optString("placeName", null);
        r.address = o.optString("address", null);
        r.category = o.optString("category", null);
        r.thumbnailUrl = o.optString("thumbnailUrl", null);
        r.createdAt = o.optString("createdAt", null);
        if (o.has("lat")) r.lat = o.optDouble("lat");
        if (o.has("lng")) r.lng = o.optDouble("lng");
        return r;
    }

    // 목록 읽기
    public void fetchBookmarks(Callback<List<BookmarkRes>> cb){
        executor.execute(() -> {
            try{
                Request req = new Request.Builder()
                        .url(API_BASE + "/api/bookmarks")
                        .addHeader("X-User-Id", USER_ID)
                        .get()
                        .build();
                try(Response resp = ok.newCall(req).execute()){
                    if(!resp.isSuccessful()) throw new IOException("HTTP "+resp.code());
                    String json = resp.body().string();
                    List<BookmarkRes> list = new ArrayList<>();
                    JSONArray arr = new JSONArray(json);
                    for(int i=0;i<arr.length();i++){
                        JSONObject o = arr.getJSONObject(i);
                        BookmarkRes r = new BookmarkRes();
                        r.id = o.optLong("id");
                        r.provider = o.optString("provider", null);
                        r.externalId = o.optString("externalId", null);
                        r.placeName = o.optString("placeName", null);
                        r.address = o.optString("address", null);
                        r.category = o.optString("category", null);
                        r.thumbnailUrl = o.optString("thumbnailUrl", null);
                        r.createdAt = o.optString("createdAt", null);
                        if (o.has("lat")) r.lat = o.optDouble("lat");
                        if (o.has("lng")) r.lng = o.optDouble("lng");
                        list.add(r);
                    }
                    mainHandler.post(() -> cb.onSuccess(list));
                }
            }catch(Exception e){
                mainHandler.post(() -> cb.onError(e));
            }
        });
    }

    // 삭제(선택) — Provider는 서버 enum 이름과 동일해야 함 ("TOURAPI")
    public void removeBookmark(String provider, String externalId, Callback<Boolean> cb){
        executor.execute(() -> {
            try{
                Request req = new Request.Builder()
                        .url(API_BASE + "/api/bookmarks/" + provider + "/" + externalId)
                        .addHeader("X-User-Id", USER_ID)
                        .delete()
                        .build();
                try(Response resp = ok.newCall(req).execute()){
                    if(!resp.isSuccessful()) throw new IOException("HTTP "+resp.code());
                    mainHandler.post(() -> cb.onSuccess(true));
                }
            }catch(Exception e){
                mainHandler.post(() -> cb.onError(e));
            }
        });
    }
}
