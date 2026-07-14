package com.fongmi.android.tv.ai.subtitle;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class AiAudioTrackBufferSizeProviderTest {
    @Test
    public void tenSecondStereoPcmLookaheadIsFrameAligned() {
        assertEquals(1_920_000,
                AiAudioTrackBufferSizeProvider.lookaheadBytes(48_000, 4, 10_000));
    }

    @Test
    public void tenSecondTenChannelPcmLookaheadSupportsAv3aOutput() {
        assertEquals(9_600_000,
                AiAudioTrackBufferSizeProvider.lookaheadBytes(48_000, 20, 10_000));
    }
}
