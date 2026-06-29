package com.fongmi.android.tv.bean;

import java.util.Collections;
import java.util.List;

public class FeaturedVodRow {

    private final List<Vod> items;

    public static FeaturedVodRow create(List<Vod> items) {
        return new FeaturedVodRow(items);
    }

    private FeaturedVodRow(List<Vod> items) {
        this.items = items == null ? Collections.emptyList() : items;
    }

    public List<Vod> getItems() {
        return items;
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }

    public int size() {
        return items.size();
    }

    public Vod get(int index) {
        return items.get(index);
    }
}
