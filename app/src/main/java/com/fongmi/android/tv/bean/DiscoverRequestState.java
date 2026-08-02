package com.fongmi.android.tv.bean;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public final class DiscoverRequestState {

    private final LinkedHashMap<String, Vod> items = new LinkedHashMap<>();
    private int generation;

    public int reset() {
        items.clear();
        return ++generation;
    }

    public int getGeneration() {
        return generation;
    }

    public boolean accepts(int requestGeneration) {
        return generation == requestGeneration;
    }

    public void addAll(int requestGeneration, List<Vod> values) {
        if (!accepts(requestGeneration) || values == null) return;
        for (Vod item : values) if (item != null && !item.getId().isEmpty()) items.putIfAbsent(item.getId(), item);
    }

    public List<Vod> getItems() {
        return new ArrayList<>(items.values());
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }

    public int size() {
        return items.size();
    }
}
