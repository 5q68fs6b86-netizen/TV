package com.fongmi.android.tv.bean;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class DiscoverRequestStateTest {

    @Test
    public void shouldIgnoreStaleResponsesAndDeduplicatePages() {
        DiscoverRequestState state = new DiscoverRequestState();
        int first = state.reset();
        int current = state.reset();

        state.addAll(first, List.of(vod("tmdb:movie:1")));
        assertTrue(state.isEmpty());

        state.addAll(current, List.of(vod("tmdb:movie:1"), vod("tmdb:movie:2")));
        state.addAll(current, List.of(vod("tmdb:movie:2"), vod("tmdb:movie:3")));

        assertFalse(state.isEmpty());
        assertEquals(3, state.size());
        assertEquals("tmdb:movie:1", state.getItems().get(0).getId());
        assertEquals("tmdb:movie:3", state.getItems().get(2).getId());
    }

    private static Vod vod(String id) {
        Vod vod = new Vod();
        vod.setId(id);
        return vod;
    }
}
