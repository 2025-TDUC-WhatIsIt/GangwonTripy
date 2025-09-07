package com.example.gangwontripy.ui.faq;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.gangwontripy.R;
import com.example.gangwontripy.data.model.FaqItem;
import java.util.List;

public class FaqAdapter extends RecyclerView.Adapter<FaqAdapter.FaqViewHolder> {

    private final List<FaqItem> faqList;

    public FaqAdapter(List<FaqItem> faqList) {
        this.faqList = faqList;
    }

    @NonNull
    @Override
    public FaqViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_faq, parent, false);
        return new FaqViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FaqViewHolder holder, int position) {
        FaqItem currentItem = faqList.get(position);
        holder.bind(currentItem);
    }

    @Override
    public int getItemCount() {
        return faqList.size();
    }

    class FaqViewHolder extends RecyclerView.ViewHolder {
        private final TextView titleTextView;
        private final TextView dateTextView;
        private final TextView contentTextView;
        private final ImageView arrowImageView;
        private final LinearLayout itemLayout;

        public FaqViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.tv_faq_title);
            dateTextView = itemView.findViewById(R.id.tv_faq_date);
            contentTextView = itemView.findViewById(R.id.tv_faq_content);
            arrowImageView = itemView.findViewById(R.id.iv_arrow);
            itemLayout = itemView.findViewById(R.id.faq_item_layout);
        }

        void bind(FaqItem item) {
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