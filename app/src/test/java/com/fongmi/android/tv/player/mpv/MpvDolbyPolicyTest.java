package com.fongmi.android.tv.player.mpv;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class MpvDolbyPolicyTest {

    @Test
    public void shouldUsePlatformDecoderForDolbyHardwareDecode() {
        assertTrue(MpvDolbyPolicy.shouldUsePlatformDecoder(true, true, 5));
        assertTrue(MpvDolbyPolicy.shouldUsePlatformDecoder(true, true, 7));
    }

    @Test
    public void shouldUseSoftwareDecodeWhenDolbyHardwareDecodeIsOff() {
        assertTrue(MpvDolbyPolicy.shouldUseSoftwareDecoder(true, false, 5));
        assertTrue(MpvDolbyPolicy.shouldUseSoftwareDecoder(false, true, 8));
    }

    @Test
    public void shouldLeaveNonDolbyVideoUnchanged() {
        assertFalse(MpvDolbyPolicy.shouldUsePlatformDecoder(true, true, -1));
        assertFalse(MpvDolbyPolicy.shouldUseSoftwareDecoder(true, false, -1));
    }
}
