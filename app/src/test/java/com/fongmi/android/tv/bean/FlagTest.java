package com.fongmi.android.tv.bean;

import org.junit.Test;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

public class FlagTest {

    @Test
    public void shouldIgnoreOutOfBoundsSavedPosition() {
        Flag flag = new Flag("main");
        Episode first = Episode.create("01", "url1");
        flag.getEpisodes().add(first);
        flag.getEpisodes().add(Episode.create("02", "url2"));
        flag.setPosition(5);

        assertNull(flag.find("unknown", true));
        assertSame(first, flag.find("unknown", false));
    }
}
