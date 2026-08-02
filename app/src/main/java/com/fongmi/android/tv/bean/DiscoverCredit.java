package com.fongmi.android.tv.bean;

public final class DiscoverCredit {

    private final String name;
    private final String character;
    private final String profile;

    public DiscoverCredit(String name, String character, String profile) {
        this.name = name == null ? "" : name;
        this.character = character == null ? "" : character;
        this.profile = profile == null ? "" : profile;
    }

    public String getName() {
        return name;
    }

    public String getCharacter() {
        return character;
    }

    public String getProfile() {
        return profile;
    }
}
