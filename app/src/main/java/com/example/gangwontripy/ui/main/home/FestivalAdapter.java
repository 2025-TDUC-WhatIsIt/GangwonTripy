package com.example.gangwontripy.ui.main.home;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.gangwontripy.R;
import com.example.gangwontripy.data.model.FestivalItem;

import java.util.ArrayList;
import java.util.List;

public class FestivalAdapter extends RecyclerView.Adapter<FestivalAdapter.VH> {
    private final List<FestivalItem> data = new ArrayList<>();

    public void submitList(List<FestivalItem> items) {
        data.clear();
        if (items != null) data.addAll(items);
        notifyDataSetChanged();
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_festival, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        FestivalItem it = data.get(position);
        h.txtTitle.setText(TextUtils.isEmpty(it.getTitle()) ? "축제" : it.getTitle());
        h.txtDate.setText(buildDate(it.getEventStartDate(), it.getEventEndDate()));

        StringBuilder addr = new StringBuilder();
        if (!TextUtils.isEmpty(it.getAddr1())) addr.append(it.getAddr1());
        if (!TextUtils.isEmpty(it.getAddr2())) {
            if (addr.length() > 0) addr.append(" ");
            addr.append(it.getAddr2());
        }
        h.txtAddr.setText(addr.length() == 0 ? "주소 정보 없음" : addr.toString());
        h.txtTel.setText(TextUtils.isEmpty(it.getTel()) ? "" : it.getTel());

        String img = !TextUtils.isEmpty(it.getFirstImage()) ? it.getFirstImage() : it.getFirstImage2();
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
    public int getItemCount() { return data.size(); }

    static class VH extends RecyclerView.ViewHolder {
        ImageView img;
        TextView txtTitle, txtDate, txtAddr, txtTel;
        VH(@NonNull View v) {
            super(v);
            img = v.findViewById(R.id.imgFestival);
            txtTitle = v.findViewById(R.id.txtTitle);
            txtDate  = v.findViewById(R.id.txtDate);
            txtAddr  = v.findViewById(R.id.txtAddr);
            txtTel   = v.findViewById(R.id.txtTel);
        }
    }

    private String buildDate(String s, String e) {
        if (TextUtils.isEmpty(s) && TextUtils.isEmpty(e)) return "일정 미정";
        String fs = formatYmd(s);
        String fe = formatYmd(e);

        // 2025.10.22 ~ 10.26 (연도/월이 같으면 생략)
        if (!TextUtils.isEmpty(fs) && !TextUtils.isEmpty(fe)) {
            if (fs.substring(0, 7).equals(fe.substring(0, 7))) {
                // 같은 연/월
                return fs + " ~ " + fe.substring(8);
            }
            return fs + " ~ " + fe;
        } else if (!TextUtils.isEmpty(fs)) {
            return fs + " ~";
        } else {
            return "~ " + fe;
        }
    }

    private String formatYmd(String ymd) {
        if (TextUtils.isEmpty(ymd) || ymd.length() != 8) return "";
        // yyyymmdd -> yyyy.MM.dd
        return ymd.substring(0,4) + "." + ymd.substring(4,6) + "." + ymd.substring(6,8);
    }
}
