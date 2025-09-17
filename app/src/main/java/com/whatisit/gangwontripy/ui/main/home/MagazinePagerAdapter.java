package com.whatisit.gangwontripy.ui.main.home;

import android.content.Intent;
import android.net.Uri;
import android.view.*;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.whatisit.gangwontripy.R;
import com.whatisit.gangwontripy.data.model.MagazineRes;
import java.util.ArrayList;
import java.util.List;

public class MagazinePagerAdapter extends RecyclerView.Adapter<MagazinePagerAdapter.VH> {
    private final List<MagazineRes> items = new ArrayList<>();

    public interface OnMagazineClickListener {
        void onClick(MagazineRes item);
    }
    private OnMagazineClickListener listener;
    public void setOnMagazineClickListener(OnMagazineClickListener l){ this.listener = l; }

    public void submitList(List<MagazineRes> list){
        items.clear();
        if (list != null) items.addAll(list);
        notifyDataSetChanged();
    }
    public int getRealCount(){ return items.size(); }

    @NonNull @Override public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_magazine, parent, false);
        return new VH(v);
    }

    @Override public void onBindViewHolder(@NonNull VH h, int pos) {
        MagazineRes m = items.get(pos);
        Glide.with(h.itemView).load(m.imageUrl).into(h.img);
        h.badge.setText((m.season != null ? m.season : "") + (m.topic != null ? " · " + m.topic : ""));
        h.headline.setText(m.headline);
        h.place.setText(m.placeName);

        View.OnClickListener openDetail = v -> {
            if (listener != null) listener.onClick(m);
        };
        h.link.setText(h.itemView.getContext().getString(R.string.more)); // "더보기"
        h.link.setOnClickListener(openDetail);
        h.itemView.setOnClickListener(openDetail);
    }

    @Override public int getItemCount() { return items.size(); }

    static class VH extends RecyclerView.ViewHolder {
        ImageView img;
        TextView badge, headline, place, link;

        VH(View v) {
            super(v);
            img = v.findViewById(R.id.img_cover);
            badge = v.findViewById(R.id.tv_badge);
            headline = v.findViewById(R.id.tv_headline);
            place = v.findViewById(R.id.tv_place);
            link = v.findViewById(R.id.btn_link);
        }
    }
}
