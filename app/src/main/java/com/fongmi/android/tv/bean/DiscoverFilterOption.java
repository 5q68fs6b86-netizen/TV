package com.fongmi.android.tv.bean;

public final class DiscoverFilterOption {

    private final String value;
    private final String label;
    private final String startDate;
    private final String endDate;
    private final boolean selected;

    public DiscoverFilterOption(String value, String label, boolean selected) {
        this(value, label, "", "", selected);
    }

    public DiscoverFilterOption(String value, String label, String startDate, String endDate, boolean selected) {
        this.value = value == null ? "" : value;
        this.label = label == null ? "" : label;
        this.startDate = startDate == null ? "" : startDate;
        this.endDate = endDate == null ? "" : endDate;
        this.selected = selected;
    }

    public String getValue() { return value; }
    public String getLabel() { return label; }
    public String getStartDate() { return startDate; }
    public String getEndDate() { return endDate; }
    public boolean isSelected() { return selected; }
}
