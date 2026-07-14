package com.fongmi.android.tv.ai.subtitle;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class AiSubtitleCueTest {
    @Test
    public void bilingualShowsChineseTranslationAboveOriginal() {
        assertEquals("你好\nHello", AiSubtitleRuntime.composeCue(
                AiLanguage.ENGLISH,
                AiSubtitleSettings.SubtitleMode.BILINGUAL,
                "Hello",
                "你好"));
    }

    @Test
    public void mandarinNeverDuplicatesTwoLines() {
        assertEquals("这是中文", AiSubtitleRuntime.composeCue(
                AiLanguage.MANDARIN,
                AiSubtitleSettings.SubtitleMode.BILINGUAL,
                "這是中文",
                "这是中文"));
    }

    @Test
    public void diagnosticBackendTextIsNotAPlaybackCue() {
        assertTrue(AiSubtitleRuntime.isDiagnosticResult("MTranServer 测试通过"));
        assertFalse(AiSubtitleRuntime.isDiagnosticResult("MTranServer 可以正常翻译字幕"));
    }

    @Test
    public void futureCueWaitsForItsAudioWindow() {
        AiSubtitleRuntime.CueWindow window = AiSubtitleRuntime.planCueWindow(
                10_000, 12_000, 13_000, 100_000, 7_000);
        assertEquals(104_000, window.startElapsedMs());
        assertEquals(106_500, window.endElapsedMs());
    }

    @Test
    public void lateTranslatedCueIsDroppedInsteadOfFollowingAudio() {
        assertNull(AiSubtitleRuntime.planCueWindow(10_000, 12_000,
                20_000, 100_000, 7_000));
        assertNull(AiSubtitleRuntime.planCueWindow(10_000, 12_000,
                30_000, 100_000, 7_000));
    }

    @Test
    public void shortTailOfLongSpeechIsDroppedInsteadOfFlashing() {
        assertNull(AiSubtitleRuntime.planCueWindow(10_000, 14_000,
                18_250, 100_000, 4_750));
    }

    @Test
    public void slightlyLateCueStillKeepsMostOfSpeechDuration() {
        AiSubtitleRuntime.CueWindow window = AiSubtitleRuntime.planCueWindow(
                10_000, 12_000, 14_650, 100_000, 4_750);
        assertEquals(2_500, window.endElapsedMs() - window.startElapsedMs());
        assertEquals(2_500, AiSubtitleRuntime.desiredCueDurationMs(10_000, 12_000));
    }

    @Test
    public void exactPresentationClockDoesNotIntentionallyLeadSpeech() {
        AiSubtitleRuntime.CueWindow window = AiSubtitleRuntime.planCueWindow(
                10_000, 12_000, 13_000, 100_000, 7_000);
        assertEquals(4_000, window.startElapsedMs() - 100_000);
    }

    @Test
    public void translationPastSyncToleranceIsDroppedRatherThanShownLate() {
        assertNull(AiSubtitleRuntime.planCueWindow(
                10_000, 12_000, 17_200, 100_000, 7_000));
    }

    @Test
    public void streamingBudgetKeepsLeanbackFiveSecondSegmentation() {
        assertEquals(4_800L,
                SherpaSubtitleController.streamingUtteranceBudgetMs(10_070L, 910L));
    }

    @Test
    public void streamingBudgetReservesTranslationTimeOnShortMobileAudioTrack() {
        assertEquals(2_580L,
                SherpaSubtitleController.streamingUtteranceBudgetMs(5_180L, 857L));
        assertEquals(2_530L,
                SherpaSubtitleController.streamingUtteranceBudgetMs(5_180L, 1_450L));
    }

    @Test
    public void streamingBudgetHasSafeBounds() {
        assertEquals(1_200L,
                SherpaSubtitleController.streamingUtteranceBudgetMs(3_500L, 2_000L));
        assertEquals(4_800L,
                SherpaSubtitleController.streamingUtteranceBudgetMs(Long.MAX_VALUE, 0L));
    }
}
