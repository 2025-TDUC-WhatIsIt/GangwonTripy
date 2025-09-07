// app/java/.../ui/bookmark/BookmarkFragment.java
package com.example.gangwontripy.ui.bookmark;

import android.os.Bundle;
import android.view.*;
import androidx.annotation.*;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.gangwontripy.R;
import com.example.gangwontripy.data.api.ApiService;
import com.example.gangwontripy.data.model.BookmarkRes;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

// BookmarkFragment.java
// ui/bookmark/BookmarkFragment.java
public class BookmarkFragment extends Fragment {
    private ApiService api;
    private BookmarkAdapter adapter;
    private final List<BookmarkRes> full = new ArrayList<>();

    // 필터를 String으로 쓰고 있으니 유지
    private static final String SIG_HONGCHEON = "16";
    private static final String SIG_HOENGSEONG = "18";
    private static final String SIG_INJE = "10";

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater i, @Nullable ViewGroup c, @Nullable Bundle s){
        return i.inflate(R.layout.fragment_recycler_only, c, false);
    }

    private @Nullable String currentFilter = null;

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle s) {
        api = new ApiService(requireContext());
        RecyclerView rv = v.findViewById(R.id.recycler);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new BookmarkAdapter((item, position) -> confirmAndRemove(item));
        rv.setAdapter(adapter);

        v.findViewById(R.id.chipAll).setOnClickListener(x -> applyFilter(null));
        v.findViewById(R.id.chipHongcheon).setOnClickListener(x -> applyFilter(SIG_HONGCHEON));
        v.findViewById(R.id.chipHoengseong).setOnClickListener(x -> applyFilter(SIG_HOENGSEONG));
        v.findViewById(R.id.chipInje).setOnClickListener(x -> applyFilter(SIG_INJE));

        api.fetchBookmarks(new ApiService.Callback<List<BookmarkRes>>() {
            @Override public void onSuccess(List<BookmarkRes> data) {
                full.clear();
                if (data != null) full.addAll(data);
                applyFilter(null);
            }
            @Override public void onError(Exception e) {
                android.widget.Toast.makeText(requireContext(), "북마크 불러오기 실패", android.widget.Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void applyFilter(@Nullable String sigungu) {
        currentFilter = sigungu;
        if (sigungu == null) {
            adapter.submitList(new ArrayList<>(full));
            return;
        }
        List<BookmarkRes> filtered = new ArrayList<>();
        for (BookmarkRes b : full) {
            if (b.sigunguCode != null && sigungu.equals(String.valueOf(b.sigunguCode))) {
                filtered.add(b);
            }
        }
        adapter.submitList(filtered);
    }

    private void confirmAndRemove(BookmarkRes item) {
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("북마크 해제")
                .setMessage("이 장소를 북마크에서 삭제할까요?")
                .setNegativeButton("취소", (d, w) -> { /* no-op */ })
                .setPositiveButton("삭제", (d, w) -> doRemove(item))
                .show();
    }

    private void doRemove(BookmarkRes item) {
        if (item.provider == null || item.externalId == null) {
            android.widget.Toast.makeText(requireContext(), "삭제 정보가 올바르지 않습니다.", android.widget.Toast.LENGTH_SHORT).show();
            // UI 복구를 위해 전체 리스트 재적용
            applyFilter(currentFilter);
            return;
        }
        api.removeBookmark(item.provider, item.externalId, new ApiService.Callback<Boolean>() {
            @Override public void onSuccess(Boolean ok) {
                // full에서 제거
                for (int i = 0; i < full.size(); i++) {
                    BookmarkRes b = full.get(i);
                    if (Objects.equals(b.id, item.id)) { // id가 가장 안전
                        full.remove(i);
                        break;
                    }
                }
                applyFilter(currentFilter);
                android.widget.Toast.makeText(requireContext(), "북마크가 삭제되었습니다.", android.widget.Toast.LENGTH_SHORT).show();
            }
            @Override public void onError(Exception e) {
                android.widget.Toast.makeText(requireContext(), "삭제 실패: " + e.getMessage(), android.widget.Toast.LENGTH_SHORT).show();
                // 실패 시에도 최신 데이터로 복구
                applyFilter(currentFilter);
            }
        });
    }
}
