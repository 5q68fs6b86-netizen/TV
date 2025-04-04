package com.fongmi.android.tv.utils;

import android.Manifest;
import android.app.Notification;
import android.content.Context;
import android.content.pm.PackageManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationChannelCompat;
import androidx.core.app.NotificationManagerCompat;

import com.fongmi.android.tv.App;
import com.fongmi.android.tv.databinding.ViewProgressBinding; // 确保这个 import 正确
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class Notify {

    public static final String DEFAULT = "default";
    public static final int ID = 9527;
    private AlertDialog mDialog;
    private Toast mToast;

    // --- MODIFICATION: Define the forbidden text ---
    private static final String FORBIDDEN_TEXT = "王二小放牛娃";
    // --- END MODIFICATION ---

    private static class Loader {
        static volatile Notify INSTANCE = new Notify();
    }

    private static Notify get() {
        return Loader.INSTANCE;
    }

    public static void createChannel() {
        NotificationManagerCompat notifyMgr = NotificationManagerCompat.from(App.get());
        notifyMgr.createNotificationChannel(new NotificationChannelCompat.Builder(DEFAULT, NotificationManagerCompat.IMPORTANCE_LOW).setName("TV").build());
    }

    public static String getError(int resId, Throwable e) {
        // Check if the error message itself contains the forbidden text? Optional.
        // Currently only filters toasts, not the generated error string.
        if (TextUtils.isEmpty(e.getMessage())) return ResUtil.getString(resId);
        return ResUtil.getString(resId) + "\n" + e.getMessage();
    }

    public static void show(Notification notification) {
        if (ActivityCompat.checkSelfPermission(App.get(), Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) return;
        NotificationManagerCompat.from(App.get()).notify(ID, notification);
    }

    public static void show(int resId) {
        if (resId != 0) {
            // Get the string first, then pass it to show(String) which calls makeText
            show(ResUtil.getString(resId));
        }
    }

    public static void show(String text) {
        // This method calls makeText where the filtering happens
        get().makeText(text);
    }

    public static void progress(Context context) {
        dismiss();
        get().create(context);
    }

    public static void dismiss() {
        try {
            if (get().mDialog != null && get().mDialog.isShowing()) { // Check if showing before dismiss
                get().mDialog.dismiss();
            }
        } catch (Exception ignored) {
            // Ignored as per original code
        } finally {
             // Ensure dialog reference is cleared after dismissal attempt
             if (get().mDialog != null) {
                 get().mDialog = null;
             }
        }
    }

    private void create(Context context) {
        // Ensure previous dialog is fully dismissed before creating a new one
        if (mDialog != null && mDialog.isShowing()) {
             try {
                 mDialog.dismiss();
             } catch (Exception ignored) {}
        }
        mDialog = null; // Clear reference

        try {
            ViewProgressBinding binding = ViewProgressBinding.inflate(LayoutInflater.from(context));
            mDialog = new MaterialAlertDialogBuilder(context).setView(binding.getRoot()).create();
            if (mDialog.getWindow() != null) { // Null check for window
                mDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            }
            mDialog.setCancelable(false); // Make progress dialog typically non-cancelable by back press
            mDialog.show();
        } catch (Exception e) {
            // Handle exceptions during dialog creation, e.g., log it
             System.err.println("Error creating progress dialog: " + e.getMessage());
             mDialog = null; // Ensure dialog is null if creation failed
        }
    }

    // --- MODIFIED: Added check in makeText ---
    private void makeText(String message) {
        // Cancel any previous toast
        if (mToast != null) {
            mToast.cancel();
        }
        // Don't show if message is empty
        if (TextUtils.isEmpty(message)) {
            return;
        }

        // >>> Check if the message contains the forbidden text <<<
        if (message.contains(FORBIDDEN_TEXT)) {
            // If it contains the text, simply return and do nothing
            // Optional: Log that a toast was filtered
             System.out.println("Filtered toast message containing '" + FORBIDDEN_TEXT + "': " + message);
            return;
        }
        // >>> End of check <<<

        // If the message is valid and does not contain forbidden text, show the toast
        try {
            mToast = Toast.makeText(App.get(), message, Toast.LENGTH_LONG);
            mToast.show();
        } catch (Exception e) {
            // Catch potential exceptions during Toast creation/showing
            System.err.println("Error showing toast: " + e.getMessage());
            mToast = null; // Clear reference if failed
        }
    }
    // --- END MODIFICATION ---
}