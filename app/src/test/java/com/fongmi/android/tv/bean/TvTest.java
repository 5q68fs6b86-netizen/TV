package com.fongmi.android.tv.bean;

import org.junit.Test;
import org.simpleframework.xml.core.Persister;

import static org.junit.Assert.assertEquals;

public class TvTest {

    @Test
    public void shouldUseFirstNonEmptyProgrammeTitle() throws Exception {
        String xml = "<tv><programme start=\"20260810000000 +0800\" stop=\"20260810010000 +0800\" channel=\"cctv\"><title></title><title lang=\"zh\">新闻</title><title lang=\"en\">News</title></programme></tv>";

        Tv tv = new Persister().read(Tv.class, xml, false);

        assertEquals("新闻", tv.getProgramme().get(0).getTitle());
    }
}
