package com.example.gangwontripy.data.api;

import android.os.Handler;
import android.os.Looper;

import com.example.gangwontripy.data.model.FestivalItem;
import com.example.gangwontripy.data.model.TouristSpotItem;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;
import com.example.gangwontripy.BuildConfig;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ApiService {

    private static final String BASE = "https://apis.data.go.kr/B551011/KorService2";
    public static String buildFestivalUrl(int sigunguCode) {
        String today = new SimpleDateFormat("yyyyMMdd", Locale.KOREA).format(new Date());
        return BASE + "/searchFestival2"
                + "?MobileOS=ETC&MobileApp=MobileApp"
                + "&serviceKey=" + BuildConfig.TOUR_API_SERVICE_KEY
                + "&areaCode=32"
                /*+ "&sigunguCode=" + sigunguCode*/
                + "&eventStartDate=" + today
                + "&arrange=Q&numOfRows=30&pageNo=1";
    }
    public static final String HOENGSEONG_NATURAL_URL =
            "https://apis.data.go.kr/B551011/KorService2/areaBasedList2?MobileOS=ETC&MobileApp=MobileApp&serviceKey="+
                    BuildConfig.TOUR_API_SERVICE_KEY+"&areaCode=32&sigunguCode=18&numOfRows=30&pageNo=1&arrange=Q&cat1=A01";
    public static final String HONGCHEON_NATURAL_URL =
            "https://apis.data.go.kr/B551011/KorService2/areaBasedList2?MobileOS=ETC&MobileApp=MobileApp&serviceKey="+
                    BuildConfig.TOUR_API_SERVICE_KEY+"&areaCode=32&sigunguCode=16&numOfRows=30&pageNo=1&arrange=Q&cat1=A01";
    public static final String INJE_NATURAL_URL =
            "https://apis.data.go.kr/B551011/KorService2/areaBasedList2?MobileOS=ETC&MobileApp=MobileApp&serviceKey="+
                    BuildConfig.TOUR_API_SERVICE_KEY+"&areaCode=32&sigunguCode=10&numOfRows=30&pageNo=1&arrange=Q&cat1=A01";

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
            URL url = new URL(reqUrl);
            conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(15000);
            conn.setConnectTimeout(15000);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);

            int code = conn.getResponseCode();
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
                List<FestivalItem> list = fetchFestivalSpots(url);
                mainHandler.post(() -> callback.onSuccess(list));
            } catch (Exception e) {
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
                                    // HTML br 처리 (&lt;br&gt; 또는 <br>)
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
}
