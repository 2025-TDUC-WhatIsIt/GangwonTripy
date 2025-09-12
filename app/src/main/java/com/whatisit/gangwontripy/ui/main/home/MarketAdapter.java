package com.whatisit.gangwontripy.ui.main.home;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.whatisit.gangwontripy.data.model.MarketItem;
import com.whatisit.gangwontripy.R;
import java.util.List;

public class MarketAdapter extends RecyclerView.Adapter<MarketAdapter.MarketViewHolder> {

    private List<MarketItem> marketList;

    public MarketAdapter(List<MarketItem> marketList) {
        this.marketList = marketList;
    }

    @NonNull
    @Override
    public MarketViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // item_market.xml 레이아웃을 inflate(객체화) 합니다.
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_market, parent, false);
        return new MarketViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MarketViewHolder holder, int position) {
        // 현재 position에 맞는 데이터를 가져와서 ViewHolder의 뷰에 설정합니다.
        MarketItem item = marketList.get(position);
        holder.marketName.setText(item.getName());
        holder.marketImage.setImageResource(item.getImageUrl()); // Glide/Picasso 라이브러리 사용을 권장합니다.
    }

    @Override
    public int getItemCount() {
        // 전체 아이템의 개수를 반환합니다.
        return marketList.size();
    }

    // ViewHolder 클래스: 아이템 뷰의 구성요소들을 보관하는 객체입니다.
    public static class MarketViewHolder extends RecyclerView.ViewHolder {
        ImageView marketImage;
        TextView marketName;

        public MarketViewHolder(@NonNull View itemView) {
            super(itemView);
            marketImage = itemView.findViewById(R.id.image_market);
            marketName = itemView.findViewById(R.id.text_market_name);
        }
    }
}