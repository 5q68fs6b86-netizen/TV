package com.fongmi.android.tv.bean;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class DiscoverDetail {

    private final DiscoverMediaKey key;
    private final String title;
    private final String originalTitle;
    private final String backdrop;
    private final String poster;
    private final String overview;
    private final String rating;
    private final String year;
    private final String genres;
    private final String countries;
    private final int runtimeMinutes;
    private final int seasons;
    private final int episodes;
    private final String status;
    private final String creators;
    private final List<DiscoverCredit> cast;

    public DiscoverDetail(DiscoverMediaKey key, String title, String originalTitle, String backdrop, String poster,
                          String overview, String rating, String year, String genres, String countries,
                          int runtimeMinutes, int seasons, int episodes, String status,
                          String creators, List<DiscoverCredit> cast) {
        this.key = key;
        this.title = value(title);
        this.originalTitle = value(originalTitle);
        this.backdrop = value(backdrop);
        this.poster = value(poster);
        this.overview = value(overview);
        this.rating = value(rating);
        this.year = value(year);
        this.genres = value(genres);
        this.countries = value(countries);
        this.runtimeMinutes = runtimeMinutes;
        this.seasons = seasons;
        this.episodes = episodes;
        this.status = value(status);
        this.creators = value(creators);
        this.cast = cast == null ? Collections.emptyList() : Collections.unmodifiableList(new ArrayList<>(cast));
    }

    private static String value(String value) {
        return value == null ? "" : value;
    }

    public DiscoverMediaKey getKey() { return key; }
    public String getTitle() { return title; }
    public String getOriginalTitle() { return originalTitle; }
    public String getBackdrop() { return backdrop; }
    public String getPoster() { return poster; }
    public String getOverview() { return overview; }
    public String getRating() { return rating; }
    public String getYear() { return year; }
    public String getGenres() { return genres; }
    public String getCountries() { return countries; }
    public int getRuntimeMinutes() { return runtimeMinutes; }
    public int getSeasons() { return seasons; }
    public int getEpisodes() { return episodes; }
    public String getStatus() { return status; }
    public String getCreators() { return creators; }
    public List<DiscoverCredit> getCast() { return cast; }
}
