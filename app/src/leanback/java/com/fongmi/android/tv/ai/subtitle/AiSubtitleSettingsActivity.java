package com.fongmi.android.tv.ai.subtitle;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.viewbinding.ViewBinding;

import com.fongmi.android.tv.R;
import com.fongmi.android.tv.databinding.ActivityAiSubtitleSettingsBinding;
import com.fongmi.android.tv.ui.base.BaseActivity;
import com.fongmi.android.tv.ui.custom.JetStreamDialogDecor;
import com.fongmi.android.tv.utils.ResUtil;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.Locale;
import java.util.function.Consumer;

public final class AiSubtitleSettingsActivity extends BaseActivity implements AiSecretPushDialog.Listener {

    private static final String TAG = "AiSubtitle";
    private static final String SECRET_MASK = "••••••••";
    private static final String[] LLM_PRESETS = {"deepseek-v4-flash", "deepseek-v4-pro", "deepseek-chat"};

    private ActivityAiSubtitleSettingsBinding binding;
    private AsrModelManager modelManager;
    private AiLanguage selectedLanguage;
    private AiSubtitleSettings.TranslationProvider selectedProvider;
    private AiSubtitleSettings.SubtitleMode selectedMode;
    private OpenAiSubtitleTranslator tester;

    public static void start(Context context) {
        context.startActivity(new Intent(context, AiSubtitleSettingsActivity.class));
    }

    @Override
    protected ViewBinding getBinding() {
        return (binding = ActivityAiSubtitleSettingsBinding.inflate(getLayoutInflater()));
    }

    @Override
    protected void initView(Bundle state) {
        modelManager = AiSubtitleRuntime.get().models();
        selectedLanguage = AiSubtitleSettings.getLanguage();
        selectedProvider = AiSubtitleSettings.getTranslationProvider();
        selectedMode = AiSubtitleSettings.getSubtitleMode();
        tester = new OpenAiSubtitleTranslator();

        binding.enabledSwitch.setChecked(AiSubtitleSettings.isEnabled());
        binding.languageInput.setText(selectedLanguage.toString());
        binding.providerInput.setText(selectedProvider.toString());
        binding.modeInput.setText(selectedMode.toString());
        binding.mtranUrl.setText(AiSubtitleSettings.getMTranUrl());
        binding.baseUrl.setText(AiSubtitleSettings.getBaseUrl());
        binding.modelInput.setText(AiSubtitleSettings.getTranslationModel());
        binding.thinkingSwitch.setChecked(AiSubtitleSettings.isThinkingEnabled());
        binding.contextSwitch.setChecked(AiSubtitleSettings.isContextEnabled());
        setupSecretInputs();
        updateLanguageStatus();
        updateProviderUi();
        binding.enabledSwitch.requestFocus();
    }

    private void setupSecretInputs() {
        boolean mtranSaved = !SecretStore.getMTranToken().isEmpty();
        boolean apiSaved = !SecretStore.getApiKey().isEmpty();
        binding.mtranToken.setText(mtranSaved ? SECRET_MASK : "");
        binding.apiKey.setText(apiSaved ? SECRET_MASK : "");
        binding.mtranToken.setSelectAllOnFocus(true);
        binding.apiKey.setSelectAllOnFocus(true);
        setHelper(binding.mtranTokenHelper, mtranSaved ? "Token 已安全保存；留空保持不变" : null);
        setHelper(binding.apiKeyHelper, apiSaved ? "API Key 已安全保存；留空保持不变" : null);
    }

    private static void setHelper(TextView view, String text) {
        view.setText(text == null ? "" : text);
        view.setVisibility(text == null ? View.GONE : View.VISIBLE);
    }

    @Override
    protected void initEvent() {
        binding.languageInput.setOnClickListener(v -> showChoice(binding.languageInput, AiLanguage.values(), "选择识别语种", value -> {
            selectedLanguage = value;
            updateLanguageStatus();
        }));
        binding.providerInput.setOnClickListener(v -> showChoice(binding.providerInput, AiSubtitleSettings.TranslationProvider.values(), "选择翻译后端", value -> {
            selectedProvider = value;
            binding.testResult.setText("");
            updateProviderUi();
        }));
        binding.modeInput.setOnClickListener(v -> showChoice(binding.modeInput, AiSubtitleSettings.SubtitleMode.values(), "选择字幕显示方式", value -> selectedMode = value));
        binding.modelInput.setOnClickListener(v -> showModelDialog());
        binding.download.setOnClickListener(v -> downloadLanguage());
        binding.delete.setOnClickListener(v -> deleteLanguage());
        binding.testBackend.setOnClickListener(v -> testBackend());
        binding.mtranTokenPush.setOnClickListener(v -> AiSecretPushDialog.create(AiSecretPushDialog.TARGET_MTRAN_TOKEN).show(this));
        binding.apiKeyPush.setOnClickListener(v -> AiSecretPushDialog.create(AiSecretPushDialog.TARGET_API_KEY).show(this));
        binding.save.setOnClickListener(v -> {
            persistForm(true);
            AiSubtitleRuntime.get().onSettingsChanged();
            Toast.makeText(this, "已保存并立即应用", Toast.LENGTH_SHORT).show();
            binding.testResult.setText("");
            updateLanguageStatus();
            updateProviderUi();
        });
    }

    private <T> void showChoice(TextView view, T[] values, String title, Consumer<T> selected) {
        String[] labels = new String[values.length];
        int checked = 0;
        CharSequence current = view.getText();
        for (int i = 0; i < values.length; i++) {
            labels[i] = values[i].toString();
            if (current != null && labels[i].contentEquals(current)) checked = i;
        }
        AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setTitle(title)
                .setNegativeButton(R.string.dialog_negative, null)
                .setSingleChoiceItems(labels, checked, (choice, position) -> {
                    if (position < 0 || position >= values.length) return;
                    T value = values[position];
                    view.setText(value.toString());
                    selected.accept(value);
                    choice.dismiss();
                    view.requestFocus();
                })
                .show();
        JetStreamDialogDecor.tintButtons(dialog);
    }

    private void showModelDialog() {
        String custom = "自定义模型名…";
        String[] labels = {LLM_PRESETS[0], LLM_PRESETS[1], LLM_PRESETS[2], custom};
        AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setTitle("选择翻译模型")
                .setNegativeButton(R.string.dialog_negative, null)
                .setSingleChoiceItems(labels, -1, (choice, which) -> {
                    choice.dismiss();
                    if (which < LLM_PRESETS.length) {
                        binding.modelInput.setText(LLM_PRESETS[which]);
                        binding.modelInput.requestFocus();
                    } else {
                        showModelInputDialog();
                    }
                })
                .show();
        JetStreamDialogDecor.tintButtons(dialog);
    }

    private void showModelInputDialog() {
        EditText input = new EditText(this);
        int padding = ResUtil.dp2px(24);
        input.setHint("输入自定义模型名");
        input.setSingleLine(true);
        input.setText(text(binding.modelInput));
        input.setPadding(padding, ResUtil.dp2px(12), padding, ResUtil.dp2px(12));
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setSelection(input.length());
        AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setTitle("自定义翻译模型")
                .setView(input)
                .setPositiveButton(R.string.dialog_positive, (d, which) -> {
                    binding.modelInput.setText(input.getText().toString().trim());
                    binding.modelInput.requestFocus();
                })
                .setNegativeButton(R.string.dialog_negative, null)
                .show();
        JetStreamDialogDecor.tintButtons(dialog);
    }

    private void updateProviderUi() {
        binding.mtranGroup.setVisibility(selectedProvider == AiSubtitleSettings.TranslationProvider.MTRAN ? View.VISIBLE : View.GONE);
        binding.llmGroup.setVisibility(selectedProvider == AiSubtitleSettings.TranslationProvider.OPENAI ? View.VISIBLE : View.GONE);
        binding.testBackend.setVisibility(selectedProvider == AiSubtitleSettings.TranslationProvider.OFF ? View.GONE : View.VISIBLE);
        binding.testResult.setVisibility(selectedProvider == AiSubtitleSettings.TranslationProvider.OFF ? View.GONE : View.VISIBLE);
    }

    private void updateLanguageStatus() {
        boolean installed = modelManager.isInstalled(selectedLanguage);
        long mb = (modelManager.requiredBytes(selectedLanguage) + 1024 * 1024 - 1) / (1024 * 1024);
        String memory = modelManager.canRun(selectedLanguage) ? "" : "；设备内存低于安全门槛";
        binding.languageStatus.setText(selectedLanguage.label() + "语言包：" + (installed ? "已安装" : "未安装，约 " + mb + " MB") + memory);
        binding.download.setEnabled(!installed && !modelManager.isDownloading() && modelManager.canRun(selectedLanguage));
        binding.delete.setEnabled(installed && !modelManager.isDownloading());
    }

    private void downloadLanguage() {
        AiLanguage language = selectedLanguage;
        binding.download.setEnabled(false);
        modelManager.download(language, new AsrModelManager.Listener() {
            @Override
            public void onProgress(int percent, String fileName) {
                runOnUiThread(() -> binding.languageStatus.setText(String.format(Locale.ROOT, "%s语言包：%d%%", language.label(), percent)));
            }

            @Override
            public void onComplete() {
                runOnUiThread(() -> {
                    AiSubtitleRuntime.get().onSettingsChanged();
                    updateLanguageStatus();
                });
            }

            @Override
            public void onError(String message) {
                Log.w(TAG, "model download skipped: " + message);
                runOnUiThread(AiSubtitleSettingsActivity.this::updateLanguageStatus);
            }
        });
    }

    private void deleteLanguage() {
        modelManager.delete(selectedLanguage);
        AiSubtitleRuntime.get().onSettingsChanged();
        updateLanguageStatus();
    }

    private void testBackend() {
        persistForm(false);
        binding.testBackend.setEnabled(false);
        binding.testResult.setText("检测中…");
        tester.reset();
        tester.test(selectedLanguage, (available, latencyMs, detail) -> runOnUiThread(() -> {
            if (isFinishing() || isDestroyed()) return;
            binding.testBackend.setEnabled(true);
            binding.testResult.setText(available ? "可用 · " + latencyMs + " ms" : "不可用 · " + latencyMs + " ms" + (detail.isBlank() ? "" : " · " + detail));
        }));
    }

    private void persistForm(boolean clearSecrets) {
        boolean requestedEnabled = binding.enabledSwitch.isChecked();
        boolean ready = modelManager.isInstalled(selectedLanguage) && modelManager.canRun(selectedLanguage);
        AiSubtitleSettings.setEnabled(requestedEnabled && ready);
        if (requestedEnabled && !ready) {
            binding.enabledSwitch.setChecked(false);
            Toast.makeText(this, "请先下载“" + selectedLanguage.label() + "”语言包", Toast.LENGTH_SHORT).show();
        }
        AiSubtitleSettings.setLanguage(selectedLanguage);
        AiSubtitleSettings.setTranslationProvider(selectedProvider);
        AiSubtitleSettings.setSubtitleMode(selectedMode);
        AiSubtitleSettings.setMTranUrl(text(binding.mtranUrl));
        AiSubtitleSettings.setBaseUrl(text(binding.baseUrl));
        AiSubtitleSettings.setTranslationModel(text(binding.modelInput));
        AiSubtitleSettings.setThinkingEnabled(binding.thinkingSwitch.isChecked());
        AiSubtitleSettings.setContextEnabled(binding.contextSwitch.isChecked());
        String mtranToken = text(binding.mtranToken);
        if (!mtranToken.isBlank() && !SECRET_MASK.equals(mtranToken)) {
            SecretStore.putMTranToken(mtranToken);
            setHelper(binding.mtranTokenHelper, "Token 已安全保存；留空保持不变");
            if (clearSecrets) binding.mtranToken.setText(SECRET_MASK);
        }
        String apiKey = text(binding.apiKey);
        if (!apiKey.isBlank() && !SECRET_MASK.equals(apiKey)) {
            SecretStore.putApiKey(apiKey);
            setHelper(binding.apiKeyHelper, "API Key 已安全保存；留空保持不变");
            if (clearSecrets) binding.apiKey.setText(SECRET_MASK);
        }
    }

    @Override
    public void onAiSecretInput(String target, String value) {
        if (AiSecretPushDialog.TARGET_MTRAN_TOKEN.equals(target)) {
            binding.mtranToken.setText(value);
            binding.mtranToken.setSelection(binding.mtranToken.length());
        } else if (AiSecretPushDialog.TARGET_API_KEY.equals(target)) {
            binding.apiKey.setText(value);
            binding.apiKey.setSelection(binding.apiKey.length());
        }
    }

    private static String text(TextView view) {
        return view.getText() == null ? "" : view.getText().toString().trim();
    }

    @Override
    protected void onDestroy() {
        if (tester != null) tester.reset();
        super.onDestroy();
    }
}
