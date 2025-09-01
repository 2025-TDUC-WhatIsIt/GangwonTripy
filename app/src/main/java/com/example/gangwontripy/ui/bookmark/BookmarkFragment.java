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

import java.util.List;

public class BookmarkFragment extends Fragment {
    private final ApiService api = new ApiService();
    private BookmarkAdapter adapter;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater i, @Nullable ViewGroup c, @Nullable Bundle s){
        return i.inflate(R.layout.fragment_recycler_only, c, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle s) {
        RecyclerView rv = v.findViewById(R.id.recycler);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new BookmarkAdapter();
        rv.setAdapter(adapter);

        api.fetchBookmarks(new ApiService.Callback<List<BookmarkRes>>() {
            @Override public void onSuccess(List<BookmarkRes> data) { adapter.submitList(data); }
            @Override public void onError(Exception e) { /* 토스트 */ }
        });
    }
}
