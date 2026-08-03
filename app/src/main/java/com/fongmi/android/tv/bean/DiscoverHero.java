package com.fongmi.android.tv.bean;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class DiscoverHero {

    private final List<Vod> items = new ArrayList<>();

    public void replace(List<Vod> values) {
        items.clear();
        if (values != null) items.addAll(values);
    }

    public List<Vod> getItems() {
        return Collections.unmodifiableList(items);
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }
}
