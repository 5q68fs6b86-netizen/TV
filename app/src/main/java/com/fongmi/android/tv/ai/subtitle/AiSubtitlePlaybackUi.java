package com.fongmi.android.tv.ai.subtitle;

import android.widget.TextView;

import androidx.fragment.app.FragmentActivity;

import com.fongmi.android.tv.R;
import com.fongmi.android.tv.ui.activity.PlaybackActivity;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public final class AiSubtitlePlaybackUi {
    private AiSubtitlePlaybackUi() {
    }

    public static void refresh(TextView toggle, TextView language) {
        boolean enabled = AiSubtitleSettings.isEnabled();
        toggle.setText(toggle.getContext().getString(enabled ? R.string.ai_subtitle_on : R.string.ai_subtitle_off));
        toggle.setSelected(enabled);
        language.setText(language.getContext().getString(R.string.ai_subtitle_language, AiSubtitleSettings.getLanguage().label()));
    }

    public static void toggle(FragmentActivity activity, TextView toggle, TextView language) {
        AiLanguage selected = AiSubtitleSettings.getLanguage();
        if (AiSubtitleSettings.isEnabled()) {
            AiSubtitleSettings.setEnabled(false);
            AiSubtitleRuntime.get().prepareForAudioPipelineRebuild();
            reloadAudioPipeline(activity);
            refresh(toggle, language);
            return;
        }
        if (!isReady(selected)) {
            AiSubtitleSettings.setEnabled(false);
            AiSubtitleRuntime.get().onSettingsChanged();
            refresh(toggle, language);
            showMissing(activity, selected);
            return;
        }
        AiSubtitleSettings.setEnabled(true);
        AiSubtitleRuntime.get().prepareForAudioPipelineRebuild();
        reloadAudioPipeline(activity);
        refresh(toggle, language);
    }

    public static void chooseLanguage(FragmentActivity activity, TextView toggle, TextView language) {
        AiLanguage[] values = AiLanguage.values();
        String[] labels = new String[values.length];
        AsrModelManager models = AiSubtitleRuntime.get().models();
        for (int i = 0; i < values.length; i++) {
            labels[i] = values[i].label() + (models.isInstalled(values[i]) ? "" : "（未下载）");
        }
        new MaterialAlertDialogBuilder(activity)
                .setTitle(R.string.ai_subtitle_choose_language)
                .setNegativeButton(R.string.dialog_negative, null)
                .setSingleChoiceItems(labels, AiSubtitleSettings.getLanguage().ordinal(), (dialog, which) -> {
                    AiLanguage selected = values[which];
                    boolean wasEnabled = AiSubtitleSettings.isEnabled();
                    AiSubtitleSettings.setLanguage(selected);
                    if (!isReady(selected)) AiSubtitleSettings.setEnabled(false);
                    if (wasEnabled) {
                        AiSubtitleRuntime.get().prepareForAudioPipelineRebuild();
                        reloadAudioPipeline(activity);
                    } else {
                        AiSubtitleRuntime.get().onSettingsChanged();
                    }
                    refresh(toggle, language);
                    dialog.dismiss();
                    if (!isReady(selected)) showMissing(activity, selected);
                })
                .show();
    }

    private static boolean isReady(AiLanguage language) {
        AsrModelManager models = AiSubtitleRuntime.get().models();
        return models.isInstalled(language) && models.canRun(language);
    }

    private static void reloadAudioPipeline(FragmentActivity activity) {
        if (activity instanceof PlaybackActivity playback) playback.reloadAiSubtitleAudioPipeline();
    }

    private static void showMissing(FragmentActivity activity, AiLanguage language) {
        AsrModelManager models = AiSubtitleRuntime.get().models();
        String message = !models.isInstalled(language)
                ? activity.getString(R.string.ai_subtitle_model_missing, language.label())
                : activity.getString(R.string.ai_subtitle_memory_blocked, language.label());
        new MaterialAlertDialogBuilder(activity)
                .setTitle(R.string.player_ai_subtitle)
                .setMessage(message)
                .setNegativeButton(R.string.dialog_negative, null)
                .setPositiveButton(R.string.ai_subtitle_go_download, (dialog, which) -> AiSubtitleSettingsActivity.start(activity))
                .show();
    }
}
