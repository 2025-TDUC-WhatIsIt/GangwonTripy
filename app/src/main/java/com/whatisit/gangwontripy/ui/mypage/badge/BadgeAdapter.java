package com.whatisit.gangwontripy.ui.mypage.badge;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.whatisit.gangwontripy.data.model.BadgeItem;
import com.whatisit.gangwontripy.R;

import java.util.List;

public class BadgeAdapter extends RecyclerView.Adapter<BadgeAdapter.BadgeViewHolder> {

    private final List<BadgeItem> badgeList;
    private final OnBadgeClickListener listener; // 클릭 리스너 인터페이스

    public interface OnBadgeClickListener {
        void onBadgeClick(BadgeItem badge);
    }

    public BadgeAdapter(List<BadgeItem> badgeList, OnBadgeClickListener listener) {
        this.badgeList = badgeList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public BadgeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.item_badge, parent, false);
        return new BadgeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BadgeViewHolder holder, int position) {
        BadgeItem currentItem = badgeList.get(position);
        holder.bind(currentItem, listener);
    }

    @Override
    public int getItemCount() {
        int count = (badgeList != null) ? badgeList.size() : 0;
        Log.d("BadgeAdapter", "getItemCount: " + count);
        return count;
    }

    static class BadgeViewHolder extends RecyclerView.ViewHolder {
        ImageView badgeImageView;

        public BadgeViewHolder(@NonNull View itemView) {
            super(itemView);
            badgeImageView = itemView.findViewById(R.id.iv_badge_image);
        }

        void bind(BadgeItem item, OnBadgeClickListener listener) {
            if (item.isAcquired()) {
                badgeImageView.setImageResource(item.getImageResourceId());
                badgeImageView.setAlpha(1.0f); // 획득 시 선명하게
            } else {
                badgeImageView.setImageResource(R.drawable.temp_ic_badge_locked); // 미획득 시 잠금 이미지
                badgeImageView.setAlpha(0.5f); // 미획득 시 흐리게
            }

            itemView.setOnClickListener(v -> {
                if(item.isAcquired()){ // 획득한 배지만 클릭 가능
                    listener.onBadgeClick(item);
                }
            });
        }
    }
}