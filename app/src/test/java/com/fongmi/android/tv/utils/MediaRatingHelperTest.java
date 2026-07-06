package com.fongmi.android.tv.utils;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class MediaRatingHelperTest {

    @Test
    public void shouldParseTmdbRatingFromSearchResult() throws Exception {
        String json = "{\"results\":[{\"id\":19995,\"title\":\"阿凡达\",\"vote_average\":7.608,\"vote_count\":34153}]}";

        MediaRatingHelper.Rating rating = MediaRatingHelper.parseTmdbRating(json);

        assertEquals(7.608, rating.getValue(), 0.0001);
        assertEquals("7.6", rating.getText());
    }

    @Test
    public void shouldIgnoreMissingTmdbRating() throws Exception {
        String json = "{\"results\":[{\"id\":1,\"title\":\"新片\",\"vote_average\":0,\"vote_count\":0}]}";

        assertNull(MediaRatingHelper.parseTmdbRating(json));
    }

    @Test
    public void shouldSelectExactDoubanCandidateBeforeSequels() throws Exception {
        String json = "[" +
                "{\"title\":\"阿凡达：火与烬\",\"year\":\"2025\",\"id\":\"5348089\"}," +
                "{\"title\":\"阿凡达\",\"year\":\"2009\",\"sub_title\":\"Avatar\",\"id\":\"1652587\"}," +
                "{\"title\":\"阿凡达：水之道\",\"year\":\"2022\",\"id\":\"4811774\"}" +
                "]";

        String id = MediaRatingHelper.selectDoubanSubjectId(json, "阿凡达", "2009", "电影");

        assertEquals("1652587", id);
    }

    @Test
    public void shouldSelectDoubanTvCandidateByYearAndEpisode() throws Exception {
        String json = "[{\"title\":\"狂飙\",\"year\":\"2023\",\"type\":\"movie\",\"id\":\"35465232\",\"episode\":\"39\"}]";

        String id = MediaRatingHelper.selectDoubanSubjectId(json, "狂飙", "2023", "国产剧");

        assertEquals("35465232", id);
    }

    @Test
    public void shouldRejectDoubanCandidateWhenYearAndTitleDoNotMatch() throws Exception {
        String json = "[{\"title\":\"阿凡达：火与烬\",\"year\":\"2025\",\"id\":\"5348089\"}]";

        assertNull(MediaRatingHelper.selectDoubanSubjectId(json, "阿凡达", "2009", "电影"));
    }

    @Test
    public void shouldParseDoubanRatingFromAbstract() throws Exception {
        String json = "{\"r\":0,\"subject\":{\"title\":\"阿凡达 Avatar‎ (2009)\",\"rate\":\"8.8\",\"release_year\":\"2009\"}}";

        MediaRatingHelper.Rating rating = MediaRatingHelper.parseDoubanRating(json);

        assertEquals(8.8, rating.getValue(), 0.0001);
        assertEquals("8.8", rating.getText());
    }

    @Test
    public void shouldIgnoreMissingDoubanRating() throws Exception {
        String json = "{\"r\":0,\"subject\":{\"title\":\"未评分\",\"rate\":\"\"}}";

        assertNull(MediaRatingHelper.parseDoubanRating(json));
    }
}
