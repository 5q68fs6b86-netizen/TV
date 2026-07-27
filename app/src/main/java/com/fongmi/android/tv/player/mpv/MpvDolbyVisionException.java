package com.fongmi.android.tv.player.mpv;

final class MpvDolbyVisionException extends Exception {

    MpvDolbyVisionException(int profile) {
        super("Dolby Vision profile " + profile + " requires the platform decoder");
    }
}
