package com.example.gangwontripy.ui.directions;

import com.example.gangwontripy.BuildConfig;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.gangwontripy.R;
import com.kakao.vectormap.KakaoMap;
import com.kakao.vectormap.KakaoMapReadyCallback;
import com.kakao.vectormap.KakaoMapSdk;
import com.kakao.vectormap.LatLng;
import com.kakao.vectormap.MapLifeCycleCallback;
import com.kakao.vectormap.MapType;
import com.kakao.vectormap.MapView;
import com.kakao.vectormap.MapViewInfo;

public class DirectionsFragment extends Fragment {

    private MapView mapView;
    private KakaoMap kakaoMap;

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
                Log.d("KakaoMap", "지도 로딩 완료");
            }

            @Override
            public LatLng getPosition() {
                return LatLng.from(37.5665, 126.9780); // 서울
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
