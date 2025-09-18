// com/whatisit/gangwontripy/ui/directions/DirectionsFragment.java
package com.whatisit.gangwontripy.ui.directions;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.*;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.view.MenuHost;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.kakao.vectormap.KakaoMap;
import com.kakao.vectormap.KakaoMapReadyCallback;
import com.kakao.vectormap.LatLng;
import com.kakao.vectormap.MapLifeCycleCallback;
import com.kakao.vectormap.MapType;
import com.kakao.vectormap.MapView;
import com.kakao.vectormap.MapViewInfo;
import com.kakao.vectormap.camera.CameraUpdateFactory;
import com.kakao.vectormap.label.LabelLayer;
import com.kakao.vectormap.label.LabelOptions;
import com.kakao.vectormap.label.LabelStyle;
import com.kakao.vectormap.label.LabelStyles;
import com.kakao.vectormap.label.LabelTextBuilder;
import com.kakao.vectormap.label.LabelTextStyle;
import com.whatisit.gangwontripy.BuildConfig;
import com.whatisit.gangwontripy.R;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;

import okhttp3.*;

public class DirectionsFragment extends Fragment {
    private View progressOverlay;
    // ===== Kakao Map =====
    private MapView mapView;
    private KakaoMap kakaoMap;
    private LabelLayer labelLayer;

    // 3가지 PinStyle
    private LabelStyles pinStylesNormal;
    private LabelStyles pinStylesSmall;
    private LabelStyles pinStylesSelected;
    @Nullable private String selectedPoiId = null;

    // POI 카드 오버레이
    private FrameLayout overlayLayer;
    private View poiSheet;
    private ImageView ivPhoto;
    private TextView tvTitle, tvAddr, tvTel;

    // ===== Networking =====
    private final OkHttpClient http = new OkHttpClient();
    private final Gson gson = new Gson();
    private static final String API_BASE = BuildConfig.API_BASE;

    // ===== Data =====
    private List<Poi> allPois = new ArrayList<>();
    private final Set<Integer> activeTypes = new HashSet<>(Arrays.asList(12,14,15,39)); // 처음엔 모두 선택

    // ===== TopCard =====
    private MaterialCardView cardControls;

    // ===== BottomSheet =====
    private BottomSheetBehavior<FrameLayout> bottomSheetBehavior;
    private PoiAdapter poiAdapter;

    // ===== Config =====
    private static final int RADIUS = 10_000; // m
    private static final LatLng POS_HS = LatLng.from(37.48895833, 127.9872222); // 횡성
    private static final LatLng POS_HC = LatLng.from(37.69442222, 127.8908417); // 홍천
    private static final LatLng POS_IJ = LatLng.from(38.06697222, 128.1726972); // 인제
    private static final LatLng POS_ALL = LatLng.from(
            (POS_HS.latitude + POS_HC.latitude + POS_IJ.latitude) / 3.0,
            (POS_HS.longitude + POS_HC.longitude + POS_IJ.longitude) / 3.0
    );
    private static final LatLng DEFAULT_CENTER = POS_HS;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_directions, container, false);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        progressOverlay = view.findViewById(R.id.progress_overlay);
        // ==== BottomSheet & Recycler ====
        cardControls = view.findViewById(R.id.card_controls);
        FrameLayout bs = view.findViewById(R.id.bottom_sheet);
        bottomSheetBehavior = BottomSheetBehavior.from(bs);
        bottomSheetBehavior.setHideable(false);
        try { bottomSheetBehavior.setDraggable(true); } catch (Throwable ignore) {}
        setPeekHeightDp(32);

        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);

//        bottomSheetBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
//            @Override public void onStateChanged(@NonNull View sheet, int newState) { /* no-op */ }
//            @Override public void onSlide(@NonNull View sheet, float slideOffset) {
//                // slideOffset: EXPANDED=1f, COLLAPSED≈0f, HIDDEN=-1f
//                if (slideOffset < -0.25f) { // 충분히 내리면
//                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
//                }
//            }
//        });

        // 바텀시트 위치에 따라 상단 카드(카테고리 선택지) 숨김
        bottomSheetBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                // 상태가 완전히 펼쳐졌을 때 (STATE_EXPANDED)
                if (newState == BottomSheetBehavior.STATE_EXPANDED && cardControls != null) {
                    cardControls.setVisibility(View.GONE);
                }
                // 상태가 접혔을 때 (STATE_COLLAPSED)
                else if (newState == BottomSheetBehavior.STATE_COLLAPSED && cardControls != null) {
                    cardControls.setVisibility(View.VISIBLE);
                }
            }
            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                if (cardControls == null) return;
                // slideOffset: 0 (접힘) ~ 1 (펼쳐짐)
                // 부드러운 애니메이션을 위해 onSlide에서도 alpha 값을 조절할 수 있습니다.
                // slideOffset이 0.5 이상으로 올라가면 점점 투명하게 만듦
                float alpha = 1.0f - (slideOffset * 2);
                if (alpha < 0) alpha = 0;
                cardControls.setAlpha(alpha);
            }
        });

        RecyclerView rv = view.findViewById(R.id.rv_pois);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        poiAdapter = new PoiAdapter(p -> {
            selectedPoiId = p.id;
            if (bottomSheetBehavior != null) bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);

            showPoiSheet(p); // 카드 띄우기
            if (kakaoMap != null) {
                try {
                    kakaoMap.moveCamera(CameraUpdateFactory.newCenterPosition(LatLng.from(p.lat, p.lng), 16));
                } catch (Throwable ignore) {}
            }
            // ★ 서버 재조회 없이 스타일만 새로 그리기
            applyFilter();
            // 바텀시트는 사용자가 보고 있으니 그대로 두기 (원하면 유지/펼침 선택)
            // bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        });
        rv.setAdapter(poiAdapter);

        preparePoiSheetOverlay((ViewGroup) view);

        // ==== Map ====
        mapView = view.findViewById(R.id.map_view);
        mapView.start(new MapLifeCycleCallback() {
            @Override public void onMapDestroy() { Log.d("KakaoMap", "map destroyed"); }
            @Override public void onMapError(@NonNull Exception e) { Log.e("KakaoMap", "map error", e); }
        }, new KakaoMapReadyCallback() {
            @Override public void onMapReady(@NonNull KakaoMap map) {
                kakaoMap = map;
                labelLayer = kakaoMap.getLabelManager().getLayer();
                pinStylesNormal   = buildPinStyles(/*tint*/ null, /*s*/16,22,28);
                pinStylesSmall    = buildPinStyles(0xFF9E9E9E /*회색*/, 12,16,20);
                pinStylesSelected = buildPinStyles(0xFFFF3B30 /*빨강*/, 20,26,32);
                fetchPoisFromServer(DEFAULT_CENTER.latitude, DEFAULT_CENTER.longitude, RADIUS, null);
            }
            @Override public LatLng getPosition() { return DEFAULT_CENTER; }
            @Override public int getZoomLevel() { return 14; }
            @Override public MapViewInfo getMapViewInfo() { return MapViewInfo.from("openmap", MapType.NORMAL); }
        });
        mapView.setOnTouchListener((v, ev) -> {
            if (ev.getAction() == MotionEvent.ACTION_DOWN) {
                if (bottomSheetBehavior != null && bottomSheetBehavior.getState() != BottomSheetBehavior.STATE_COLLAPSED) {
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                }
                // 오버레이 열려있으면 닫을지 말지는 기획에 따라
                // hidePoiSheet();
            }
            return false; // false: 지도 제스처는 그대로 동작
        });
        // ==== Top controls ====
        initTopControls(view);

        // (선택) 액션바 메뉴 사용 시
        MenuHost host = requireActivity();
        host.addMenuProvider(new MenuProvider() {
            @Override public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
                inflater.inflate(R.menu.menu_directions, menu);
            }
            @Override public boolean onMenuItemSelected(@NonNull MenuItem item) {
                if (item.getItemId() == R.id.action_filter) { showFilterDialog(); return true; }
                return false;
            }
        }, getViewLifecycleOwner(), Lifecycle.State.RESUMED);
    }
    private void setPeekHeightDp(int dpVal){
        int px = dp(dpVal);
        try { bottomSheetBehavior.setPeekHeight(px, true); }
        catch (Throwable t){ bottomSheetBehavior.setPeekHeight(px); }
    }
    private void setLoading(boolean on) {
        if (!isAdded()) return;
        if (progressOverlay != null) {
            progressOverlay.setVisibility(on ? View.VISIBLE : View.GONE);
        }
    }

    // 오버레이 준비 함수 교체
    private void preparePoiSheetOverlay(ViewGroup root) {
        overlayLayer = new FrameLayout(requireContext());
        overlayLayer.setLayoutParams(new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        overlayLayer.setClickable(true);
        overlayLayer.setVisibility(View.GONE);

        // ★ 반투명 딤 배경
        overlayLayer.setBackgroundColor(0x99000000); // #99000000 ≒ 60% 불투명

        // 바깥(딤 영역) 클릭 시 오버레이 닫기 + 리스트 하단바는 접힘(또는 숨김)
        overlayLayer.setOnClickListener(v -> {
            hidePoiSheet();
            if (bottomSheetBehavior != null)
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        });

        poiSheet = LayoutInflater.from(requireContext()).inflate(R.layout.sheet_poi, overlayLayer, false);

        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.BOTTOM);
        poiSheet.setLayoutParams(lp);
        poiSheet.setClickable(true);

        // ★ 시트 자체 배경(둥글고 흰색)과 그림자
        poiSheet.setBackgroundResource(R.drawable.bg_sheet_rounded); // 아래 드로어블 추가
        poiSheet.setElevation(dp(8));

        tvTitle = poiSheet.findViewById(R.id.tvTitle);
        ivPhoto = poiSheet.findViewById(R.id.ivPhoto);
        tvAddr  = poiSheet.findViewById(R.id.tvAddr);
        tvTel   = poiSheet.findViewById(R.id.tvTel);
        poiSheet.findViewById(R.id.btnRoute).setOnClickListener(v -> {
            Poi p = (Poi) poiSheet.getTag();
            if (p != null) openKakaoRoute(p);
        });

        overlayLayer.addView(poiSheet);
        root.addView(overlayLayer);
    }



    private void showPoiSheet(Poi p) {
        overlayLayer.setVisibility(View.VISIBLE);
        poiSheet.setVisibility(View.VISIBLE);
        poiSheet.setTag(p);

        tvTitle.setText(p.title == null ? "제목 없음" : p.title);
        tvAddr.setText(p.addr == null ? "" : p.addr);
        tvTel.setText(p.tel == null ? "" : p.tel);

        if (p.image != null && !p.image.isEmpty()) {
            Glide.with(this)
                    .load(p.image)
                    .placeholder(R.drawable.image_placeholder)
                    .error(R.drawable.image_gone)
                    .into(ivPhoto);
        } else {
            ivPhoto.setImageResource(R.drawable.image_placeholder);
        }

        // 핀 클릭 시 리스트 바텀시트는 접기
        if (bottomSheetBehavior != null) {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        }
    }

    private void hidePoiSheet() {
        if (overlayLayer != null) overlayLayer.setVisibility(View.GONE);
        if (poiSheet != null) poiSheet.setVisibility(View.GONE);
    }

    private void initTopControls(@NonNull View root) {
        // 👇 var 쓰지 말고 타입 명시
        TextInputLayout til = root.findViewById(R.id.til_location);
        MaterialAutoCompleteTextView act = root.findViewById(R.id.act_location);

        Context context = getContext();
        if (context != null) {
            ArrayAdapter<String> adapter = new ArrayAdapter<>(
                    context,
                    android.R.layout.simple_dropdown_item_1line,
                    AREAS
            );
            act.setAdapter(adapter);
        }

        // 최초 표시
        act.setText(AREAS[selectedAreaIndex], false);

        // 아이템을 클릭했을 때의 동작을 정의합니다.
        act.setOnItemClickListener((parent, view, position, id) -> {
            selectedAreaIndex = position;
            onAreaSelected(position);
        });

//        // 텍스트 클릭 → 다이얼로그
//        View.OnClickListener openDialog = v -> showAreaPickerDialog(act);
//        act.setOnClickListener(openDialog);
//        act.setOnFocusChangeListener((v, hasFocus) -> { if (hasFocus) showAreaPickerDialog(act); });
//
//        // end icon 클릭 → 다이얼로그
//        if (til != null) {
//            til.setEndIconOnClickListener(v -> showAreaPickerDialog(act));
//        }
    }

    private void showAreaPickerDialog(@NonNull MaterialAutoCompleteTextView act) {
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("지역 선택")
                .setSingleChoiceItems(AREAS, selectedAreaIndex, (d, which) -> {
                    selectedAreaIndex = which;
                    act.setText(AREAS[which], false);
                    onAreaSelected(which);
                    d.dismiss();
                })
                .show();
    }

    private void onAreaSelected(int pos) {
        switch (pos) {
            case 0: moveAndSearch(POS_ALL, 11, 25_000); break;
            case 1: moveAndSearch(POS_IJ, 14, RADIUS);  break;
            case 2: moveAndSearch(POS_HC, 14, RADIUS);  break;
            case 3: moveAndSearch(POS_HS, 14, RADIUS);  break;
        }
    }


    // 2) 다이얼로그 + 선택 처리
    private static final String[] AREAS = {"전체","인제","홍천","횡성"};
    private int selectedAreaIndex = 0; // “전체”로 시작


    // 카메라 이동 + 재조회
    private void moveAndSearch(LatLng target, int zoom, int radius) {
        if (kakaoMap != null) {
            try {
                kakaoMap.moveCamera(CameraUpdateFactory.newCenterPosition(target, zoom));
            } catch (Throwable ignore) {}
        }
        fetchPoisFromServer(target.latitude, target.longitude, radius, null);
    }

    private void showFilterDialog() {
        final String[] labels = {"관광지(12)","문화시설(14)","축제/공연(15)","음식점(39)"};
        final int[] types = {12,14,15,39};
        final boolean[] checked = new boolean[types.length];
        for (int i=0;i<types.length;i++) checked[i] = activeTypes.contains(types[i]);

        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("표시할 카테고리")
                .setMultiChoiceItems(labels, checked, (d, which, isChecked) -> checked[which] = isChecked)
                .setPositiveButton("적용", (d, w) -> {
                    activeTypes.clear();
                    for (int i=0;i<types.length;i++) if (checked[i]) activeTypes.add(types[i]);
                    applyFilter();
                })
                .setNegativeButton("취소", null)
                .show();
    }

    private void applyFilter() {
        if (!isAdded()) return;
        List<Poi> filtered = (allPois == null) ? Collections.emptyList()
                : allPois.stream().filter(p -> activeTypes.contains(p.contentTypeId)).collect(Collectors.toList());

        renderPois(filtered);
        poiAdapter.submitList(filtered);

        View root = getView();
        if (root != null) {
            TextView title = root.findViewById(R.id.tv_list_title);
            if (title != null) title.setText("주변 장소 (" + filtered.size() + ")");
        }
    }

    // ===== 서버 호출(JSON) =====
    private void fetchPoisFromServer(double lat, double lng, int radius, @Nullable String typesCsv) {
        String url = API_BASE + "/api/pois"
                + "?lat=" + lat + "&lng=" + lng + "&radius=" + radius
                + (typesCsv != null ? "&types=" + Uri.encode(typesCsv) : "")
                + "&page=1&size=60";
        setLoading(true);
        Request req = new Request.Builder().url(url).get().build();
        http.newCall(req).enqueue(new Callback() {
            @Override public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e("POI", "request failed", e);
                if (!isAdded()) return;
                requireActivity().runOnUiThread(() -> setLoading(false));
            }
            @Override public void onResponse(@NonNull Call call, @NonNull Response resp) throws IOException {
                selectedPoiId = null;

                if (!resp.isSuccessful()) {
                    Log.e("POI", "HTTP " + resp.code());
                    if (!isAdded()) return;
                    requireActivity().runOnUiThread(() -> setLoading(false));
                    return;
                }
                String json = resp.body().string();
                Type listType = new TypeToken<List<Poi>>(){}.getType();
                List<Poi> pois = gson.fromJson(json, listType);

                if (!isAdded()) return;
                requireActivity().runOnUiThread(() -> {
                    hidePoiSheet();
                    allPois = (pois != null) ? pois : new ArrayList<>();
                    applyFilter();
                    setLoading(false);
                });
            }
        });
    }
    private static boolean isFinite(double v) {
        return !Double.isNaN(v) && !Double.isInfinite(v);
    }
    private static boolean validLatLng(double lat, double lng) {
        return isFinite(lat) && isFinite(lng)
                && lat >= -90.0 && lat <= 90.0
                && lng >= -180.0 && lng <= 180.0
                && !(lat == 0.0 && lng == 0.0); // 0,0 은 잘못된 좌표로 취급
    }
    private static String nz(String s) { return (s == null) ? "" : s; }

    // ===== 마커 렌더 =====
    private void renderPois(List<Poi> pois) {
        if (kakaoMap == null || labelLayer == null) return;
        labelLayer.removeAll();

        List<LabelOptions> opts = new ArrayList<>(pois.size());
        long rank = 1_000_000_000L;

        for (Poi p : pois) {
            if (!validLatLng(p.lat, p.lng)) {
                Log.w("POI", "Skip invalid coord: id=" + nz(p.id) + " lat=" + p.lat + " lng=" + p.lng);
                continue;
            }
            // ★ 스타일 결정: 선택되었으면 빨강, 아니면 (선택이 있을 때) 축소
            LabelStyles styles;
            if (selectedPoiId != null) {
                styles = (p.id != null && p.id.equals(selectedPoiId)) ? pinStylesSelected : pinStylesSmall;
            } else {
                styles = pinStylesNormal;
            }

            LabelOptions o = LabelOptions.from(LatLng.from(p.lat, p.lng))
                    .setStyles(styles)
                    .setClickable(true)
                    .setTag(p)
                    .setRank(rank--);

            o.setTexts(new com.kakao.vectormap.label.LabelTextBuilder().setTexts(safeTitle(p.title)));
            opts.add(o);
        }
        if (opts.isEmpty()) return;
        try {
            labelLayer.addLabels(opts);
        } catch (RuntimeException e) {
            Log.e("POI", "addLabels failed: " + e.getMessage() + " — will bisect add", e);
            for (int i = 0; i < opts.size(); i++) {
                try {
                    labelLayer.addLabels(java.util.Collections.singletonList(opts.get(i)));
                } catch (RuntimeException one) {
                    Log.e("POI", "Bad LabelOptions at index=" + i
                            + " (id=" + ((Poi)opts.get(i).getTag()).id + "): " + one.getMessage());
                }
            }
        }

        kakaoMap.setOnLabelClickListener((map, layer, label) -> {
            Object tag = label.getTag();
            if (tag instanceof Poi) {
                Poi p = (Poi) tag;
                selectedPoiId = p.id;
                if (bottomSheetBehavior != null)
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                showPoiSheet(p);
                try {
                    kakaoMap.moveCamera(CameraUpdateFactory
                            .newCenterPosition(com.kakao.vectormap.LatLng.from(p.lat, p.lng), 16));
                } catch (Throwable ignore) {}
                applyFilter();
                return true;
            }
            return false;
        });
    }

    // ===== 핀 스타일 =====
    private LabelStyles buildPinStyles(@Nullable Integer tint, int s, int m, int l) {
        // 텍스트 스타일은 동일하게
        LabelTextStyle text = LabelTextStyle.from(14, 0xFF000000);

        Bitmap bmS = makePinBitmap(R.drawable.pin, s, tint);
        Bitmap bmM = makePinBitmap(R.drawable.pin, m, tint);
        Bitmap bmL = makePinBitmap(R.drawable.pin, l, tint);

        LabelStyle ls = LabelStyle.from(bmS).setAnchorPoint(0.5f, 1.0f).setTextStyles(text).setZoomLevel(0);
        LabelStyle lm = LabelStyle.from(bmM).setAnchorPoint(0.5f, 1.0f).setTextStyles(text).setZoomLevel(12);
        LabelStyle ll = LabelStyle.from(bmL).setAnchorPoint(0.5f, 1.0f).setTextStyles(text).setZoomLevel(16);
        return LabelStyles.from(ls, lm, ll);
    }

    private Bitmap makePinBitmap(int drawableRes, int sizeDp, @Nullable Integer tint) {
        int px = dp(sizeDp);
        android.graphics.drawable.Drawable dr = AppCompatResources.getDrawable(requireContext(), drawableRes);
        if (dr == null) throw new IllegalStateException("pin drawable missing");
        dr = dr.mutate();
        if (tint != null) dr.setTint(tint);

        android.graphics.Bitmap bmp = android.graphics.Bitmap.createBitmap(px, px, android.graphics.Bitmap.Config.ARGB_8888);
        android.graphics.Canvas canvas = new android.graphics.Canvas(bmp);
        dr.setBounds(0, 0, px, px);
        dr.draw(canvas);
        return bmp;
    }
    private void openKakaoRoute(Poi p) {
        // 목적지
        double dlat = p.lat, dlng = p.lng;

        // (선택) 출발지는 지도의 현재 중심을 사용. 못 구하면 생략하고 앱에서 현재위치로 처리하도록 시도
        String spParam = null;
        try {
            LatLng center = kakaoMap != null ? kakaoMap.getCameraPosition().getPosition() : null;
            if (center != null) spParam = String.format(Locale.US, "sp=%f,%f&", center.latitude, center.longitude);
        } catch (Throwable ignore) {}

        // 1차: KakaoMap 앱 스킴
        String appUrl = "kakaomap://route?" +
                (spParam != null ? spParam : "") +
                String.format(Locale.US, "ep=%f,%f&by=car", dlat, dlng);

        // 2차: 모바일웹 스킴(앱 없을 때)
        String webUrl = "http://m.map.kakao.com/scheme/route?" +
                (spParam != null ? spParam : "") +
                String.format(Locale.US, "ep=%f,%f&by=car", dlat, dlng);

        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(appUrl)));
        } catch (Exception notInstalled) {
            try {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(webUrl)));
            } catch (Exception e) {
                // 최종: 마켓 열기
                startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse("market://details?id=net.daum.android.map")));
            }
        }
    }


    private int dp(int dp) { return (int) TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics()); }
    private static String safeTitle(String s) { return (s == null || s.trim().isEmpty()) ? "제목 없음" : s.trim(); }

    // ===== 모델(JSON) =====
    static class Poi {
        public String id, title, addr, tel, image;
        public double lat, lng;
        public int contentTypeId;
    }

    // ===== 어댑터 =====
    static class PoiAdapter extends androidx.recyclerview.widget.ListAdapter<Poi, PoiAdapter.VH> {
        interface OnItemClick { void onClick(Poi p); }
        private final OnItemClick onItemClick;
        protected PoiAdapter(OnItemClick cb) {
            super(new DiffUtil.ItemCallback<Poi>() {
                @Override public boolean areItemsTheSame(@NonNull Poi a, @NonNull Poi b) {
                    return Objects.equals(a.id, b.id);
                }
                @Override public boolean areContentsTheSame(@NonNull Poi a, @NonNull Poi b) {
                    return Objects.equals(a.title,b.title)
                            && Objects.equals(a.addr,b.addr)
                            && Objects.equals(a.image,b.image)
                            && a.lat==b.lat && a.lng==b.lng
                            && a.contentTypeId==b.contentTypeId;
                }
            });
            this.onItemClick = cb;
        }
        static class VH extends RecyclerView.ViewHolder {
            ImageView iv; TextView t1,t2;
            VH(@NonNull View v){ super(v);
                iv=v.findViewById(R.id.ivThumb);
                t1=v.findViewById(R.id.tvTitle);
                t2=v.findViewById(R.id.tvAddr);
            }
        }
        @NonNull @Override public VH onCreateViewHolder(@NonNull ViewGroup p, int vt){
            return new VH(LayoutInflater.from(p.getContext()).inflate(R.layout.item_poi, p, false));
        }
        @Override public void onBindViewHolder(@NonNull VH h, int pos){
            Poi p = getItem(pos);
            h.t1.setText(p.title==null?"제목 없음":p.title);
            h.t2.setText(p.addr==null?"":p.addr);
            Glide.with(h.iv).load(p.image==null||p.image.isEmpty()?R.drawable.image_placeholder:p.image)
                    .placeholder(R.drawable.image_placeholder)
                    .error(R.drawable.image_gone)
                    .into(h.iv);
            h.itemView.setOnClickListener(v -> onItemClick.onClick(p));
        }
    }

    @Override public void onResume() { super.onResume(); if (mapView != null) mapView.resume(); }
    @Override public void onPause() { if (mapView != null) mapView.pause(); super.onPause(); }
    @Override public void onDestroyView() { if (mapView != null) mapView.finish(); super.onDestroyView(); }
}
