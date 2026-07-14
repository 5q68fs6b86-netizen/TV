package com.fongmi.android.tv.ai.subtitle;

import com.github.catvod.utils.Prefers;

public final class AiSubtitleSettings {
    private static final String PREFIX = "ai_subtitle_";

    public enum TranslationProvider {
        OFF("关闭（仅显示识别原文）"),
        DEFAULT("默认翻译"),
        MTRAN("MTranServer"),
        OPENAI("大模型（OpenAI 兼容）");

        private final String label;

        TranslationProvider(String label) {
            this.label = label;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    public enum SubtitleMode {
        BILINGUAL("双语"),
        TRANSLATED_ONLY("仅译文");

        private final String label;

        SubtitleMode(String label) {
            this.label = label;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    private AiSubtitleSettings() {
    }

    public static boolean isEnabled() {
        return Prefers.getBoolean(PREFIX + "enabled", false);
    }

    public static void setEnabled(boolean value) {
        Prefers.put(PREFIX + "enabled", value);
    }

    public static AiLanguage getLanguage() {
        return AiLanguage.fromCode(Prefers.getString(PREFIX + "language", "zh"));
    }

    public static void setLanguage(AiLanguage value) {
        Prefers.put(PREFIX + "language", value.code());
    }

    public static TranslationProvider getTranslationProvider() {
        try {
            return TranslationProvider.valueOf(Prefers.getString(PREFIX + "translation_provider", TranslationProvider.OFF.name()));
        } catch (Exception ignored) {
            return TranslationProvider.OFF;
        }
    }

    public static void setTranslationProvider(TranslationProvider value) {
        Prefers.put(PREFIX + "translation_provider", value.name());
    }

    public static SubtitleMode getSubtitleMode() {
        try {
            return SubtitleMode.valueOf(Prefers.getString(PREFIX + "subtitle_mode", SubtitleMode.BILINGUAL.name()));
        } catch (Exception ignored) {
            return SubtitleMode.BILINGUAL;
        }
    }

    public static void setSubtitleMode(SubtitleMode value) {
        Prefers.put(PREFIX + "subtitle_mode", value.name());
    }

    public static String getMTranUrl() {
        return Prefers.getString(PREFIX + "mtran_url", "http://localhost:8989").trim();
    }

    public static void setMTranUrl(String value) {
        Prefers.put(PREFIX + "mtran_url", value == null ? "" : value.trim());
    }

    public static String getBaseUrl() {
        return Prefers.getString(PREFIX + "base_url", "https://api.deepseek.com").trim();
    }

    public static void setBaseUrl(String value) {
        Prefers.put(PREFIX + "base_url", value == null ? "" : value.trim());
    }

    public static String getTranslationModel() {
        return Prefers.getString(PREFIX + "translation_model", "deepseek-v4-flash").trim();
    }

    public static void setTranslationModel(String value) {
        Prefers.put(PREFIX + "translation_model", value == null ? "" : value.trim());
    }

    public static boolean isThinkingEnabled() {
        return Prefers.getBoolean(PREFIX + "thinking_enabled", false);
    }

    public static void setThinkingEnabled(boolean value) {
        Prefers.put(PREFIX + "thinking_enabled", value);
    }

    public static boolean isContextEnabled() {
        return Prefers.getBoolean(PREFIX + "context_enabled", false);
    }

    public static void setContextEnabled(boolean value) {
        Prefers.put(PREFIX + "context_enabled", value);
    }

    public static int getAsrThreads() {
        int cores = Runtime.getRuntime().availableProcessors();
        return Math.max(1, Math.min(cores - 1, 3));
    }
}
