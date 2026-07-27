package com.fongmi.android.tv.player.exo;

import androidx.media3.common.PlaybackException;

final class ExoTunnelFallback {

    private ExoTunnelFallback() {
    }

    static boolean shouldFallback(boolean tunnelingEnabled, int errorCode) {
        if (!tunnelingEnabled) return false;
        return switch (errorCode) {
            case PlaybackException.ERROR_CODE_FAILED_RUNTIME_CHECK,
                    PlaybackException.ERROR_CODE_DECODER_INIT_FAILED,
                    PlaybackException.ERROR_CODE_DECODER_QUERY_FAILED,
                    PlaybackException.ERROR_CODE_DECODING_FAILED,
                    PlaybackException.ERROR_CODE_AUDIO_TRACK_INIT_FAILED,
                    PlaybackException.ERROR_CODE_AUDIO_TRACK_WRITE_FAILED -> true;
            default -> false;
        };
    }
}
