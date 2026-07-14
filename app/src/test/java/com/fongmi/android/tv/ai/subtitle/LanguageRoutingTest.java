package com.fongmi.android.tv.ai.subtitle;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class LanguageRoutingTest {
    @Test
    public void uiLanguagesRouteToDeterministicEfficientModels() {
        assertEquals(AsrModel.MANDARIN_ZIPFORMER_CTC, AiLanguage.MANDARIN.model());
        assertEquals(AsrModel.CANTONESE_WENET_CTC, AiLanguage.CANTONESE.model());
        assertEquals(AsrModel.GERMAN_KROKO, AiLanguage.GERMAN.model());
        assertEquals(AsrModel.ENGLISH_KROKO, AiLanguage.ENGLISH.model());
        assertEquals(AsrModel.FRENCH_KROKO, AiLanguage.FRENCH.model());
        assertEquals(AsrModel.SPANISH_KROKO, AiLanguage.SPANISH.model());
        assertEquals(AsrModel.JAPANESE_MOONSHINE, AiLanguage.JAPANESE.model());
        java.util.Set<AsrModel> packages = new java.util.HashSet<>();
        for (AiLanguage language : AiLanguage.values()) packages.add(language.model());
        assertEquals(AiLanguage.values().length, packages.size());
    }

    @Test
    public void resamplerConvertsFortyEightKhzToSixteenKhz() {
        float[] input = new float[48000];
        for (int i = 0; i < input.length; i++) input[i] = (float) Math.sin(i * 0.01);
        float[] output = SherpaSubtitleController.resample(input, 48000, 16000);
        assertEquals(16000, output.length);
    }

    @Test
    public void routedPackagesArePinnedAndIndependentlyVerifiable() {
        for (AiLanguage language : AiLanguage.values()) {
            AsrModel model = language.model();
            assertFalse(model.folder.isBlank());
            assertTrue(model.files.length >= 2);
            assertTrue(model.downloadBytes() > 0L);
            for (ModelFile file : model.files) {
                assertTrue(file.url.startsWith("https://"));
                assertFalse("floating model revision: " + file.url, file.url.contains("/resolve/main/"));
                assertEquals(64, file.sha256.length());
                assertTrue(file.size > 0L);
            }
        }
    }

    @Test
    public void streamingPackagesDoNotDownloadOfflineVad() {
        assertTrue(AiLanguage.MANDARIN.model().isStreaming());
        assertTrue(AiLanguage.GERMAN.model().isStreaming());
        assertTrue(AiLanguage.ENGLISH.model().isStreaming());
        assertTrue(AiLanguage.FRENCH.model().isStreaming());
        assertTrue(AiLanguage.SPANISH.model().isStreaming());
        assertFalse(AiLanguage.MANDARIN.model().needsVad);
        assertFalse(AiLanguage.GERMAN.model().needsVad);
        assertTrue(AiLanguage.CANTONESE.model().needsVad);
        assertTrue(AiLanguage.JAPANESE.model().needsVad);
    }
}
