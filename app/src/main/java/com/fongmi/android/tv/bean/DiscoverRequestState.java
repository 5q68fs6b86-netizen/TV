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
        addAllAndGetAdded(requestGeneration, values);
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

    public List<Vod> addAllAndGetAdded(int requestGeneration, List<Vod> values) {
        List<Vod> added = new ArrayList<>();
        if (!accepts(requestGeneration) || values == null) return added;
        for (Vod item : values) {
            if (item == null || item.getId().isEmpty() || items.containsKey(item.getId())) continue;
            items.put(item.getId(), item);
            added.add(item);
        }
        return added;
    }
}
