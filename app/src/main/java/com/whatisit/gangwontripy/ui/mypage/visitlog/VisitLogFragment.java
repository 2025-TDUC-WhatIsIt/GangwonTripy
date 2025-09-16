// ui/mypage/visitlog/VisitLogFragment.java
package com.whatisit.gangwontripy.ui.mypage.visitlog;

import static androidx.lifecycle.AndroidViewModel_androidKt.getApplication;

import com.whatisit.gangwontripy.R;

import android.os.Bundle;
import android.view.*;
import androidx.annotation.*;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.*;

import android.widget.TextView;
import android.widget.Toast;

import com.whatisit.gangwontripy.core.SessionManager;
import com.whatisit.gangwontripy.data.api.ApiService;
import com.whatisit.gangwontripy.data.api.VisitApi;
import com.whatisit.gangwontripy.data.model.TimelineItem;
import com.whatisit.gangwontripy.data.model.VisitItem;
import com.whatisit.gangwontripy.data.model.YearItem;

import java.util.*;

public class VisitLogFragment extends Fragment {

    private VisitLogAdapter adapter;
    private ApiService api;
    private Long userId;
    private RecyclerView rv;
    private TextView emptyView;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_visit_log, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        userId = SessionManager.getInstance(requireContext()).getUserId();

        if (userId == null || userId <= 0L) {
            // 로그인 정보 없으면 안내 후 리턴
            emptyView.setText("로그인이 필요합니다.");
            emptyView.setVisibility(View.VISIBLE);
            rv.setVisibility(View.GONE);
            Toast.makeText(requireContext(), "세션 정보가 없습니다. 다시 로그인해 주세요.", Toast.LENGTH_SHORT).show();
            return;
        }
        rv = view.findViewById(R.id.rv_visit_log);
        emptyView = view.findViewById(R.id.text_empty_visit_log);

        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new VisitLogAdapter(new ArrayList<>());
        rv.setAdapter(adapter);

        api = new ApiService(requireContext());

        // ✅ API 호출
        loadTimeline();
    }

    private void loadTimeline() {
        // 필요시 로딩뷰 보여주기
        rv.setVisibility(View.GONE);
        emptyView.setText("불러오는 중...");
        emptyView.setVisibility(View.VISIBLE);

        api.fetchVisitTimeline(userId,200, new ApiService.Callback<List<VisitApi.VisitLogItem>>() {
            @Override public void onSuccess(List<VisitApi.VisitLogItem> items) {
                List<TimelineItem> timeline = buildTimeline(items);
                adapter = new VisitLogAdapter(timeline);
                rv.setAdapter(adapter);

                if (timeline.isEmpty()) {
                    rv.setVisibility(View.GONE);
                    emptyView.setText("방문 기록이 없습니다.");
                    emptyView.setVisibility(View.VISIBLE);
                } else {
                    emptyView.setVisibility(View.GONE);
                    rv.setVisibility(View.VISIBLE);
                }
            }
            @Override public void onError(Exception e) {
                rv.setVisibility(View.GONE);
                emptyView.setText("방문 기록을 불러오지 못했습니다.");
                emptyView.setVisibility(View.VISIBLE);

                android.widget.Toast.makeText(requireContext(),
                        "timeline 실패: " + (e.getMessage() == null ? "" : e.getMessage()),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    /** 서버 응답 → 연도 구분 + 방문 아이템으로 변환 */
    private List<TimelineItem> buildTimeline(List<VisitApi.VisitLogItem> src) {
        if (src == null) return Collections.emptyList();

        // 최신순으로 온다고 가정하지만, 확실히 정렬
        src.sort((a, b) -> safeDate(b.visitedAt).compareTo(safeDate(a.visitedAt)));

        List<TimelineItem> out = new ArrayList<>();
        String curYear = null;
        for (VisitApi.VisitLogItem it : src) {
            String y = yearOf(it.visitedAt);
            if (y != null && !y.equals(curYear)) {
                curYear = y;
                out.add(new YearItem(curYear));
            }
            out.add(new VisitItem(
                    safeTitle(it.title),
                    formatDate(it.visitedAt) // "yyyy.MM.dd"
            ));
        }
        return out;
    }

    private static String safeTitle(String s) { return (s == null || s.isBlank()) ? "방문지" : s; }

    /** "2025-08-10T14:22:31" → "2025.08.10" */
    private static String formatDate(String iso) {
        if (iso == null || iso.length() < 10) return "";
        String yyyy_mm_dd = iso.substring(0, 10); // 2025-08-10
        return yyyy_mm_dd.replace("-", ".");      // 2025.08.10
    }

    /** 연도 추출 */
    private static String yearOf(String iso) {
        if (iso == null || iso.length() < 4) return null;
        return iso.substring(0, 4);
    }

    /** 문자열 비교 안전을 위한 키 */
    private static String safeDate(String iso) {
        // 널/짧은 값도 정렬 가능하도록
        return iso == null ? "" : iso;
    }
}
