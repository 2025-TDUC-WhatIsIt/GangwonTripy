package com.example.gangwontripy.ui.directions;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.*;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.view.MenuHost;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;

import com.bumptech.glide.Glide;
import com.example.gangwontripy.BuildConfig;
import com.example.gangwontripy.R;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.kakao.vectormap.KakaoMap;
import com.kakao.vectormap.KakaoMapReadyCallback;
import com.kakao.vectormap.LatLng;
import com.kakao.vectormap.MapLifeCycleCallback;
import com.kakao.vectormap.MapType;
import com.kakao.vectormap.MapView;
import com.kakao.vectormap.MapViewInfo;
import com.kakao.vectormap.label.*;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;

import okhttp3.*;

public class DirectionsFragment extends Fragment {

    // ===== Kakao Map =====
    private MapView mapView;
    private KakaoMap kakaoMap;
    private LabelLayer labelLayer;
    private LabelStyles pinStyles;

    // ===== Networking =====
    private final OkHttpClient http = new OkHttpClient();
    private final Gson gson = new Gson();
    private static final String API_BASE = BuildConfig.API_BASE; // 예: http://<server>:8080

    // ===== Data =====
    private List<Poi> allPois = new ArrayList<>();
    private final Set<Integer> activeTypes = new HashSet<>(Arrays.asList(12,14,15,39)); // 처음엔 모두 선택

    // ===== Config =====
    private static final int RADIUS = 10_000; // m
    private static final LatLng DEFAULT_CENTER = LatLng.from(37.48895833, 127.9872222); // 횡성

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

        mapView = view.findViewById(R.id.map_view);

        mapView.start(new MapLifeCycleCallback() {
            @Override public void onMapDestroy() { Log.d("KakaoMap", "지도 종료됨"); }
            @Override public void onMapError(@NonNull Exception e) { Log.e("KakaoMap", "지도 오류", e); }
        }, new KakaoMapReadyCallback() {
            @Override
            public void onMapReady(@NonNull KakaoMap map) {
                kakaoMap = map;
                labelLayer = kakaoMap.getLabelManager().getLayer(); // 기본 레이어
                pinStyles = buildPinStyles();

                // 최초: 중심 좌표 기준으로 전체 타입(12/14/15/39) 조회
                fetchPoisFromServer(DEFAULT_CENTER.latitude, DEFAULT_CENTER.longitude, RADIUS, null);
                Log.d("KakaoMap", "지도 로딩 완료");
            }

            @Override public LatLng getPosition() { return DEFAULT_CENTER; }
            @Override public int getZoomLevel() { return 14; }
            @Override public MapViewInfo getMapViewInfo() { return MapViewInfo.from("openmap", MapType.NORMAL); }
        });
        MenuHost menuHost = requireActivity();
        menuHost.addMenuProvider(new MenuProvider() {
            @Override
            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
                menuInflater.inflate(R.menu.menu_directions, menu);
            }

            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
                if (menuItem.getItemId() == R.id.action_filter) {
                    showFilterDialog();
                    return true;
                }
                return false;
            }
        }, getViewLifecycleOwner(), Lifecycle.State.RESUMED);
    }

    // ===================== Options menu (필터) =====================


    private void showFilterDialog() {
        final String[] labels = {"관광지(12)","문화시설(14)","축제/공연(15)","음식점(39)"};
        final int[] types = {12,14,15,39};
        final boolean[] checked = new boolean[types.length];
        for (int i=0;i<types.length;i++) checked[i] = activeTypes.contains(types[i]);

        new AlertDialog.Builder(requireContext())
                .setTitle("표시할 카테고리")
                .setMultiChoiceItems(labels, checked, (d, which, isChecked) -> checked[which] = isChecked)
                .setPositiveButton("적용", (d, w) -> {
                    activeTypes.clear();
                    for (int i=0;i<types.length;i++) if (checked[i]) activeTypes.add(types[i]);
                    applyFilter(); // 로컬 필터 즉시 반영
                })
                .setNegativeButton("취소", null)
                .show();
    }

    private void applyFilter() {
        if (allPois == null) return;
        List<Poi> filtered = allPois.stream()
                .filter(p -> activeTypes.contains(p.contentTypeId))
                .collect(Collectors.toList());
        renderPois(filtered);
    }

    // ===================== 서버 호출 (JSON) =====================
    private void fetchPoisFromServer(double lat, double lng, int radius, @Nullable String typesCsv) {
        // typesCsv == null 이면 서버가 12,14,15,39 모두 조회
        String url = API_BASE + "/api/pois"
                + "?lat=" + lat + "&lng=" + lng + "&radius=" + radius
                + (typesCsv != null ? "&types=" + Uri.encode(typesCsv) : "")
                + "&page=1&size=60"; // 넉넉히

        Request req = new Request.Builder().url(url).get().build();
        http.newCall(req).enqueue(new Callback() {
            @Override public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e("POI", "요청 실패", e);
            }
            @Override public void onResponse(@NonNull Call call, @NonNull Response resp) throws IOException {
                if (!resp.isSuccessful()) {
                    Log.e("POI", "응답 코드: " + resp.code());
                    return;
                }
                String json = resp.body().string();
                Type listType = new TypeToken<List<Poi>>(){}.getType();
                List<Poi> pois = gson.fromJson(json, listType);

                if (!isAdded()) return;
                requireActivity().runOnUiThread(() -> {
                    allPois = (pois != null) ? pois : new ArrayList<>();
                    applyFilter(); // 현재 activeTypes 기준으로 렌더
                });
            }
        });
    }

    // ===================== 마커 렌더 =====================
    private void renderPois(List<Poi> pois) {
        if (kakaoMap == null || labelLayer == null) return;
        labelLayer.removeAll();

        List<LabelOptions> opts = new ArrayList<>(pois.size());
        long rank = 1_000_000_000L;
        for (Poi p : pois) {
            LabelOptions o = LabelOptions.from(LatLng.from(p.lat, p.lng))
                    .setStyles(pinStyles)
                    .setClickable(true)
                    .setTag(p)
                    .setRank(rank--);
            o.setTexts(new LabelTextBuilder().setTexts(safeTitle(p.title)));
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

    // ===================== 바텀시트 =====================
    private void showPoiBottomSheet(Poi p) {
        BottomSheetDialog d = new BottomSheetDialog(requireContext());
        View sheet = LayoutInflater.from(getContext()).inflate(R.layout.sheet_poi, null, false);

        ImageView iv = sheet.findViewById(R.id.ivPhoto);
        String imgUrl = (p.image != null && !p.image.trim().isEmpty()) ? p.image : null;

        Glide.with(this)
                .load(imgUrl != null ? imgUrl : R.drawable.image_gone)
                .placeholder(R.drawable.image_placeholder)
                .error(R.drawable.image_gone)
                .centerCrop()
                .into(iv);

        ((TextView) sheet.findViewById(R.id.tvTitle)).setText(safeTitle(p.title));
        ((TextView) sheet.findViewById(R.id.tvAddr)).setText(p.addr != null ? p.addr : "");
        ((TextView) sheet.findViewById(R.id.tvTel)).setText(
                (p.tel == null || p.tel.isEmpty()) ? "연락처 없음" : p.tel
        );

        sheet.findViewById(R.id.btnRoute).setOnClickListener(v -> {
            String url = "https://map.kakao.com/link/to/" + Uri.encode(safeTitle(p.title)) + "," + p.lat + "," + p.lng;
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
        });

        d.setContentView(sheet);
        d.show();
    }

    // ===================== 스타일/유틸 =====================
    private LabelStyles buildPinStyles() {
        LabelTextStyle text = LabelTextStyle.from(14, 0xFF000000);
        BitmapDrawable d = (BitmapDrawable) AppCompatResources.getDrawable(requireContext(), R.drawable.pin);
        // dp -> px
        int s = dp(16), m = dp(22), l = dp(28);

        LabelStyle ls = LabelStyle.from(Bitmap.createScaledBitmap(d.getBitmap(), s, s, true))
                .setAnchorPoint(0.5f, 1.0f)
                .setTextStyles(text)
                .setZoomLevel(0);
        LabelStyle lm = LabelStyle.from(Bitmap.createScaledBitmap(d.getBitmap(), m, m, true))
                .setAnchorPoint(0.5f, 1.0f)
                .setTextStyles(text)
                .setZoomLevel(12);
        LabelStyle ll = LabelStyle.from(Bitmap.createScaledBitmap(d.getBitmap(), l, l, true))
                .setAnchorPoint(0.5f, 1.0f)
                .setTextStyles(text)
                .setZoomLevel(16);

        return LabelStyles.from(ls, lm, ll);
    }

    private int dp(int dp) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics()
        );
    }

    private static String safeTitle(String s) {
        if (s == null) return "제목 없음";
        String t = s.trim();
        return t.isEmpty() ? "제목 없음" : t;
    }

    // ===================== 모델(JSON) =====================
    // 서버(BFF) /api/pois 응답 형식에 맞춰야 합니다.
    static class Poi {
        String id, title, addr, tel, image;
        double lat, lng;
        int contentTypeId; // 12/14/15/39
    }

    @Override
    public void onResume() { super.onResume(); if (mapView != null) mapView.resume(); }
    @Override
    public void onPause() { if (mapView != null) mapView.pause(); super.onPause(); }
    @Override
    public void onDestroyView() { if (mapView != null) mapView.finish(); super.onDestroyView(); }
}
