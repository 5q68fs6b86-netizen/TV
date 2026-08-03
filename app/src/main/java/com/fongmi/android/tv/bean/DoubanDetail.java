package com.fongmi.android.tv.bean;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public final class DoubanDetail implements Parcelable {

    private final String subjectId;
    private final String mediaType;
    private final String title;
    private final String poster;
    private final String rating;
    private final String year;
    private final String genres;
    private final String region;
    private final String duration;
    private final String directors;
    private final String actors;
    private final String comment;

    public DoubanDetail(String subjectId, String mediaType, String title, String poster, String rating,
                        String year, String genres, String region, String duration, String directors,
                        String actors, String comment) {
        this.subjectId = value(subjectId);
        this.mediaType = value(mediaType);
        this.title = value(title);
        this.poster = value(poster);
        this.rating = value(rating);
        this.year = value(year);
        this.genres = value(genres);
        this.region = value(region);
        this.duration = value(duration);
        this.directors = value(directors);
        this.actors = value(actors);
        this.comment = value(comment);
    }

    private DoubanDetail(Parcel in) {
        subjectId = value(in.readString());
        mediaType = value(in.readString());
        title = value(in.readString());
        poster = value(in.readString());
        rating = value(in.readString());
        year = value(in.readString());
        genres = value(in.readString());
        region = value(in.readString());
        duration = value(in.readString());
        directors = value(in.readString());
        actors = value(in.readString());
        comment = value(in.readString());
    }

    private static String value(String value) {
        return value == null ? "" : value.trim();
    }

    public String getSubjectId() { return subjectId; }
    public String getMediaType() { return mediaType; }
    public String getTitle() { return title; }
    public String getPoster() { return poster; }
    public String getRating() { return rating; }
    public String getYear() { return year; }
    public String getGenres() { return genres; }
    public String getRegion() { return region; }
    public String getDuration() { return duration; }
    public String getDirectors() { return directors; }
    public String getActors() { return actors; }
    public String getComment() { return comment; }

    public boolean hasReliableMatchFields() {
        return !title.isEmpty() && !year.isEmpty() && (DiscoverMediaKey.MOVIE.equals(mediaType) || DiscoverMediaKey.TV.equals(mediaType));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(subjectId);
        dest.writeString(mediaType);
        dest.writeString(title);
        dest.writeString(poster);
        dest.writeString(rating);
        dest.writeString(year);
        dest.writeString(genres);
        dest.writeString(region);
        dest.writeString(duration);
        dest.writeString(directors);
        dest.writeString(actors);
        dest.writeString(comment);
    }

    public static final Creator<DoubanDetail> CREATOR = new Creator<>() {
        @Override
        public DoubanDetail createFromParcel(Parcel source) {
            return new DoubanDetail(source);
        }

        @Override
        public DoubanDetail[] newArray(int size) {
            return new DoubanDetail[size];
        }
    };
}
