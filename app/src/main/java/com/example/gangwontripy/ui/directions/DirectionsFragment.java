package com.example.gangwontripy.ui.directions;

import static android.text.TextUtils.isEmpty;

import com.bumptech.glide.Glide;
import com.example.gangwontripy.BuildConfig;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.fragment.app.Fragment;

import com.example.gangwontripy.R;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.kakao.vectormap.KakaoMap;
import com.kakao.vectormap.KakaoMapReadyCallback;
import com.kakao.vectormap.KakaoMapSdk;
import com.kakao.vectormap.LatLng;
import com.kakao.vectormap.MapLifeCycleCallback;
import com.kakao.vectormap.MapType;
import com.kakao.vectormap.MapView;
import com.kakao.vectormap.MapViewInfo;
import com.kakao.vectormap.label.LabelLayer;
import com.kakao.vectormap.label.LabelOptions;
import com.kakao.vectormap.label.LabelStyle;
import com.kakao.vectormap.label.LabelStyles;
import com.kakao.vectormap.label.LabelTextBuilder;
import com.kakao.vectormap.label.LabelTextStyle;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class DirectionsFragment extends Fragment {

    private MapView mapView;
    private KakaoMap kakaoMap;
    private LabelLayer labelLayer;
    private LabelStyles pinStyles;
    private OkHttpClient http = new OkHttpClient();

    private static final String KAKAO_MAP_KEY = BuildConfig.KAKAO_MAP_KEY;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_directions, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // SDK 초기화
        KakaoMapSdk.init(requireContext(), KAKAO_MAP_KEY);

        // XML에 있는 MapView 가져오기
        mapView = view.findViewById(R.id.map_view);

        // 지도 시작
        mapView.start(new MapLifeCycleCallback() {
            @Override
            public void onMapDestroy() {
                Log.d("KakaoMap", "지도 종료됨");
            }

            @Override
            public void onMapError(@NonNull Exception e) {
                Log.e("KakaoMap", "지도 오류 발생", e);
            }
        }, new KakaoMapReadyCallback() {
            @Override
            public void onMapReady(@NonNull KakaoMap map) {
                kakaoMap = map;
                // 라벨 레이어 & 스타일 준비 (핀 아이콘 + 텍스트 크기/색 지정 가능)
                labelLayer = kakaoMap.getLabelManager().getLayer();  // 기본 레이어 사용 :contentReference[oaicite:2]{index=2}
                pinStyles = buildPinStyles();

                // 정적으로 선언 (위치 기반)
                String apiUrl = "https://apis.data.go.kr/B551011/KorService2/locationBasedList2?MobileOS=AND&MobileApp=MobileApp&mapX=127.9872222&mapY=37.48895833&radius=10000&serviceKey=EeqW3Ilbl2vnG%2Fe2pFHcPgXU%2BCKI5IEpduAsNzG6uCTk6U5LZGc%2BqSx0ULLEayS2Nikh0iI6KOIE8XrZL8XiUw%3D%3D&numOfRows=30&pageNo=1&contentTypeId=39"; // <-- 실제 URL로 교체
                fetchPoisFromApi(apiUrl);
                Log.d("KakaoMap", "지도 로딩 완료");
            }

            @Override
            public LatLng getPosition() {
                return LatLng.from(37.48895833, 127.9872222); // 횡성
                // return LatLng.from(37.69442222, 127.8908417); // 홍천
                // return LatLng.from(38.06697222, 128.1726972); // 인제
            }

            @Override
            public int getZoomLevel() {
                return 14;
            }

            @Override
            public MapViewInfo getMapViewInfo() {
                return MapViewInfo.from("openmap", MapType.NORMAL); // ✅ 수정됨
            }
        });
    }
    // dp → px
    private int dp(int dp) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
    }

    // drawable(any) → 정사각 px 크기의 Bitmap
    private Bitmap toBitmap(@NonNull Drawable d, int sizePx) {
        if (d instanceof BitmapDrawable) {
            Bitmap src = ((BitmapDrawable) d).getBitmap();
            return Bitmap.createScaledBitmap(src, sizePx, sizePx, true);
        }
        Bitmap out = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(out);
        d.setBounds(0, 0, sizePx, sizePx);
        d.draw(c);
        return out;
    }

    private Bitmap loadScaledBitmap(@DrawableRes int resId, int sizeDp) {
        Drawable d = AppCompatResources.getDrawable(requireContext(), resId);
        return toBitmap(d, dp(sizeDp));
    }

    /** B + C: 런타임 스케일 + 줌 레벨별 스타일 */
    private LabelStyles buildPinStyles() {
        // 원하는 크기(dp)와 적용 줌 레벨(최소값)만 조정하면 됨
        Bitmap pinS = loadScaledBitmap(R.drawable.pin, 16); // 작게
        Bitmap pinM = loadScaledBitmap(R.drawable.pin, 22); // 보통
        Bitmap pinL = loadScaledBitmap(R.drawable.pin, 28); // 크게

        LabelTextStyle text = LabelTextStyle.from(14, 0xFF000000);

        LabelStyle s = LabelStyle.from(pinS)
                .setAnchorPoint(0.5f, 1.0f)
                .setTextStyles(text)
                .setZoomLevel(0);   // 줌아웃

        LabelStyle m = LabelStyle.from(pinM)
                .setAnchorPoint(0.5f, 1.0f)
                .setTextStyles(text)
                .setZoomLevel(12);  // 중간 줌부터 적용

        LabelStyle l = LabelStyle.from(pinL)
                .setAnchorPoint(0.5f, 1.0f)
                .setTextStyles(text)
                .setZoomLevel(16);  // 많이 확대 시 적용

        return LabelStyles.from(s, m, l);
    }
    // ===================== 네트워크 호출 =====================
    private void fetchPoisFromApi(String url) {
        Request req = new Request.Builder().url(url).build();
        http.newCall(req).enqueue(new Callback() {
            @Override public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e("API", "요청 실패", e);
            }

            @Override public void onResponse(@NonNull Call call, @NonNull Response resp) throws IOException {
                if (!resp.isSuccessful()) { Log.e("API", "응답 코드: " + resp.code()); return; }
                String xml = resp.body().string();

                // XML 파싱
                List<Poi> pois = parseTourXml(xml);

                if (!isAdded()) return;
                requireActivity().runOnUiThread(() -> addPoisToMap(pois));
            }
        });
    }

    // ===================== XML 파싱 (XmlPullParser) =====================
    private List<Poi> parseTourXml(String xml) {
        List<Poi> list = new ArrayList<>();
        try {
            XmlPullParserFactory f = XmlPullParserFactory.newInstance();
            XmlPullParser xpp = f.newPullParser();
            xpp.setInput(new StringReader(xml));

            Poi cur = null;
            int event = xpp.getEventType();
            while (event != XmlPullParser.END_DOCUMENT) {
                if (event == XmlPullParser.START_TAG) {
                    String name = xpp.getName();
                    if ("item".equals(name)) {
                        cur = new Poi();
                    } else if (cur != null) {
                        switch (name) {
                            case "title":      cur.title = readText(xpp); break;
                            case "addr1":      cur.addr  = readText(xpp); break;
                            case "tel":        cur.tel   = readText(xpp); break;
                            case "firstimage": cur.image = readText(xpp); break;
                            case "firstimage2": cur.image2 = readText(xpp); break;
                            case "mapy":       cur.lat   = toD(readText(xpp)); break; // 위도
                            case "mapx":       cur.lng   = toD(readText(xpp)); break; // 경도
                            default:
                                // 다른 태그는 건너뜀
                                break;
                        }
                    }
                } else if (event == XmlPullParser.END_TAG) {
                    if ("item".equals(xpp.getName()) && cur != null) {
                        // title 기본값 보정
                        cur.title = fallbackTitle(cur);
                        if (cur.lat != 0 && cur.lng != 0) list.add(cur);
                        cur = null;
                    }
                }
                event = xpp.next();
            }
        } catch (Exception e) {
            Log.e("API", "XML 파싱 실패", e);
        }
        return list;
    }


    private static String next(XmlPullParser xpp) throws Exception {
        xpp.next(); String t = xpp.getText(); xpp.nextTag(); return t == null ? "" : t.trim();
    }
    private static double toD(String s) { try { return Double.parseDouble(s); } catch (Exception e) { return 0d; } }
    private static String readText(XmlPullParser xpp) throws Exception {
        String t = xpp.nextText();            // ← 핵심: 태그 안의 텍스트 안전하게 반환
        if (t == null) return "";
        t = t.replace('\n', ' ').trim().replaceAll("\\s+", " ");
        return t;
    }

    private static String fallbackTitle(Poi p) {
        // title이 비면 addr로, 그것도 없으면 기본값
        if (p.title != null && !p.title.trim().isEmpty()) return p.title.trim();
        if (p.addr  != null && !p.addr.trim().isEmpty())  return p.addr.trim();
        return "제목 없음";
    }

    // ===================== 지도에 라벨(마커) 추가 =====================
    private void addPoisToMap(List<Poi> pois) {
        if (kakaoMap == null || labelLayer == null || pois == null || pois.isEmpty()) return;

        List<LabelOptions> opts = new ArrayList<>(pois.size());
        int idx = 0;
        for (Poi p : pois) {
            idx++;

            String title = fallbackTitle(p); // ← 여기서도 방어

            LabelOptions o = LabelOptions.from(LatLng.from(p.lat, p.lng))
                    .setStyles(pinStyles)
                    .setClickable(true)
                    .setTag(p)
                    .setRank(1_000_000_000L - idx); // 겹침시 우선 노출 도움

            // 최신 SDK 방식: LabelTextBuilder
            LabelTextBuilder tb = new LabelTextBuilder().setTexts(title);
            o.setTexts(tb);

            opts.add(o);
        }

        labelLayer.addLabels(opts);

        kakaoMap.setOnLabelClickListener((map, layer, label) -> {
            Object tag = label.getTag();
            if (tag instanceof Poi) {
                showPoiBottomSheet((Poi) tag);
                return true;
            }
            return false;
        });
    }
    private static String safeTitle(String s) {
        if (s == null) return "제목 없음";
        String t = s.trim();
        return t.isEmpty() ? "제목 없음" : t;
    }
    // ===================== 바텀시트 상세 =====================
    private static String toHttpsIfPossible(String url) {
        if (url == null) return null;
        // 가능하면 https로 바꿔보기 (지원하면 그대로 성공)
        if (url.startsWith("http://")) {
            return url.replaceFirst("http://", "https://");
        }
        return url;
    }
    private void showPoiBottomSheet(Poi p) {
        BottomSheetDialog d = new BottomSheetDialog(requireContext());
        View sheet = LayoutInflater.from(getContext()).inflate(R.layout.sheet_poi, null, false);

        // ① URL 준비 (p.image가 비면 숨김)
        ImageView iv = sheet.findViewById(R.id.ivPhoto);

        // firstimage가 비면 firstimage2 사용
        String imgUrl = (p.image != null && !p.image.trim().isEmpty())
                ? p.image
                : p.image2;

        // 모델을 URL 또는 로컬 리소스로 결정
        Object model = (imgUrl != null && !imgUrl.trim().isEmpty())
                ? /* toHttpsIfPossible(imgUrl) 를 쓰는 경우: */ imgUrl  // https 변환 쓰면 여기만 교체
                : Integer.valueOf(R.drawable.image_gone);               // ← 이미지 없으면 이 리소스 표시

        iv.setVisibility(View.VISIBLE); // 항상 보이도록

        Glide.with(this)
                .load(model)
                .placeholder(R.drawable.image_placeholder)  // 로딩 중
                .error(R.drawable.image_gone)               // 로드 실패 시에도 image_gone
                .centerCrop()
                .into(iv);


        ((TextView) sheet.findViewById(R.id.tvTitle)).setText(p.title);
        ((TextView) sheet.findViewById(R.id.tvAddr)).setText(p.addr);
        ((TextView) sheet.findViewById(R.id.tvTel)).setText(
                (p.tel == null || p.tel.isEmpty()) ? "연락처 없음" : p.tel
        );

        sheet.findViewById(R.id.btnRoute).setOnClickListener(v -> {
            String url = "https://map.kakao.com/link/to/" + Uri.encode(p.title) + "," + p.lat + "," + p.lng;
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
        });

        d.setContentView(sheet);
        d.show();
    }

    // ===================== 모델 =====================
    static class Poi {
        String title, addr, tel, image, image2;
        double lat, lng; // mapy=lat, mapx=lng (WGS84)
    }
    @Override
    public void onResume() {
        super.onResume();
        if (mapView != null) {
            mapView.resume();
        }
    }

    @Override
    public void onPause() {
        if (mapView != null) {
            mapView.pause();
        }
        super.onPause();
    }

    @Override
    public void onDestroyView() {
        if (mapView != null) {
            mapView.finish();
        }
        super.onDestroyView();
    }
}
