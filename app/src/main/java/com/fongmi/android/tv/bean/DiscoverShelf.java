package com.fongmi.android.tv.bean;

import java.util.ArrayList;
import java.util.List;

public class DiscoverShelf {

    public enum Type { FEATURE, RANKING }

    private final Type type;
    private final String title;
    private final String facetKind;
    private final List<Vod> items;

    public DiscoverShelf(Type type, String title, String facetKind, List<Vod> items) {
        this.type = type;
        this.title = title;
        this.facetKind = facetKind;
        this.items = new ArrayList<>(items);
    }

    public Type getType() {
        return type;
    }

    public String getTitle() {
        return title;
    }

    public String getFacetKind() {
        return facetKind;
    }

    public List<Vod> getItems() {
        return items;
    }
}
