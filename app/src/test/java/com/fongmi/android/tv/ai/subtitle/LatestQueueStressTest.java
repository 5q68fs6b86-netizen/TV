package com.fongmi.android.tv.ai.subtitle;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class LatestQueueStressTest {
    @Test
    public void millionOffersStayBoundedAndNonBlocking() {
        LatestQueue<Integer> queue = new LatestQueue<>(8);
        long started = System.nanoTime();
        for (int i = 0; i < 1_000_000; i++) queue.offerLatest(i);
        long elapsedMs = (System.nanoTime() - started) / 1_000_000L;
        assertEquals(8, queue.size());
        assertTrue(queue.dropped() >= 999_992L);
        assertTrue("latest queue took " + elapsedMs + "ms", elapsedMs < 3000L);
    }
}
