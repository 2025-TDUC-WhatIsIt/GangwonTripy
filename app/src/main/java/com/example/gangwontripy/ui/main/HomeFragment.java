package com.example.gangwontripy.ui.main;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.HapticFeedbackConstants;
import android.view.KeyEvent;
import android.view.Surface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView; // ✅ 추가
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.OptIn;
import androidx.camera.core.AspectRatio;          // ✅ 추가
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ExperimentalGetImage;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.example.gangwontripy.R;
import com.example.gangwontripy.data.api.ApiClient;
import com.example.gangwontripy.data.api.ApiService;
import com.example.gangwontripy.data.api.FestivalCallback;
import com.example.gangwontripy.data.api.TourCallback;
import com.example.gangwontripy.data.api.VisitApi;
import com.example.gangwontripy.data.model.BookmarkRes;
import com.example.gangwontripy.data.model.FestivalItem;
import com.example.gangwontripy.data.model.MarketItem;
import com.example.gangwontripy.data.model.TouristSpotItem;
import com.example.gangwontripy.ui.main.home.FestivalAdapter;
import com.example.gangwontripy.ui.main.home.MarketAdapter;
import com.example.gangwontripy.ui.main.home.TouristSpotPagerAdapter;
import com.example.gangwontripy.ui.mypage.QrScanActivity;
import com.example.gangwontripy.ui.spot.SearchedSpotAdapter;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScannerOptions;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment {
    // 뷰 교체용 멤버 변수
    private ConstraintLayout defaultStateContainer;
    private RecyclerView searchResultRecyclerView;
    private ProgressBar loadingIndicator;
    private EditText searchBar;

    // 검색
    private SearchedSpotAdapter searchedSpotAdapter;

    // -------- 관광명소(ViewPager2) --------
    private ViewPager2 touristPager;
    private TouristSpotPagerAdapter touristAdapter;
    private LinearLayout dotsTourist;                 // 관광명소 도트 컨테이너
    private static final int MAX_DOTS = 5;            // 최대 5개

    private final Handler autoHandler = new Handler(Looper.getMainLooper());
    private final long AUTO_DELAY_MS = 3000L;
    // 북마크
    private final java.util.Set<String> savedIds = new java.util.HashSet<>();
    private final Runnable autoRunnable = new Runnable() {
        @Override public void run() {
            if (touristPager != null && touristAdapter != null) {
                int count = touristAdapter.getRealCount();
                if (count > 1) {
                    int next = (touristPager.getCurrentItem() + 1) % count;
                    touristPager.setCurrentItem(next, true);
                    autoHandler.postDelayed(this, AUTO_DELAY_MS);
                }
            }
        }
    };

    private final ActivityResultLauncher<String> cameraPermForScanner =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                if (granted) openQrScanner();
                else android.widget.Toast.makeText(requireContext(), "카메라 권한이 필요합니다.", android.widget.Toast.LENGTH_SHORT).show();
            });
    private void openQrScanner() {
        startActivity(new Intent(requireContext(), QrScanActivity.class));
    }
    // -------- 축제(RecyclerView+Snap) --------
    private RecyclerView festivalRv;
    private FestivalAdapter festivalAdapter;
    private LinearLayout dotsFestival;                // 축제 도트 컨테이너
    private PagerSnapHelper festivalSnapHelper;
    private final Handler festivalAutoHandler = new Handler(Looper.getMainLooper());
    private final long FESTIVAL_AUTO_DELAY_MS = 3000L;
    private final Runnable festivalAutoRunnable = new Runnable() {
        @Override public void run() {
            if (festivalRv == null || festivalAdapter == null) return;
            int count = festivalAdapter.getItemCount();
            if (count <= 1) return;
            int cur = getSnappedPosition(festivalRv, festivalSnapHelper);
            if (cur == RecyclerView.NO_POSITION) cur = 0;
            int next = (cur + 1) % count;
            festivalRv.smoothScrollToPosition(next);
            festivalAutoHandler.postDelayed(this, FESTIVAL_AUTO_DELAY_MS);
        }
    };

    private ApiService apiService;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState){
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState){
        super.onViewCreated(view, savedInstanceState);
        apiService = new ApiService(requireContext());
        // XML의 뷰들을 코드와 연결
        defaultStateContainer = view.findViewById(R.id.default_state_container);
        searchResultRecyclerView = view.findViewById(R.id.search_result_recycler_view);
        loadingIndicator = view.findViewById(R.id.loading_indicator);
        searchBar = view.findViewById(R.id.search_bar);

        setupRecyclerView();

        // 검색창 동작
        searchBar.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                String query = v.getText().toString();
                if (!query.isEmpty()) {
                    performSearch(query);
                }
                return true;
            }
            return false;
        });

        // --- 전통시장 섹션 ---
        RecyclerView marketRecyclerView = view.findViewById(R.id.recycler_view_market);
        List<MarketItem> marketDataList = new ArrayList<>();
        marketDataList.add(new MarketItem("강릉중앙시장", R.drawable.img_rectangle));
        marketDataList.add(new MarketItem("속초관광수산시장", R.drawable.img_rectangle));
        marketDataList.add(new MarketItem("정선아리랑시장", R.drawable.img_rectangle));
        marketDataList.add(new MarketItem("동해동쪽바다중앙시장", R.drawable.img_rectangle));
        marketDataList.add(new MarketItem("원주중앙시장", R.drawable.img_rectangle));
        marketDataList.add(new MarketItem("춘천중앙시장", R.drawable.img_rectangle));
        MarketAdapter marketAdapter = new MarketAdapter(marketDataList);
        marketRecyclerView.setAdapter(marketAdapter);
        marketRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        new PagerSnapHelper().attachToRecyclerView(marketRecyclerView);

        // --- 관광명소 ViewPager2 & 도트 세팅 ---
        touristPager = view.findViewById(R.id.viewpager_tourist);
        touristAdapter = new TouristSpotPagerAdapter();
        touristPager.setAdapter(touristAdapter);
        touristPager.setOffscreenPageLimit(1);
        dotsTourist = view.findViewById(R.id.dots_tourist);

        touristPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            private boolean userDragging = false;
            @Override public void onPageScrollStateChanged(int state) {
                if (state == ViewPager2.SCROLL_STATE_DRAGGING) {
                    userDragging = true;
                    stopAutoSlide();
                } else if (state == ViewPager2.SCROLL_STATE_IDLE && userDragging) {
                    userDragging = false;
                    startAutoSlideIfReady();
                }
            }
            @Override public void onPageSelected(int position) {
                updateTouristDots(position, touristAdapter.getRealCount());
            }
        });

        // --- 축제 RecyclerView & 도트 세팅 ---
        festivalRv = view.findViewById(R.id.recycler_view_festival);
        festivalAdapter = new FestivalAdapter();
        festivalRv.setAdapter(festivalAdapter);
        festivalRv.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        festivalSnapHelper = new PagerSnapHelper();
        festivalSnapHelper.attachToRecyclerView(festivalRv);
        dotsFestival = view.findViewById(R.id.dots_indicator_festival);

        festivalRv.addOnScrollListener(new RecyclerView.OnScrollListener() {
            private boolean userDragging = false;
            @Override public void onScrollStateChanged(@NonNull RecyclerView rv, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                    userDragging = true;
                    festivalStopAutoSlide();
                } else if (newState == RecyclerView.SCROLL_STATE_IDLE && userDragging) {
                    userDragging = false;
                    int pos = getSnappedPosition(festivalRv, festivalSnapHelper);
                    updateFestivalDots(pos == RecyclerView.NO_POSITION ? 0 : pos, festivalAdapter.getItemCount());
                    festivalStartAutoSlideIfReady();
                }
            }
        });

        // --- 데이터 로드 ---
        fetchAllFestivals(festivalAdapter); // 축제
        fetchAllRegions();                  // 관광명소

        // ----- 플로팅 카메라 셋업 -----
        View qrFab = view.findViewById(R.id.qrFab);
        if (qrFab != null) {
            qrFab.setOnClickListener(v -> {
                if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
                        == PackageManager.PERMISSION_GRANTED) {
                    openQrScanner();
                } else {
                    cameraPermForScanner.launch(Manifest.permission.CAMERA);
                }
            });
        }
    }

    private void setupRecyclerView() {
        searchedSpotAdapter = new SearchedSpotAdapter();
        searchResultRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        searchResultRecyclerView.setAdapter(searchedSpotAdapter);
        apiService.fetchBookmarks(new ApiService.Callback<List<BookmarkRes>>() {
            @Override public void onSuccess(List<BookmarkRes> data) {
                savedIds.clear();
                for(BookmarkRes r: data) if (r.externalId != null) savedIds.add(r.externalId);
                searchedSpotAdapter.setSavedIds(savedIds);
            }
            @Override public void onError(Exception e) { }
        });

        searchedSpotAdapter.setOnBookmarkClick((item, willSave) -> {
            if (willSave) {
                apiService.addBookmarkFromTourItem(item, new ApiService.Callback<BookmarkRes>() {
                    @Override public void onSuccess(BookmarkRes data) {
                        if (item.getContentId() != null) savedIds.add(item.getContentId());
                        searchedSpotAdapter.setSavedIds(savedIds);
                    }
                    @Override public void onError(Exception e) { }
                });
            } else {
                apiService.removeBookmark("TOURAPI", item.getContentId(),
                        new ApiService.Callback<Boolean>() {
                            @Override public void onSuccess(Boolean ok) {
                                savedIds.remove(item.getContentId());
                                searchedSpotAdapter.setSavedIds(savedIds);
                            }
                            @Override public void onError(Exception e) { }
                        });
            }
        });
    }

    // 검색 로직
    private static final int[] TARGET_SIGUNGU = {18, 16, 10};

    private void performSearch(String query) {
        String q = (query == null) ? "" : query.trim();
        if (q.isEmpty()) return;

        showLoadingState();

        List<TouristSpotItem> aggregate = Collections.synchronizedList(new ArrayList<>());
        AtomicInteger pending = new AtomicInteger(TARGET_SIGUNGU.length);
        List<Exception> errors = Collections.synchronizedList(new ArrayList<>());

        for (int code : TARGET_SIGUNGU) {
            String url = ApiService.buildSearchKeywordUrl(q, code, 1, 30);

            apiService.fetchTourSpotsAsync(url, new TourCallback() {
                @Override public void onSuccess(List<TouristSpotItem> items) {
                    if (items != null) aggregate.addAll(items);
                    done();
                }
                @Override public void onError(Exception e) {
                    errors.add(e);
                    done();
                }
                private void done() {
                    if (pending.decrementAndGet() == 0) {
                        requireActivity().runOnUiThread(() -> {
                            List<TouristSpotItem> unique = dedupByContentId(aggregate);

                            List<TouristSpotItem> withImg = new ArrayList<>();
                            List<TouristSpotItem> noImg  = new ArrayList<>();
                            for (TouristSpotItem it : unique) {
                                String img = android.text.TextUtils.isEmpty(it.getFirstImage())
                                        ? it.getFirstImage2() : it.getFirstImage();
                                if (android.text.TextUtils.isEmpty(img)) noImg.add(it); else withImg.add(it);
                            }
                            withImg.addAll(noImg);

                            searchedSpotAdapter.submitList(withImg);
                            showSearchResultState();

                            View v = getView();
                            if (v != null) {
                                v.clearFocus();
                                android.view.inputmethod.InputMethodManager imm =
                                        (android.view.inputmethod.InputMethodManager) requireContext()
                                                .getSystemService(android.content.Context.INPUT_METHOD_SERVICE);
                                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                            }

                            if (withImg.isEmpty()) {
                                android.widget.Toast.makeText(requireContext(), "검색 결과가 없습니다.", android.widget.Toast.LENGTH_SHORT).show();
                            } else if (!errors.isEmpty()) {
                                android.widget.Toast.makeText(requireContext(), "일부 지역 조회에 실패하여 일부 결과만 표시합니다.", android.widget.Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
            });
        }
    }

    private List<TouristSpotItem> dedupByContentId(List<TouristSpotItem> items) {
        if (items == null) return Collections.emptyList();
        LinkedHashMap<String, TouristSpotItem> map = new LinkedHashMap<>();
        for (TouristSpotItem it : items) {
            String key = null;
            try {
                String cid = (it.getContentId() != null) ? String.valueOf(it.getContentId()) : null;
                key = (cid != null && !cid.isEmpty())
                        ? cid
                        : (String.valueOf(it.getTitle()) + "|" + String.valueOf(it.getAddr1()));
            } catch (Exception ignore) {}
            if (key == null) key = String.valueOf(it.hashCode());
            map.putIfAbsent(key, it);
        }
        return new ArrayList<>(map.values());
    }

    // UI 상태
    private void showDefaultState() {
        defaultStateContainer.setVisibility(View.VISIBLE);
        searchResultRecyclerView.setVisibility(View.GONE);
        loadingIndicator.setVisibility(View.GONE);
    }
    private void showLoadingState() {
        defaultStateContainer.setVisibility(View.GONE);
        searchResultRecyclerView.setVisibility(View.GONE);
        loadingIndicator.setVisibility(View.VISIBLE);
    }
    private void showSearchResultState() {
        defaultStateContainer.setVisibility(View.GONE);
        searchResultRecyclerView.setVisibility(View.VISIBLE);
        loadingIndicator.setVisibility(View.GONE);
    }

    // 축제 데이터
    private void fetchAllFestivals(FestivalAdapter adapter) {
        List<FestivalItem> all = new ArrayList<>();
        AtomicInteger remain = new AtomicInteger(3);

        FestivalCallback cb = new FestivalCallback() {
            @Override public void onSuccess(List<FestivalItem> items) {
                if (items != null) all.addAll(items);
                if (remain.decrementAndGet() == 0) onAllFestivalsLoaded(all, adapter);
            }
            @Override public void onError(Exception e) {
                if (remain.decrementAndGet() == 0) onAllFestivalsLoaded(all, adapter);
            }
        };

        apiService.fetchFestivalsAsync(ApiService.buildFestivalUrl(18), cb);
        apiService.fetchFestivalsAsync(ApiService.buildFestivalUrl(10), cb);
        apiService.fetchFestivalsAsync(ApiService.buildFestivalUrl(16), cb);
    }

    private void onAllFestivalsLoaded(List<FestivalItem> all, FestivalAdapter adapter) {
        if (all == null) all = new ArrayList<>();
        List<String> seen = new ArrayList<>();
        List<FestivalItem> dedup = new ArrayList<>();
        for (FestivalItem f : all) {
            if (f.getContentId() == null) { dedup.add(f); continue; }
            if (!seen.contains(f.getContentId())) {
                seen.add(f.getContentId());
                dedup.add(f);
            }
        }
        Collections.sort(dedup, (a, b) -> {
            String sa = a.getEventStartDate() == null ? "" : a.getEventStartDate();
            String sb = b.getEventStartDate() == null ? "" : b.getEventStartDate();
            if (sa.isEmpty() && sb.isEmpty()) return 0;
            if (sa.isEmpty()) return 1;
            if (sb.isEmpty()) return -1;
            return sa.compareTo(sb);
        });
        adapter.submitList(dedup);
        setupFestivalDots(adapter.getItemCount());
        int pos = getSnappedPosition(festivalRv, festivalSnapHelper);
        updateFestivalDots(pos == RecyclerView.NO_POSITION ? 0 : pos, adapter.getItemCount());
        festivalStartAutoSlideIfReady();
    }

    // 관광명소 데이터
    private void fetchAllRegions() {
        List<TouristSpotItem> all = new ArrayList<>();
        AtomicInteger remain = new AtomicInteger(3);

        TourCallback cb = new TourCallback() {
            @Override public void onSuccess(List<TouristSpotItem> items) {
                if (items != null) all.addAll(items);
                if (remain.decrementAndGet() == 0) onAllRegionLoaded(all);
            }
            @Override public void onError(Exception e) {
                if (remain.decrementAndGet() == 0) onAllRegionLoaded(all);
            }
        };

        apiService.fetchTourSpotsAsync(ApiService.HOENGSEONG_NATURAL_URL, cb);
        apiService.fetchTourSpotsAsync(ApiService.INJE_NATURAL_URL, cb);
        apiService.fetchTourSpotsAsync(ApiService.HONGCHEON_NATURAL_URL, cb);
    }

    private void onAllRegionLoaded(List<TouristSpotItem> all) {
        if (all.isEmpty()) return;
        Collections.sort(all, new Comparator<TouristSpotItem>() {
            @Override public int compare(TouristSpotItem o1, TouristSpotItem o2) {
                String t1 = o1.getModifiedTime() == null ? "" : o1.getModifiedTime();
                String t2 = o2.getModifiedTime() == null ? "" : o2.getModifiedTime();
                return t2.compareTo(t1);
            }
        });
        List<TouristSpotItem> withImg = new ArrayList<>();
        List<TouristSpotItem> noImg  = new ArrayList<>();
        for (TouristSpotItem it : all) {
            String img = TextUtils.isEmpty(it.getFirstImage()) ? it.getFirstImage2() : it.getFirstImage();
            if (TextUtils.isEmpty(img)) noImg.add(it); else withImg.add(it);
        }
        withImg.addAll(noImg);
        touristAdapter.submitList(withImg);
        setupTouristDots(touristAdapter.getRealCount());
        updateTouristDots(touristPager.getCurrentItem(), touristAdapter.getRealCount());
        startAutoSlideIfReady();
    }

    // 관광명소 도트
    private void setupTouristDots(int total) {
        if (dotsTourist == null) return;
        dotsTourist.removeAllViews();
        int visibleDots = Math.min(total, MAX_DOTS);
        if (visibleDots == 0) return;
        int dotSizePx = dp(8);
        int dotMarginPx = dp(4);
        for (int i = 0; i < visibleDots; i++) {
            View dot = new View(requireContext());
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(dotSizePx, dotSizePx);
            lp.setMargins(dotMarginPx, dotMarginPx, dotMarginPx, dotMarginPx);
            dot.setLayoutParams(lp);
            dot.setBackgroundResource(R.drawable.dot_unselected);
            dotsTourist.addView(dot);
        }
    }

    private void updateTouristDots(int position, int total) {
        if (dotsTourist == null || dotsTourist.getChildCount() == 0 || total == 0) return;
        int visible = Math.min(total, MAX_DOTS);
        int half = visible / 2; // 5 -> 2
        int start = position - half;
        if (start < 0) start = 0;
        if (start > total - visible) start = Math.max(0, total - visible);
        int center = position - start; // 0..visible-1
        boolean hasBefore = start > 0;
        boolean hasAfter  = (start + visible) < total;
        int SIZE_EDGE_MORE = dp(5);
        int SIZE_FAR       = dp(6);
        int SIZE_NEAR      = dp(8);
        int SIZE_CENTER    = dp(10);
        float ALPHA_EDGE_MORE = 0.50f;
        float ALPHA_FAR       = 0.70f;
        float ALPHA_NEAR      = 0.85f;
        float ALPHA_CENTER    = 1.00f;
        for (int i = 0; i < visible; i++) {
            View dot = dotsTourist.getChildAt(i);
            int absDist = Math.abs(i - center);
            int size; float alpha;
            if (absDist == 0) { size = SIZE_CENTER; alpha = ALPHA_CENTER; }
            else if (absDist == 1) { size = SIZE_NEAR; alpha = ALPHA_NEAR; }
            else { size = SIZE_FAR; alpha = ALPHA_FAR; }
            if (i == 0 && hasBefore) { size = Math.min(size, SIZE_EDGE_MORE); alpha = Math.min(alpha, ALPHA_EDGE_MORE); }
            if (i == visible - 1 && hasAfter) { size = Math.min(size, SIZE_EDGE_MORE); alpha = Math.min(alpha, ALPHA_EDGE_MORE); }
            LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) dot.getLayoutParams();
            lp.width = size; lp.height = size; dot.setLayoutParams(lp);
            dot.setAlpha(alpha);
            dot.setBackgroundResource(i == center ? R.drawable.dot_selected : R.drawable.dot_unselected);
            dot.requestLayout();
        }
    }

    // 축제 도트
    private void setupFestivalDots(int total) {
        if (dotsFestival == null) return;
        dotsFestival.removeAllViews();
        int visibleDots = Math.min(total, MAX_DOTS);
        if (visibleDots == 0) return;
        int dotSizePx = dp(8);
        int dotMarginPx = dp(4);
        for (int i = 0; i < visibleDots; i++) {
            View dot = new View(requireContext());
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(dotSizePx, dotSizePx);
            lp.setMargins(dotMarginPx, dotMarginPx, dotMarginPx, dotMarginPx);
            dot.setLayoutParams(lp);
            dot.setBackgroundResource(R.drawable.dot_unselected);
            dotsFestival.addView(dot);
        }
    }

    private void updateFestivalDots(int position, int total) {
        if (dotsFestival == null || dotsFestival.getChildCount() == 0 || total == 0) return;
        int visible = Math.min(total, MAX_DOTS);
        int half = visible / 2;
        int start = position - half;
        if (start < 0) start = 0;
        if (start > total - visible) start = Math.max(0, total - visible);
        int center = position - start;
        boolean hasBefore = start > 0;
        boolean hasAfter  = (start + visible) < total;
        int SIZE_EDGE_MORE = dp(5);
        int SIZE_FAR       = dp(6);
        int SIZE_NEAR      = dp(8);
        int SIZE_CENTER    = dp(10);
        float ALPHA_EDGE_MORE = 0.50f;
        float ALPHA_FAR       = 0.70f;
        float ALPHA_NEAR      = 0.85f;
        float ALPHA_CENTER    = 1.00f;
        for (int i = 0; i < visible; i++) {
            View dot = dotsFestival.getChildAt(i);
            int absDist = Math.abs(i - center);
            int size; float alpha;
            if (absDist == 0) { size = SIZE_CENTER; alpha = ALPHA_CENTER; }
            else if (absDist == 1) { size = SIZE_NEAR; alpha = ALPHA_NEAR; }
            else { size = SIZE_FAR; alpha = ALPHA_FAR; }
            if (i == 0 && hasBefore) { size = Math.min(size, SIZE_EDGE_MORE); alpha = Math.min(alpha, ALPHA_EDGE_MORE); }
            if (i == visible - 1 && hasAfter) { size = Math.min(size, SIZE_EDGE_MORE); alpha = Math.min(alpha, ALPHA_EDGE_MORE); }
            LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) dot.getLayoutParams();
            lp.width = size; lp.height = size; dot.setLayoutParams(lp);
            dot.setAlpha(alpha);
            dot.setBackgroundResource(i == center ? R.drawable.dot_selected : R.drawable.dot_unselected);
            dot.requestLayout();
        }
    }

    // 공통 유틸/생명주기
    private int getSnappedPosition(RecyclerView rv, PagerSnapHelper helper) {
        if (rv == null || helper == null || rv.getLayoutManager() == null) return RecyclerView.NO_POSITION;
        View snap = helper.findSnapView(rv.getLayoutManager());
        return (snap == null) ? RecyclerView.NO_POSITION : rv.getLayoutManager().getPosition(snap);
    }

    private int dp(int v) {
        float d = getResources().getDisplayMetrics().density;
        return (int) (v * d + 0.5f);
    }

    private void startAutoSlideIfReady() {
        stopAutoSlide();
        if (touristAdapter != null && touristAdapter.getRealCount() > 1) {
            autoHandler.postDelayed(autoRunnable, AUTO_DELAY_MS);
        }
    }
    private void stopAutoSlide() {
        autoHandler.removeCallbacksAndMessages(null);
    }

    private void festivalStartAutoSlideIfReady() {
        festivalStopAutoSlide();
        if (festivalAdapter != null && festivalAdapter.getItemCount() > 1) {
            festivalAutoHandler.postDelayed(festivalAutoRunnable, FESTIVAL_AUTO_DELAY_MS);
        }
    }
    private void festivalStopAutoSlide() {
        festivalAutoHandler.removeCallbacksAndMessages(null);
    }

    @Override public void onResume() {
        super.onResume();
        startAutoSlideIfReady();
        festivalStartAutoSlideIfReady();
    }
    @Override public void onPause()  {
        stopAutoSlide();
        festivalStopAutoSlide();
        super.onPause();
    }
    @Override public void onDestroyView() {
        stopAutoSlide();
        festivalStopAutoSlide();
        super.onDestroyView();
    }
}
