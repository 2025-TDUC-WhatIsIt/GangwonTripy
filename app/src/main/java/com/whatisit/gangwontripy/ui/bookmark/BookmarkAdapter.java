package com.whatisit.gangwontripy.ui.bookmark;

import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.whatisit.gangwontripy.R;
import com.whatisit.gangwontripy.data.model.BookmarkRes;

import java.util.ArrayList;
import java.util.List;

public class BookmarkAdapter extends RecyclerView.Adapter<BookmarkAdapter.VH> {
    public interface OnBookmarkActionListener {
        void onUnbookmark(BookmarkRes item, int position);
    }
    private final List<BookmarkRes> data = new ArrayList<>();
    private final OnBookmarkActionListener actionListener;
    public BookmarkAdapter(OnBookmarkActionListener listener) {
        this.actionListener = listener;
    }
    public void submitList(List<BookmarkRes> list){
        data.clear();
        if (list != null) data.addAll(list);
        notifyDataSetChanged();
    }

    @NonNull @Override public VH onCreateViewHolder(@NonNull ViewGroup p, int v) {
        View view = LayoutInflater.from(p.getContext()).inflate(R.layout.item_spot, p, false);
        return new VH(view, actionListener);
    }
    @Override public void onBindViewHolder(@NonNull VH h, int pos) { h.bind(data.get(pos)); }
    @Override public int getItemCount() { return data.size(); }

    static class VH extends RecyclerView.ViewHolder {
        ImageView main, sub1, sub2, sub3, sub4, bookmarkIcon;
        GridLayout grid;
        TextView name, addr, hours;

        private final OnBookmarkActionListener listener;
        private boolean removing = false;

        VH(@NonNull View v, OnBookmarkActionListener l){
            super(v);
            listener = l;
            main = v.findViewById(R.id.iv_main_image);
            grid = v.findViewById(R.id.grid_layout_images);
            sub1 = (ImageView) grid.getChildAt(0);
            sub2 = (ImageView) grid.getChildAt(1);
            sub3 = (ImageView) grid.getChildAt(2);
            sub4 = (ImageView) grid.getChildAt(3);
            name = v.findViewById(R.id.tv_spot_name);
            addr = v.findViewById(R.id.tv_address);
            hours = v.findViewById(R.id.tv_operating_hours);
            bookmarkIcon = v.findViewById(R.id.iv_bookmark);
        }
        void bind(BookmarkRes b){
            name.setText(b.placeName);
            addr.setText(b.address == null ? "" : b.address);
            hours.setVisibility(View.GONE);
            Glide.with(itemView.getContext())
                    .load(b.thumbnailUrl)
                    .placeholder(R.drawable.image_placeholder)
                    .error(R.drawable.image_placeholder)
                    .into(main);
            // 서브 이미지 숨김
            sub1.setVisibility(View.INVISIBLE);
            sub2.setVisibility(View.INVISIBLE);
            sub3.setVisibility(View.INVISIBLE);
            sub4.setVisibility(View.INVISIBLE);

            bookmarkIcon.setImageResource(R.drawable.ic_bookmark_filled);bookmarkIcon.setAlpha(removing ? 0.5f : 1f);
            bookmarkIcon.setEnabled(!removing);

            bookmarkIcon.setOnClickListener(v -> {
                if (removing) return;
                int pos = getAdapterPosition();
                if (pos == RecyclerView.NO_POSITION) return;
                removing = true;
                bookmarkIcon.setAlpha(0.5f);
                bookmarkIcon.setEnabled(false);
                if (listener != null) listener.onUnbookmark(b, pos);
            });
        }
    }
}
