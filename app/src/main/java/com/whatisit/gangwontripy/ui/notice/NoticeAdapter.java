package com.whatisit.gangwontripy.ui.notice;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.whatisit.gangwontripy.R;
import com.whatisit.gangwontripy.data.model.NoticeItem;
import java.util.List;

public class NoticeAdapter extends RecyclerView.Adapter<NoticeAdapter.NoticeViewHolder> {

    private final List<NoticeItem> noticeList;

    public NoticeAdapter(List<NoticeItem> noticeList) {
        this.noticeList = noticeList;
    }

    @NonNull
    @Override
    public NoticeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notice, parent, false);
        return new NoticeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NoticeViewHolder holder, int position) {
        NoticeItem currentItem = noticeList.get(position);
        holder.bind(currentItem);
    }

    @Override
    public int getItemCount() {
        return noticeList.size();
    }

    class NoticeViewHolder extends RecyclerView.ViewHolder {
        private final TextView titleTextView;
        private final TextView dateTextView;
        private final TextView contentTextView;
        private final ImageView arrowImageView;
        private final LinearLayout itemLayout;

        public NoticeViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.tv_notice_title);
            dateTextView = itemView.findViewById(R.id.tv_notice_date);
            contentTextView = itemView.findViewById(R.id.tv_notice_content);
            arrowImageView = itemView.findViewById(R.id.iv_arrow);
            itemLayout = itemView.findViewById(R.id.notice_item_layout);
        }

        void bind(NoticeItem item) {
            // 데이터 설정
            String titleWithCategory = item.getCategory() + " " + item.getTitle();
            titleTextView.setText(titleWithCategory);
            dateTextView.setText(item.getDate());
            contentTextView.setText(item.getContent());

            // isExpanded 상태에 따라 답변 영역의 visibility와 화살표 방향을 설정
            contentTextView.setVisibility(item.isExpanded() ? View.VISIBLE : View.GONE);
            arrowImageView.setRotation(item.isExpanded() ? 180f : 0f);

            // 아이템 클릭 리스너 설정
            itemLayout.setOnClickListener(v -> {
                // 현재 아이템의 isExpanded 상태를 반전시킴
                item.setExpanded(!item.isExpanded());
                // 변경된 아이템 하나만 갱신하여 애니메이션 효과를 줌
                notifyItemChanged(getAdapterPosition());
            });
        }
    }
}