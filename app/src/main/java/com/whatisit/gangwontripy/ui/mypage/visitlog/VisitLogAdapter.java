package com.whatisit.gangwontripy.ui.mypage.visitlog;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.whatisit.gangwontripy.R;
import com.whatisit.gangwontripy.data.model.TimelineItem;
import com.whatisit.gangwontripy.data.model.VisitItem;
import com.whatisit.gangwontripy.data.model.YearItem;
import java.util.List;

public class VisitLogAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final List<TimelineItem> items;

    public VisitLogAdapter(List<TimelineItem> items) {
        this.items = items;
    }

    @Override
    public int getItemViewType(int position) {
        return items.get(position).getViewType();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == TimelineItem.TYPE_YEAR) {
            View view = inflater.inflate(R.layout.item_timeline_year, parent, false);
            return new YearViewHolder(view);
        } else { // viewType == TimelineItem.TYPE_VISIT
            View view = inflater.inflate(R.layout.item_timeline_visit, parent, false);
            return new VisitViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == TimelineItem.TYPE_YEAR) {
            YearItem yearItem = (YearItem) items.get(position);
            ((YearViewHolder) holder).bind(yearItem);
        } else {
            VisitItem visitItem = (VisitItem) items.get(position);
            ((VisitViewHolder) holder).bind(visitItem);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    // 연도 ViewHolder
    static class YearViewHolder extends RecyclerView.ViewHolder {
        TextView yearTextView;
        YearViewHolder(@NonNull View itemView) {
            super(itemView);
            yearTextView = itemView.findViewById(R.id.tv_year);
        }
        void bind(YearItem item) {
            yearTextView.setText(item.getYear());
        }
    }

    // 방문 기록 ViewHolder
    static class VisitViewHolder extends RecyclerView.ViewHolder {
        TextView placeNameTextView;
        TextView visitDateTextView;
        VisitViewHolder(@NonNull View itemView) {
            super(itemView);
            placeNameTextView = itemView.findViewById(R.id.tv_place_name);
            visitDateTextView = itemView.findViewById(R.id.tv_visit_date);
        }
        void bind(VisitItem item) {
            placeNameTextView.setText(item.getPlaceName());
            visitDateTextView.setText(item.getDate());
        }
    }
}