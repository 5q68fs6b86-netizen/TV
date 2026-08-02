package com.fongmi.android.tv.bean;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class DiscoverMediaKeyTest {

    @Test
    public void shouldRoundTripMovieAndTvKeys() {
        DiscoverMediaKey movie = DiscoverMediaKey.of("movie", 12);
        DiscoverMediaKey tv = DiscoverMediaKey.of("tv", 12);

        assertEquals("tmdb:movie:12", movie.toString());
        assertEquals("tmdb:tv:12", tv.toString());
        assertEquals(movie, DiscoverMediaKey.parse(movie.toString()));
        assertFalse(movie.equals(tv));
    }

    @Test
    public void shouldRejectMalformedKeys() {
        assertNull(DiscoverMediaKey.parse("tmdb:12"));
        assertNull(DiscoverMediaKey.parse("tmdb:person:12"));
        assertNull(DiscoverMediaKey.parse("tmdb:movie:0"));
        assertTrue(DiscoverMediaKey.isValid("tmdb:tv:9"));
        assertFalse(DiscoverMediaKey.isValid("tmdb:person:9"));
    }
}
