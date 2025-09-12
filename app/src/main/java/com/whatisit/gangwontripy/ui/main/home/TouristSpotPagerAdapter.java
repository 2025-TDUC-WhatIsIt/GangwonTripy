package com.whatisit.gangwontripy.ui.main.home;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.whatisit.gangwontripy.R;
import com.whatisit.gangwontripy.data.model.TouristSpotItem;

import java.util.ArrayList;
import java.util.List;

public class TouristSpotPagerAdapter extends RecyclerView.Adapter<TouristSpotPagerAdapter.VH> {

    private final List<TouristSpotItem> data = new ArrayList<>();

    public void submitList(List<TouristSpotItem> items) {
        data.clear();
        if (items != null) data.addAll(items);
        notifyDataSetChanged();
    }

    public int getRealCount() {
        return data.size();
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_tourist_spot, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        TouristSpotItem item = data.get(position);
        h.txtTitle.setText(TextUtils.isEmpty(item.getTitle()) ? "제목 없음" : item.getTitle());

        StringBuilder sb = new StringBuilder();
        if (!TextUtils.isEmpty(item.getAddr1())) sb.append(item.getAddr1());
        if (!TextUtils.isEmpty(item.getAddr2())) {
            if (sb.length() > 0) sb.append(" ");
            sb.append(item.getAddr2());
        }
        h.txtAddr.setText(sb.length() == 0 ? "주소 정보 없음" : sb.toString());

        String img = !TextUtils.isEmpty(item.getFirstImage()) ? item.getFirstImage() : item.getFirstImage2();
        if (!TextUtils.isEmpty(img)) {
            Glide.with(h.img.getContext())
                    .load(img)
                    .placeholder(R.drawable.img_rectangle4)
                    .error(R.drawable.image_gone)
                    .centerCrop()
                    .into(h.img);
        } else {
            h.img.setImageResource(R.drawable.image_gone);
        }
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        ImageView img;
        TextView txtTitle, txtAddr;
        VH(@NonNull View v) {
            super(v);
            img = v.findViewById(R.id.imgCard);
            txtTitle = v.findViewById(R.id.txtTitle);
            txtAddr = v.findViewById(R.id.txtAddr);
        }
    }
}
