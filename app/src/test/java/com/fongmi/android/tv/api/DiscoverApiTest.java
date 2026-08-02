package com.fongmi.android.tv.api;

import com.fongmi.android.tv.bean.Vod;
import com.fongmi.android.tv.bean.DiscoverFacet;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class DiscoverApiTest {

    @Test
    public void shouldParseDoubanSubjects() {
        String json = "{\"subjects\":[" +
                "{\"title\":\"沙丘2\",\"rate\":\"8.2\",\"cover\":\"https://img1.doubanio.com/view/photo/s_ratio_poster/public/p1.jpg\",\"id\":\"35575567\"}," +
                "{\"title\":\"无图\",\"rate\":\"7.0\",\"cover\":\"\",\"id\":\"1\"}" +
                "]}";

        List<Vod> items = DiscoverApi.parseDoubanSubjects(json);

        assertEquals(1, items.size());
    }

    @Test
    public void shouldReturnEmptyListForInvalidDoubanBody() {
        assertTrue(DiscoverApi.parseDoubanSubjects("").isEmpty());
        assertTrue(DiscoverApi.parseDoubanSubjects("not json").isEmpty());
        assertTrue(DiscoverApi.parseDoubanSubjects("{\"msg\":\"检测到有异常请求\"}").isEmpty());
    }

    @Test
    public void shouldAppendRefererToDoubanCover() {
        String pic = DiscoverApi.doubanPic("https://img1.doubanio.com/p1.jpg");

        assertEquals("https://img1.doubanio.com/p1.jpg@Referer=https://movie.douban.com/@User-Agent=Mozilla/5.0", pic);
    }

    @Test
    public void shouldFormatDoubanRemarks() {
        assertEquals("8.2分", DiscoverApi.doubanRemarks("8.2"));
        assertEquals("", DiscoverApi.doubanRemarks(""));
        assertEquals("", DiscoverApi.doubanRemarks(null));
    }

    @Test
    public void shouldParseTmdbResultsAndSkipPersonAndMissingPoster() {
        String json = "{\"results\":[" +
                "{\"id\":693134,\"media_type\":\"movie\",\"title\":\"沙丘2\",\"poster_path\":\"/abc.jpg\",\"release_date\":\"2024-02-27\",\"vote_average\":8.16,\"overview\":\"简介\"}," +
                "{\"id\":1,\"media_type\":\"person\",\"name\":\"某演员\",\"poster_path\":\"/p.jpg\"}," +
                "{\"id\":2,\"media_type\":\"tv\",\"name\":\"无海报剧\",\"poster_path\":null}" +
                "]}";

        List<Vod> items = DiscoverApi.parseTmdbResults(json);

        assertEquals(1, items.size());
    }

    @Test
    public void shouldReturnEmptyListForInvalidTmdbBody() {
        assertTrue(DiscoverApi.parseTmdbResults("").isEmpty());
        assertTrue(DiscoverApi.parseTmdbResults("{\"status_code\":7}").isEmpty());
    }

    @Test
    public void shouldPreferTmdbTitleOverName() {
        JsonObject movie = JsonParser.parseString("{\"title\":\"电影名\",\"name\":\"别名\"}").getAsJsonObject();
        JsonObject tv = JsonParser.parseString("{\"name\":\"剧集名\"}").getAsJsonObject();

        assertEquals("电影名", DiscoverApi.tmdbName(movie));
        assertEquals("剧集名", DiscoverApi.tmdbName(tv));
    }

    @Test
    public void shouldBuildTmdbPosterUrl() {
        assertEquals("https://tapi.coolmarket.eu.org/t/p/w342/abc.jpg", DiscoverApi.tmdbPic("/abc.jpg"));
        assertEquals("https://tapi.coolmarket.eu.org/t/p/w342/abc.jpg", DiscoverApi.tmdbPic("abc.jpg"));
        assertEquals("", DiscoverApi.tmdbPic(""));
    }

    @Test
    public void shouldBuildTmdbBackdropAndLogoUrls() {
        assertEquals("https://tapi.coolmarket.eu.org/t/p/w780/hero.jpg", DiscoverApi.tmdbBackdrop("/hero.jpg"));
        assertEquals("https://tapi.coolmarket.eu.org/t/p/w300/logo.png", DiscoverApi.tmdbLogo("logo.png"));
    }

    @Test
    public void shouldParseGenresAndProviders() {
        List<DiscoverFacet> genres = DiscoverApi.parseFacets("{\"genres\":[{\"id\":878,\"name\":\"科幻\"}]}", DiscoverFacet.GENRE);
        List<DiscoverFacet> providers = DiscoverApi.parseFacets("{\"results\":[{\"provider_id\":8,\"provider_name\":\"Netflix\",\"logo_path\":\"/n.png\"}]}", DiscoverFacet.PROVIDER);

        assertEquals("878", genres.get(0).getId());
        assertEquals("Netflix", providers.get(0).getName());
        assertEquals("https://tapi.coolmarket.eu.org/t/p/w300/n.png", providers.get(0).getImage());
    }

    @Test
    public void shouldParseTmdbBackdropAndMediaType() {
        List<Vod> items = DiscoverApi.parseTmdbResults("{\"results\":[{\"id\":1,\"name\":\"剧名\",\"media_type\":\"tv\",\"poster_path\":\"/p.jpg\",\"backdrop_path\":\"/b.jpg\"}]}");

        assertEquals("剧集", items.get(0).getTypeName());
        assertEquals("https://tapi.coolmarket.eu.org/t/p/w780/b.jpg", items.get(0).getBackdrop());
    }

    @Test
    public void shouldExtractTmdbYearFromEitherDateField() {
        JsonObject movie = JsonParser.parseString("{\"release_date\":\"2024-02-27\"}").getAsJsonObject();
        JsonObject tv = JsonParser.parseString("{\"first_air_date\":\"2023-01-14\"}").getAsJsonObject();
        JsonObject none = JsonParser.parseString("{\"release_date\":\"\"}").getAsJsonObject();

        assertEquals("2024", DiscoverApi.tmdbYear(movie));
        assertEquals("2023", DiscoverApi.tmdbYear(tv));
        assertEquals("", DiscoverApi.tmdbYear(none));
    }

    @Test
    public void shouldFormatTmdbRemarks() {
        assertEquals("8.2分", DiscoverApi.tmdbRemarks(8.16));
        assertEquals("", DiscoverApi.tmdbRemarks(0));
    }
}
