package com.fongmi.android.tv.ai.subtitle;

import java.util.Locale;

public enum AiLanguage {
    MANDARIN("zh", "普通话"),
    CANTONESE("yue", "粤语"),
    GERMAN("de", "德语"),
    ENGLISH("en", "英语"),
    FRENCH("fr", "法语"),
    SPANISH("es", "西班牙语"),
    JAPANESE("ja", "日语");

    private final String code;
    private final String label;

    AiLanguage(String code, String label) {
        this.code = code;
        this.label = label;
    }

    public String code() {
        return code;
    }

    public String label() {
        return label;
    }

    public AsrModel model() {
        return switch (this) {
            case MANDARIN -> AsrModel.MANDARIN_ZIPFORMER_CTC;
            case CANTONESE -> AsrModel.CANTONESE_WENET_CTC;
            case GERMAN -> AsrModel.GERMAN_KROKO;
            case ENGLISH -> AsrModel.ENGLISH_KROKO;
            case FRENCH -> AsrModel.FRENCH_KROKO;
            case SPANISH -> AsrModel.SPANISH_KROKO;
            case JAPANESE -> AsrModel.JAPANESE_MOONSHINE;
        };
    }

    public String mTranCode() {
        return switch (this) {
            case MANDARIN -> "zh-Hans";
            case CANTONESE -> "yue";
            default -> code;
        };
    }

    public static AiLanguage fromCode(String value) {
        if (value != null) {
            String code = value.trim().toLowerCase(Locale.ROOT);
            for (AiLanguage language : values()) if (language.code.equals(code)) return language;
        }
        return MANDARIN;
    }

    @Override
    public String toString() {
        return label;
    }
}
