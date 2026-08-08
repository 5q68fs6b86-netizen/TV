package com.fongmi.android.tv.utils;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TmdbEndpointTest {

    @Test
    public void shouldKeepDefaultEndpoint() {
        assertEquals("https://tapi.coolmarket.eu.org/", TmdbEndpoint.DEFAULT_ROOT);
        assertEquals("https://tapi.coolmarket.eu.org/3/", TmdbEndpoint.DEFAULT_API_BASE);
        assertEquals("https://tapi.coolmarket.eu.org/t/p/", TmdbEndpoint.DEFAULT_IMAGE_BASE);
    }

    @Test
    public void shouldNormalizeProxyRoot() {
        assertEquals("https://example.com/", TmdbEndpoint.normalizeRoot("example.com"));
        assertEquals("http://example.com/base/", TmdbEndpoint.normalizeRoot("http://example.com/base"));
        assertEquals("https://example.com/", TmdbEndpoint.normalizeRoot("https://example.com/3/"));
        assertEquals("https://example.com/base/", TmdbEndpoint.normalizeRoot("https://example.com/base/t/p/"));
    }

    @Test
    public void shouldIgnoreEmptyOrInvalidProxyRoot() {
        assertEquals("", TmdbEndpoint.normalizeRoot(""));
        assertEquals("", TmdbEndpoint.normalizeRoot("ftp://example.com/"));
        assertEquals("", TmdbEndpoint.normalizeRoot("https:///missing-host"));
    }
}
