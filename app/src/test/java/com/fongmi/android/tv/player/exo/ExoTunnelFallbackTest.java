package com.fongmi.android.tv.player.exo;

import androidx.media3.common.PlaybackException;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ExoTunnelFallbackTest {

    @Test
    public void shouldFallbackForTunnelRendererFailures() {
        assertTrue(ExoTunnelFallback.shouldFallback(true, PlaybackException.ERROR_CODE_FAILED_RUNTIME_CHECK));
        assertTrue(ExoTunnelFallback.shouldFallback(true, PlaybackException.ERROR_CODE_DECODER_INIT_FAILED));
        assertTrue(ExoTunnelFallback.shouldFallback(true, PlaybackException.ERROR_CODE_DECODER_QUERY_FAILED));
        assertTrue(ExoTunnelFallback.shouldFallback(true, PlaybackException.ERROR_CODE_DECODING_FAILED));
        assertTrue(ExoTunnelFallback.shouldFallback(true, PlaybackException.ERROR_CODE_AUDIO_TRACK_INIT_FAILED));
        assertTrue(ExoTunnelFallback.shouldFallback(true, PlaybackException.ERROR_CODE_AUDIO_TRACK_WRITE_FAILED));
    }

    @Test
    public void shouldNotFallbackForUnrelatedFailures() {
        assertFalse(ExoTunnelFallback.shouldFallback(true, PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_FAILED));
        assertFalse(ExoTunnelFallback.shouldFallback(true, PlaybackException.ERROR_CODE_PARSING_CONTAINER_MALFORMED));
        assertFalse(ExoTunnelFallback.shouldFallback(true, PlaybackException.ERROR_CODE_DRM_SYSTEM_ERROR));
        assertFalse(ExoTunnelFallback.shouldFallback(true, PlaybackException.ERROR_CODE_AUDIO_TRACK_OFFLOAD_INIT_FAILED));
    }

    @Test
    public void shouldOnlyFallbackWhileTunnelingIsEnabled() {
        assertFalse(ExoTunnelFallback.shouldFallback(false, PlaybackException.ERROR_CODE_DECODER_INIT_FAILED));
        assertFalse(ExoTunnelFallback.shouldFallback(false, PlaybackException.ERROR_CODE_AUDIO_TRACK_INIT_FAILED));
    }
}
