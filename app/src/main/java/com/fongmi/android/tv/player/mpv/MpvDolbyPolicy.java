package com.fongmi.android.tv.player.mpv;

final class MpvDolbyPolicy {

    private MpvDolbyPolicy() {
    }

    static boolean shouldUsePlatformDecoder(
            boolean hardDecode, boolean dolbyHwdecEnabled, int profile) {
        return profile >= 0 && hardDecode && dolbyHwdecEnabled;
    }

    static boolean shouldUseSoftwareDecoder(
            boolean hardDecode, boolean dolbyHwdecEnabled, int profile) {
        return profile >= 0 && !shouldUsePlatformDecoder(hardDecode, dolbyHwdecEnabled, profile);
    }
}
