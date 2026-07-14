package com.fongmi.android.tv.ai.subtitle;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.viewbinding.ViewBinding;
import androidx.core.graphics.ColorUtils;

import com.fongmi.android.tv.R;
import com.fongmi.android.tv.databinding.ActivityAiSubtitleSettingsBinding;
import com.fongmi.android.tv.setting.Setting;
import com.fongmi.android.tv.ui.base.BaseActivity;
import com.fongmi.android.tv.utils.ResUtil;
import com.fongmi.android.tv.utils.Util;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;

import java.util.Locale;
import java.util.ArrayList;
import java.util.List;
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
        setupDropdowns();
        binding.mtranUrl.setText(AiSubtitleSettings.getMTranUrl());
        binding.baseUrl.setText(AiSubtitleSettings.getBaseUrl());
        binding.modelInput.setText(AiSubtitleSettings.getTranslationModel(), false);
        setupSecretInputs();
        binding.thinkingSwitch.setChecked(AiSubtitleSettings.isThinkingEnabled());
        binding.contextSwitch.setChecked(AiSubtitleSettings.isContextEnabled());
        updateLanguageStatus();
        updateProviderUi();
        binding.enabledSwitch.requestFocus();
    }

    private void setupSecretInputs() {
        boolean mtranSaved = !SecretStore.getMTranToken().isEmpty();
        boolean apiSaved = !SecretStore.getApiKey().isEmpty();
        if (Util.isLeanback()) {
            binding.mtranTokenPush.setVisibility(View.VISIBLE);
            binding.apiKeyPush.setVisibility(View.VISIBLE);
            binding.mtranToken.setText(mtranSaved ? SECRET_MASK : "");
            binding.apiKey.setText(apiSaved ? SECRET_MASK : "");
            binding.mtranToken.setSelectAllOnFocus(true);
            binding.apiKey.setSelectAllOnFocus(true);
            binding.mtranTokenLayout.setHelperText(mtranSaved ? "Token 已安全保存；留空保持不变" : null);
            binding.apiKeyLayout.setHelperText(apiSaved ? "API Key 已安全保存；留空保持不变" : null);
        } else {
            binding.mtranTokenPush.setVisibility(View.GONE);
            binding.apiKeyPush.setVisibility(View.GONE);
            binding.mtranToken.setText(mtranSaved ? SECRET_MASK : "");
            binding.apiKey.setText(apiSaved ? SECRET_MASK : "");
            binding.mtranToken.setSelectAllOnFocus(true);
            binding.apiKey.setSelectAllOnFocus(true);
            binding.mtranTokenLayout.setHelperText(mtranSaved ? "Token 已配置" : null);
            binding.apiKeyLayout.setHelperText(apiSaved ? "API Key 已配置" : null);
        }
    }

    private void setupDropdowns() {
        styleDropdown(binding.languageInput);
        styleDropdown(binding.providerInput);
        styleDropdown(binding.modeInput);
        styleDropdown(binding.modelInput);
        bindSelector(binding.languageInput, AiLanguage.values(), selectedLanguage, "选择识别语种", value -> {
            selectedLanguage = value;
            updateLanguageStatus();
        });
        bindSelector(binding.providerInput, AiSubtitleSettings.TranslationProvider.values(), selectedProvider, "选择翻译后端", value -> {
            selectedProvider = value;
            binding.testResult.setText("");
            updateProviderUi();
        });
        bindSelector(binding.modeInput, AiSubtitleSettings.SubtitleMode.values(), selectedMode, "选择字幕显示方式", value -> selectedMode = value);
        if (Util.isLeanback()) {
            binding.modelInput.setAdapter(dropdown(new String[0]));
            binding.modelInput.setFocusable(true);
            binding.modelInput.setFocusableInTouchMode(true);
            binding.modelInput.setClickable(true);
            binding.modelInput.setOnClickListener(v -> showModelDialog());
            binding.modelInput.setOnKeyListener((v, keyCode, event) -> {
                boolean confirm = keyCode == KeyEvent.KEYCODE_DPAD_CENTER || keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_NUMPAD_ENTER;
                if (!confirm) return false;
                if (event.getAction() == KeyEvent.ACTION_UP) showModelDialog();
                return true;
            });
        } else {
            binding.modelInput.setAdapter(dropdown(LLM_PRESETS));
        }
    }

    private <T> void bindSelector(MaterialAutoCompleteTextView view, T[] values, T current, String title, Consumer<T> selected) {
        view.setText(current.toString(), false);
        if (Util.isLeanback()) {
            view.setAdapter(dropdown(new String[0]));
            view.setFocusable(true);
            view.setFocusableInTouchMode(true);
            view.setClickable(true);
            view.setOnClickListener(v -> {
                view.dismissDropDown();
                showLeanbackChoice(view, values, title, selected);
            });
            view.setOnKeyListener((v, keyCode, event) -> {
                boolean confirm = keyCode == KeyEvent.KEYCODE_DPAD_CENTER || keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_NUMPAD_ENTER;
                if (!confirm) return false;
                if (event.getAction() == KeyEvent.ACTION_UP) showLeanbackChoice(view, values, title, selected);
                return true;
            });
        } else {
            view.setAdapter(dropdown(values));
            view.setOnClickListener(v -> view.showDropDown());
            view.setOnItemClickListener((parent, row, position, id) -> selected.accept(values[position]));
        }
    }

    private <T> void showLeanbackChoice(MaterialAutoCompleteTextView view, T[] values, String title, Consumer<T> selected) {
        String[] labels = new String[values.length];
        int checked = 0;
        for (int i = 0; i < values.length; i++) {
            labels[i] = values[i].toString();
            if (labels[i].contentEquals(view.getText())) checked = i;
        }
        AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setTitle(title)
                .setNegativeButton(R.string.dialog_negative, null)
                .setSingleChoiceItems(labels, checked, (choice, position) -> {
                    if (position < 0 || position >= values.length) return;
                    T value = values[position];
                    view.setText(value.toString(), false);
                    selected.accept(value);
                    choice.dismiss();
                    view.requestFocus();
                })
                .create();
        dialog.setOnShowListener(ignored -> {
            themeChoiceDialog(dialog);
            dialog.getListView().requestFocus();
            dialog.getListView().setSelection(dialog.getListView().getCheckedItemPosition());
        });
        dialog.show();
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
                        binding.modelInput.setText(LLM_PRESETS[which], false);
                    } else {
                        binding.modelInput.setOnClickListener(null);
                        binding.modelInput.requestFocus();
                        binding.modelInput.post(() -> {
                            InputMethodManager input = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                            input.showSoftInput(binding.modelInput, InputMethodManager.SHOW_IMPLICIT);
                            binding.modelInput.setOnClickListener(v -> showModelDialog());
                        });
                    }
                })
                .create();
        dialog.setOnShowListener(ignored -> themeChoiceDialog(dialog));
        dialog.show();
    }

    private void themeChoiceDialog(AlertDialog dialog) {
        Window window = dialog.getWindow();
        if (window == null) return;
        GradientDrawable background = new GradientDrawable();
        background.setColor(ColorUtils.setAlphaComponent(ColorUtils.blendARGB(themeSeed(), Color.BLACK, 0.36f), 224));
        background.setCornerRadius(ResUtil.dp2px(20));
        background.setStroke(ResUtil.dp2px(1), ColorUtils.setAlphaComponent(Color.WHITE, 100));
        window.setBackgroundDrawable(background);
        window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        WindowManager.LayoutParams params = window.getAttributes();
        params.dimAmount = 0.18f;
        window.setAttributes(params);
    }

    private <T> ArrayAdapter<T> dropdown(T[] values) {
        int seed = themeSeed();
        int normal = ColorUtils.setAlphaComponent(ColorUtils.blendARGB(seed, Color.BLACK, 0.28f), 205);
        int focused = ColorUtils.setAlphaComponent(ColorUtils.blendARGB(seed, Color.WHITE, 0.18f), 235);
        ArrayAdapter<T> adapter = new ArrayAdapter<>(this, R.layout.item_ai_dropdown, R.id.text, values) {
            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                View row = super.getDropDownView(position, convertView, parent);
                StateListDrawable background = new StateListDrawable();
                background.addState(new int[]{android.R.attr.state_focused}, new ColorDrawable(focused));
                background.addState(new int[]{android.R.attr.state_pressed}, new ColorDrawable(focused));
                background.addState(new int[]{android.R.attr.state_activated}, new ColorDrawable(focused));
                background.addState(new int[]{android.R.attr.state_selected}, new ColorDrawable(focused));
                background.addState(new int[0], new ColorDrawable(normal));
                row.setBackground(background);
                return row;
            }
        };
        adapter.setDropDownViewResource(R.layout.item_ai_dropdown);
        return adapter;
    }

    private void styleDropdown(MaterialAutoCompleteTextView view) {
        int surface = ColorUtils.setAlphaComponent(ColorUtils.blendARGB(themeSeed(), Color.BLACK, 0.28f), 210);
        GradientDrawable background = new GradientDrawable();
        background.setColor(surface);
        background.setCornerRadius(ResUtil.dp2px(12));
        background.setStroke(ResUtil.dp2px(1), ColorUtils.setAlphaComponent(Color.WHITE, 90));
        view.setDropDownBackgroundDrawable(background);
    }

    private int themeSeed() {
        int seed = Setting.getDynamicColor();
        return seed == 0 ? 0xFF40C090 : seed;
    }

    @Override
    protected void initEvent() {
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

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (Util.isLeanback() && (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_DOWN || event.getKeyCode() == KeyEvent.KEYCODE_DPAD_UP)) {
            if (event.getAction() == KeyEvent.ACTION_DOWN && event.getRepeatCount() == 0) {
                moveVerticalFocus(event.getKeyCode() == KeyEvent.KEYCODE_DPAD_DOWN);
            }
            return true;
        }
        if (Util.isLeanback() && isConfirmKey(event.getKeyCode())) {
            View focused = getCurrentFocus();
            boolean selector = focused == binding.languageInput || focused == binding.providerInput || focused == binding.modeInput || focused == binding.modelInput;
            if (selector) {
                if (event.getAction() == KeyEvent.ACTION_UP && event.getRepeatCount() == 0) openFocusedSelector(focused);
                return true;
            }
        }
        return super.dispatchKeyEvent(event);
    }

    private void moveVerticalFocus(boolean down) {
        List<View> order = new ArrayList<>();
        addFocusable(order, binding.enabledSwitch);
        addFocusable(order, binding.languageInput);
        View languageAction = binding.download.isEnabled() ? binding.download : binding.delete.isEnabled() ? binding.delete : null;
        addFocusable(order, languageAction);
        addFocusable(order, binding.providerInput);
        addFocusable(order, binding.modeInput);
        addFocusable(order, binding.mtranUrl);
        addFocusable(order, binding.mtranToken);
        addFocusable(order, binding.mtranTokenPush);
        addFocusable(order, binding.baseUrl);
        addFocusable(order, binding.modelInput);
        addFocusable(order, binding.apiKey);
        addFocusable(order, binding.apiKeyPush);
        addFocusable(order, binding.thinkingSwitch);
        addFocusable(order, binding.contextSwitch);
        addFocusable(order, binding.testBackend);
        addFocusable(order, binding.save);
        if (order.isEmpty()) return;
        View focused = getCurrentFocus();
        int current = order.indexOf(focused);
        if (focused == binding.download || focused == binding.delete) current = order.indexOf(languageAction);
        int next = current < 0 ? (down ? 0 : order.size() - 1) : Math.max(0, Math.min(order.size() - 1, current + (down ? 1 : -1)));
        order.get(next).requestFocus();
    }

    private static void addFocusable(List<View> order, View view) {
        if (view != null && view.isShown() && view.isEnabled() && view.isFocusable()) order.add(view);
    }

    private static boolean isConfirmKey(int keyCode) {
        return keyCode == KeyEvent.KEYCODE_DPAD_CENTER || keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_NUMPAD_ENTER;
    }

    private void openFocusedSelector(View focused) {
        if (focused == binding.languageInput) {
            showLeanbackChoice(binding.languageInput, AiLanguage.values(), "选择识别语种", value -> {
                selectedLanguage = value;
                updateLanguageStatus();
            });
        } else if (focused == binding.providerInput) {
            showLeanbackChoice(binding.providerInput, AiSubtitleSettings.TranslationProvider.values(), "选择翻译后端", value -> {
                selectedProvider = value;
                binding.testResult.setText("");
                updateProviderUi();
            });
        } else if (focused == binding.modeInput) {
            showLeanbackChoice(binding.modeInput, AiSubtitleSettings.SubtitleMode.values(), "选择字幕显示方式", value -> selectedMode = value);
        } else if (focused == binding.modelInput) {
            showModelDialog();
        }
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
            binding.mtranTokenLayout.setHelperText(Util.isLeanback() ? "Token 已安全保存；留空保持不变" : "Token 已配置");
            if (clearSecrets) binding.mtranToken.setText(SECRET_MASK);
        }
        String apiKey = text(binding.apiKey);
        if (!apiKey.isBlank() && !SECRET_MASK.equals(apiKey)) {
            SecretStore.putApiKey(apiKey);
            binding.apiKeyLayout.setHelperText(Util.isLeanback() ? "API Key 已安全保存；留空保持不变" : "API Key 已配置");
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

    private static String text(android.widget.TextView view) {
        return view.getText() == null ? "" : view.getText().toString().trim();
    }

    @Override
    protected void onDestroy() {
        if (tester != null) tester.reset();
        super.onDestroy();
    }
}
