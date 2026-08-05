package com.fongmi.android.tv.bean;

public final class DiscoverRankItem {

    private final int rank;
    private final Vod item;

    public DiscoverRankItem(int rank, Vod item) {
        this.rank = rank;
        this.item = item;
    }

    public int getRank() {
        return rank;
    }

    public Vod getItem() {
        return item;
    }
}
