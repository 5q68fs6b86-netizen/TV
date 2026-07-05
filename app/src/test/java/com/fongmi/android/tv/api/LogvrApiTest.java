package com.fongmi.android.tv.api;

import com.fongmi.android.tv.bean.Danmaku;

import org.junit.Test;

import java.lang.reflect.Field;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class LogvrApiTest {

    @Test
    public void shouldNormalizeLogvrBaseUrl() {
        assertEquals("http://host:9321/87654321/api/v2", LogvrApi.normalizeBase("http://host:9321/87654321"));
        assertEquals("http://host:9321/87654321/api/v2", LogvrApi.normalizeBase("http://host:9321/87654321/"));
        assertEquals("http://host:9321/87654321/api/v2", LogvrApi.normalizeBase("http://host:9321/87654321/api/v2"));
        assertEquals("", LogvrApi.normalizeBase(" "));
    }

    @Test
    public void shouldBuildCommentByUrlEndpointWithXmlFormat() {
        String url = LogvrApi.buildCommentByUrlUrl("http://host:9321/87654321", "https://v.qq.com/x/cover/abc.html?vid=def");

        assertTrue(url.startsWith("http://host:9321/87654321/api/v2/comment?"));
        assertTrue(url.contains("url=https%3A%2F%2Fv.qq.com%2Fx%2Fcover%2Fabc.html%3Fvid%3Ddef"));
        assertTrue(url.contains("format=xml"));
    }

    @Test
    public void shouldBuildMatchFileNameFromEpisodeTitle() {
        assertEquals("庆余年 S01E01", LogvrApi.buildMatchFileName("庆余年", "第01集"));
        assertEquals("庆余年 S01E12", LogvrApi.buildMatchFileName("庆余年", "EP12"));
        assertEquals("庆余年 第二季 S02E03", LogvrApi.buildMatchFileName("庆余年 第二季", "第03集"));
        assertEquals("庆余年 预告", LogvrApi.buildMatchFileName("庆余年", "预告"));
    }

    @Test
    public void shouldBuildSearchEpisodesEndpointWithNumericEpisode() {
        String url = LogvrApi.buildSearchEpisodesUrl("http://host:9321/87654321", "庆余年", "第01集");

        assertTrue(url.startsWith("http://host:9321/87654321/api/v2/search/episodes?"));
        assertTrue(url.contains("anime=%E5%BA%86%E4%BD%99%E5%B9%B4"));
        assertTrue(url.contains("episode=1"));
    }

    @Test
    public void shouldMapMatchResponseToCommentXmlSource() throws Exception {
        String json = "{\"success\":true,\"isMatched\":true,\"matches\":[{\"episodeId\":10001,\"animeTitle\":\"生万物\",\"episodeTitle\":\"第1集\"}]}";

        List<Danmaku> items = LogvrApi.parseMatch("http://host:9321/87654321", json);

        assertEquals(1, items.size());
        assertEquals("生万物 第1集", field(items.get(0), "name"));
        assertEquals("http://host:9321/87654321/api/v2/comment/10001?format=xml", field(items.get(0), "url"));
    }

    @Test
    public void shouldMapSearchEpisodesResponseToCommentXmlSources() throws Exception {
        String json = "{\"success\":true,\"animes\":[{\"animeTitle\":\"生万物\",\"episodes\":[{\"episodeId\":11,\"episodeTitle\":\"第1集\"},{\"episodeId\":\"12\",\"episodeTitle\":\"第2集\"}]}]}";

        List<Danmaku> items = LogvrApi.parseEpisodes("http://host:9321/87654321/api/v2", json);

        assertEquals(2, items.size());
        assertEquals("生万物 第1集", field(items.get(0), "name"));
        assertEquals("http://host:9321/87654321/api/v2/comment/11?format=xml", field(items.get(0), "url"));
        assertEquals("生万物 第2集", field(items.get(1), "name"));
        assertEquals("http://host:9321/87654321/api/v2/comment/12?format=xml", field(items.get(1), "url"));
    }

    @Test
    public void shouldTreatXmlWithoutItemsAsEmpty() {
        assertTrue(LogvrApi.hasDanmakuXml("<i><d p=\"1,1,25,16777215,0,0,0,1\">test</d></i>"));
        assertEquals(0, LogvrApi.parseMatch("http://host", "{\"success\":true,\"matches\":[]}").size());
        assertEquals(0, LogvrApi.parseEpisodes("http://host", "{\"success\":true,\"animes\":[]}").size());
    }

    private String field(Danmaku danmaku, String name) throws Exception {
        Field field = Danmaku.class.getDeclaredField(name);
        field.setAccessible(true);
        return (String) field.get(danmaku);
    }
}
