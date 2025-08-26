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
            // contentId를 고유 식별자로 사용
            return oldItem.getContentId().equals(newItem.getContentId());
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
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_spot, parent, false);
        return new SpotViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SpotViewHolder holder, int position) {
        TouristSpotItem currentSpot = getItem(position);
        holder.bind(currentSpot);
    }

    // ViewHolder 클래스: bind 메소드를 TouristSpotItem에 맞게 수정
    static class SpotViewHolder extends RecyclerView.ViewHolder {
        private final ImageView mainImageView;
        private final GridLayout gridLayoutImages;
        private final TextView spotNameTextView;
        private final TextView addressTextView;
        private final TextView operatingHoursTextView; // 이 뷰는 운영시간 정보가 없으므로 숨기거나 다른 정보로 대체
        private final ImageView bookmarkImageView;

        public SpotViewHolder(@NonNull View itemView) {
            super(itemView);
            mainImageView = itemView.findViewById(R.id.iv_main_image);
            gridLayoutImages = itemView.findViewById(R.id.grid_layout_images);
            spotNameTextView = itemView.findViewById(R.id.tv_spot_name);
            addressTextView = itemView.findViewById(R.id.tv_address);
            operatingHoursTextView = itemView.findViewById(R.id.tv_operating_hours);
            bookmarkImageView = itemView.findViewById(R.id.iv_bookmark);
        }

        // bind 메소드를 현재 TouristSpotItem 모델에 맞게 수정
        public void bind(TouristSpotItem spot) {
            spotNameTextView.setText(spot.getTitle());
            addressTextView.setText(spot.getAddr1()); // 주소는 addr1을 사용

            // 운영 시간 정보가 없으므로, 해당 TextView를 숨기거나 비워둠
            operatingHoursTextView.setVisibility(View.GONE);
            // 또는 operatingHoursTextView.setText("");

            // 대표 이미지 로드
            Glide.with(itemView.getContext())
                    .load(spot.getFirstImage()) // firstImage를 대표 이미지로 사용
                    .placeholder(R.color.gray)
                    .error(R.drawable.image_placeholder) // 이미지 로드 실패 시 보여줄 이미지
                    .into(mainImageView);

            // 작은 이미지 4개 로드 (firstImage2와 다른 이미지들이 필요)
            // 현재 모델에는 작은 이미지가 하나뿐이므로, 일단 firstImage2만 로드하는 예시
            ImageView subImageView1 = (ImageView) gridLayoutImages.getChildAt(0);
            if (spot.getFirstImage2() != null && !spot.getFirstImage2().isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(spot.getFirstImage2())
                        .placeholder(R.color.gray)
                        .into(subImageView1);
            } else {
                // 이미지가 없으면 숨김
                subImageView1.setVisibility(View.INVISIBLE);
            }

            // 나머지 3개 작은 이미지는 일단 숨김 처리
            ((ImageView) gridLayoutImages.getChildAt(1)).setVisibility(View.INVISIBLE);
            ((ImageView) gridLayoutImages.getChildAt(2)).setVisibility(View.INVISIBLE);
            ((ImageView) gridLayoutImages.getChildAt(3)).setVisibility(View.INVISIBLE);
        }
    }
}