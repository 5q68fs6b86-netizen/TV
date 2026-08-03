package com.fongmi.android.tv.bean;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class DiscoverFilterPanel {

    private final List<List<DiscoverFilterOption>> rows = new ArrayList<>();

    public DiscoverFilterPanel() {
        for (int i = 0; i < 5; i++) rows.add(Collections.emptyList());
    }

    public void setRow(int row, List<DiscoverFilterOption> options) {
        rows.set(row, options == null ? Collections.emptyList() : new ArrayList<>(options));
    }

    public List<DiscoverFilterOption> getRow(int row) {
        return Collections.unmodifiableList(rows.get(row));
    }
}
