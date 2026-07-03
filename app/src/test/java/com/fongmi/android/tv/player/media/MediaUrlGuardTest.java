package com.fongmi.android.tv.player.media;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class MediaUrlGuardTest {

    @Test
    public void shouldRejectTplvImageResource() {
        String url = "https://p3-dcd-sign.byteimg.com/tos-cn-i-f042mdwyw7/44de9796e7ae4a38bb374d4573fd50fe~tplv-f042mdwyw7-image.image?lk3s=13ddc783";

        assertTrue(MediaUrlGuard.shouldReject(url, null));
    }

    @Test
    public void shouldRejectImageMimeFormat() {
        assertTrue(MediaUrlGuard.shouldReject("https://example.com/resource", "image/webp"));
    }

    @Test
    public void shouldRejectCommonImageExtensions() {
        assertTrue(MediaUrlGuard.shouldReject("https://example.com/poster.webp?token=abc", null));
        assertTrue(MediaUrlGuard.shouldReject("https://example.com/poster.JPG#preview", null));
    }

    @Test
    public void shouldAcceptBytetosVideoWithoutExtension() {
        String url = "https://lf26-imcloud-file-sign.bytetos.com/tos-cn-v-0000c2428/ocohCGPMJEGRYfAv7BCIuxUHhIojA9LbUfCL4e?x-expires=1783078397";

        assertFalse(MediaUrlGuard.shouldReject(url, null));
    }

    @Test
    public void shouldAcceptCommonVideoUrls() {
        assertFalse(MediaUrlGuard.shouldReject("https://example.com/video.mp4?token=abc", null));
        assertFalse(MediaUrlGuard.shouldReject("https://example.com/live/index.m3u8", null));
        assertFalse(MediaUrlGuard.shouldReject("https://example.com/dash/manifest.mpd", "video/mp4"));
    }
}
