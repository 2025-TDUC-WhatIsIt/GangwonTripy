// com/whatisit/gangwontripy/ui/directions/DirectionsFragment.java
package com.whatisit.gangwontripy.ui.directions;

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
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
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

    // ===== Kakao Map =====
    private MapView mapView;
    private KakaoMap kakaoMap;
    private LabelLayer labelLayer;
    private LabelStyles pinStyles;

    // ===== Networking =====
    private final OkHttpClient http = new OkHttpClient();
    private final Gson gson = new Gson();
    private static final String API_BASE = BuildConfig.API_BASE;

    // ===== Data =====
    private List<Poi> allPois = new ArrayList<>();
    private final Set<Integer> activeTypes = new HashSet<>(Arrays.asList(12,14,15,39)); // 처음엔 모두 선택

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

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // ==== BottomSheet & Recycler ====
        FrameLayout bs = view.findViewById(R.id.bottom_sheet);
        bottomSheetBehavior = BottomSheetBehavior.from(bs);
        bottomSheetBehavior.setHideable(false);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);

        RecyclerView rv = view.findViewById(R.id.rv_pois);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        poiAdapter = new PoiAdapter(p -> {
            moveAndSearch(LatLng.from(p.lat, p.lng), 16, RADIUS);
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        });
        rv.setAdapter(poiAdapter);

        // ==== Map ====
        mapView = view.findViewById(R.id.map_view);
        mapView.start(new MapLifeCycleCallback() {
            @Override public void onMapDestroy() { Log.d("KakaoMap", "map destroyed"); }
            @Override public void onMapError(@NonNull Exception e) { Log.e("KakaoMap", "map error", e); }
        }, new KakaoMapReadyCallback() {
            @Override public void onMapReady(@NonNull KakaoMap map) {
                kakaoMap = map;
                labelLayer = kakaoMap.getLabelManager().getLayer();
                pinStyles = buildPinStyles();
                fetchPoisFromServer(DEFAULT_CENTER.latitude, DEFAULT_CENTER.longitude, RADIUS, null);
            }
            @Override public LatLng getPosition() { return DEFAULT_CENTER; }
            @Override public int getZoomLevel() { return 14; }
            @Override public MapViewInfo getMapViewInfo() { return MapViewInfo.from("openmap", MapType.NORMAL); }
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

    private void initTopControls(@NonNull View root) {
        // 지역 드롭다운
        MaterialAutoCompleteTextView act = root.findViewById(R.id.act_location);
        List<String> AREAS = Arrays.asList("전체","인제","홍천","횡성");
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_list_item_1, AREAS);
        act.setAdapter(adapter);
        act.setText("전체", false);
        act.setOnClickListener(v -> act.showDropDown());
        act.setOnFocusChangeListener((v, hasFocus) -> { if (hasFocus) act.showDropDown(); });
        act.setOnItemClickListener((parent, v, pos, id) -> {
            switch (pos) {
                case 0: moveAndSearch(POS_ALL, 11, 25_000); break;
                case 1: moveAndSearch(POS_IJ, 14, RADIUS);  break;
                case 2: moveAndSearch(POS_HC, 14, RADIUS);  break;
                case 3: moveAndSearch(POS_HS, 14, RADIUS);  break;
            }
        });

        // 칩 필터 (다중 선택)
        ChipGroup chips = root.findViewById(R.id.chips_types);
        Map<Integer,Integer> chipToType = new HashMap<>();
        chipToType.put(R.id.chip_12, 12);
        chipToType.put(R.id.chip_14, 14);
        chipToType.put(R.id.chip_15, 15);
        chipToType.put(R.id.chip_39, 39);

        chips.setOnCheckedStateChangeListener((group, checkedIds) -> {
            activeTypes.clear();
            for (int id : checkedIds) {
                Integer type = chipToType.get(id);
                if (type != null) activeTypes.add(type);
            }
            // 선택이 0개면 빈 리스트(=지도/목록 비움)
            applyFilter();
        });
    }

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

        Request req = new Request.Builder().url(url).get().build();
        http.newCall(req).enqueue(new Callback() {
            @Override public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e("POI", "request failed", e);
            }
            @Override public void onResponse(@NonNull Call call, @NonNull Response resp) throws IOException {
                if (!resp.isSuccessful()) {
                    Log.e("POI", "HTTP " + resp.code());
                    return;
                }
                String json = resp.body().string();
                Type listType = new TypeToken<List<Poi>>(){}.getType();
                List<Poi> pois = gson.fromJson(json, listType);

                if (!isAdded()) return;
                requireActivity().runOnUiThread(() -> {
                    allPois = (pois != null) ? pois : new ArrayList<>();
                    applyFilter();
                });
            }
        });
    }

    // ===== 마커 렌더 =====
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
                Poi p = (Poi) tag;
                // (옵션) 상세 바텀시트 사용 중이면 유지 가능
                // showPoiBottomSheet(p);

                // 목록 바텀시트 펼치고 해당 아이템 위치로 스크롤
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                int idx = poiAdapter.getCurrentList().indexOf(p);
                RecyclerView rv = requireView().findViewById(R.id.rv_pois);
                if (idx >= 0 && rv != null) rv.scrollToPosition(idx);
                return true;
            }
            return false;
        });
    }

    // ===== 핀 스타일 =====
    private LabelStyles buildPinStyles() {
        LabelTextStyle text = LabelTextStyle.from(14, 0xFF000000);
        BitmapDrawable d = (BitmapDrawable) AppCompatResources.getDrawable(requireContext(), R.drawable.pin);
        int s = dp(16), m = dp(22), l = dp(28);
        LabelStyle ls = LabelStyle.from(Bitmap.createScaledBitmap(d.getBitmap(), s, s, true))
                .setAnchorPoint(0.5f, 1.0f).setTextStyles(text).setZoomLevel(0);
        LabelStyle lm = LabelStyle.from(Bitmap.createScaledBitmap(d.getBitmap(), m, m, true))
                .setAnchorPoint(0.5f, 1.0f).setTextStyles(text).setZoomLevel(12);
        LabelStyle ll = LabelStyle.from(Bitmap.createScaledBitmap(d.getBitmap(), l, l, true))
                .setAnchorPoint(0.5f, 1.0f).setTextStyles(text).setZoomLevel(16);
        return LabelStyles.from(ls, lm, ll);
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
