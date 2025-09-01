package com.example.gangwontripy.ui.spot;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.gangwontripy.R;
import com.example.gangwontripy.data.model.TouristSpotItem; // 모델 클래스를 올바르게 import

public class SearchedSpotAdapter extends ListAdapter<TouristSpotItem, SearchedSpotAdapter.SpotViewHolder> {

    // DiffUtil.ItemCallback: TouristSpotItem 클래스에 맞춰 수정
    private static final DiffUtil.ItemCallback<TouristSpotItem> DIFF_CALLBACK = new DiffUtil.ItemCallback<TouristSpotItem>() {
        @Override
        public boolean areItemsTheSame(@NonNull TouristSpotItem oldItem, @NonNull TouristSpotItem newItem) {
            String a = oldItem.getContentId() == null ? "" : oldItem.getContentId();
            String b = newItem.getContentId() == null ? "" : newItem.getContentId();
            return a.equals(b);
        }

        @Override
        public boolean areContentsTheSame(@NonNull TouristSpotItem oldItem, @NonNull TouristSpotItem newItem) {
            // 위에서 추가한 equals() 메소드를 사용하여 내용 비교
            return oldItem.equals(newItem);
        }
    };

    // 생성자 (변경 없음)
    public SearchedSpotAdapter() {
        super(DIFF_CALLBACK);
    }

    @NonNull
    @Override
    public SpotViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_spot, parent, false);
        return new SpotViewHolder(v, this); // ← 어댑터 참조 주입
    }

    @Override
    public void onBindViewHolder(@NonNull SpotViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    static class SpotViewHolder extends RecyclerView.ViewHolder {
        private final SearchedSpotAdapter owner; // ← 어댑터 참조
        private final ImageView mainImageView, bookmarkImageView;
        private final GridLayout gridLayoutImages;
        private final TextView spotNameTextView, addressTextView, operatingHoursTextView;

        SpotViewHolder(@NonNull View itemView, SearchedSpotAdapter owner) {
            super(itemView);
            this.owner = owner;
            mainImageView = itemView.findViewById(R.id.iv_main_image);
            gridLayoutImages = itemView.findViewById(R.id.grid_layout_images);
            spotNameTextView = itemView.findViewById(R.id.tv_spot_name);
            addressTextView = itemView.findViewById(R.id.tv_address);
            operatingHoursTextView = itemView.findViewById(R.id.tv_operating_hours);
            bookmarkImageView = itemView.findViewById(R.id.iv_bookmark);
        }

        void bind(TouristSpotItem spot) {
            android.util.Log.d("BookmarkClick", "clicked contentId=" + spot.getContentId());
            spotNameTextView.setText(spot.getTitle());
            addressTextView.setText(spot.getAddr1());
            operatingHoursTextView.setVisibility(View.GONE);

            String main = (spot.getFirstImage()==null||spot.getFirstImage().isEmpty())
                    ? spot.getFirstImage2() : spot.getFirstImage();

            Glide.with(itemView.getContext())
                    .load(main)
                    .placeholder(R.drawable.image_placeholder)
                    .error(R.drawable.image_placeholder)
                    .into(mainImageView);

            // 서브 이미지 숨김
            for (int i=0;i<4;i++) ((ImageView)gridLayoutImages.getChildAt(i)).setVisibility(View.INVISIBLE);

            // 현재 저장상태 반영
            boolean saved = owner.savedIds.contains(spot.getContentId());
            bookmarkImageView.setImageResource(saved ? R.drawable.ic_bookmark_filled
                    : R.drawable.ic_bookmark_border);

            bookmarkImageView.setOnClickListener(v -> {
                if (owner.bookmarkClick == null) return;

                String id = spot.getContentId();
                if (id == null || id.isEmpty()) return;

                boolean wasSaved = owner.savedIds.contains(id);
                if (wasSaved) owner.savedIds.remove(id); else owner.savedIds.add(id);

                int pos = getAdapterPosition();              // ← 여기만 변경
                if (pos != RecyclerView.NO_POSITION) {
                    owner.notifyItemChanged(pos);
                }

                owner.bookmarkClick.onClick(spot, !wasSaved);
            });

        }
    }
    public interface OnBookmarkClick {
        void onClick(TouristSpotItem item, boolean willSave); // true면 저장, false면 삭제
    }

    private OnBookmarkClick bookmarkClick;
    private java.util.Set<String> savedIds = new java.util.HashSet<>(); // 이미 저장된 externalId들(contentId)

    public void setOnBookmarkClick(OnBookmarkClick cb){ this.bookmarkClick = cb; }
    public void setSavedIds(java.util.Set<String> ids){
        this.savedIds = (ids == null) ? new java.util.HashSet<>() : ids;
        notifyDataSetChanged();
    }
}