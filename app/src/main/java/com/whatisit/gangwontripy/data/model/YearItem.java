package com.whatisit.gangwontripy.data.model;

public class YearItem implements TimelineItem {
    private String year;

    public YearItem(String year) {
        this.year = year;
    }

    public String getYear() {
        return year;
    }

    @Override
    public int getViewType() {
        return TYPE_YEAR;
    }
}