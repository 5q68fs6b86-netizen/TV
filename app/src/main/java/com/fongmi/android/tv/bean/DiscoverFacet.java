package com.fongmi.android.tv.bean;

import android.os.Parcel;
import android.os.Parcelable;

public class DiscoverFacet implements Parcelable {

    public static final String GENRE = "genre";
    public static final String COMPANY = "company";
    public static final String PROVIDER = "provider";
    public static final String TOP_MOVIE = "top_movie";
    public static final String TOP_TV = "top_tv";
    public static final String NOW_PLAYING = "now_playing";

    private final String kind;
    private final String id;
    private final String name;
    private final String image;

    public DiscoverFacet(String kind, String id, String name, String image) {
        this.kind = kind;
        this.id = id;
        this.name = name;
        this.image = image;
    }

    protected DiscoverFacet(Parcel in) {
        kind = in.readString();
        id = in.readString();
        name = in.readString();
        image = in.readString();
    }

    public String getKind() {
        return kind;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getImage() {
        return image;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(kind);
        dest.writeString(id);
        dest.writeString(name);
        dest.writeString(image);
    }

    public static final Creator<DiscoverFacet> CREATOR = new Creator<>() {
        @Override
        public DiscoverFacet createFromParcel(Parcel in) {
            return new DiscoverFacet(in);
        }

        @Override
        public DiscoverFacet[] newArray(int size) {
            return new DiscoverFacet[size];
        }
    };
}
