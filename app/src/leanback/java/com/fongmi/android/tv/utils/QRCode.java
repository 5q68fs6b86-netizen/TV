package com.fongmi.android.tv.utils;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;

import com.fongmi.android.tv.ui.theme.JetStreamPalette;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;

import java.util.EnumMap;
import java.util.Map;

public class QRCode {

    private static final int FINDER_MODULES = 7;
    private static final float MODULE_RADIUS_RATIO = 0.45f;
    private static final int FOREGROUND = Color.WHITE;
    private static final int BACKGROUND = Color.BLACK;
    private static final int LIGHT_FOREGROUND = Color.BLACK;
    private static final int LIGHT_BACKGROUND = Color.WHITE;
    private static final float BACKGROUND_RADIUS_RATIO = 0.08f;

    public static Bitmap getBitmap(String content, int size, int margin) {
        return getBitmap(content, size, margin, FOREGROUND, BACKGROUND);
    }

    public static Bitmap getLightBitmap(String content, int size, int margin) {
        return getBitmap(content, size, margin, LIGHT_FOREGROUND, LIGHT_BACKGROUND);
    }

    public static Bitmap getJetStreamBitmap(String content, int size, int margin) {
        return getBitmap(content, size, margin, FOREGROUND, JetStreamPalette.surfaceInt());
    }

    private static Bitmap getBitmap(String content, int size, int margin, int foreground, int background) {
        try {
            BitMatrix matrix = encode(content, size, margin);
            int quietZone = detectQuietZone(matrix);
            int finderSize = detectFinderPx(matrix, quietZone);
            return render(matrix, quietZone, finderSize, foreground, background);
        } catch (Exception e) {
            return null;
        }
    }

    private static BitMatrix encode(String content, int size, int margin) throws Exception {
        Map<EncodeHintType, Object> hints = new EnumMap<>(EncodeHintType.class);
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        hints.put(EncodeHintType.MARGIN, margin);
        return new MultiFormatWriter().encode(content, BarcodeFormat.QR_CODE, ResUtil.dp2px(size), ResUtil.dp2px(size), hints);
    }

    private static int detectQuietZone(BitMatrix matrix) {
        for (int y = 0; y < matrix.getHeight(); y++) for (int x = 0; x < matrix.getWidth(); x++) if (matrix.get(x, y)) return Math.min(x, y);
        return 0;
    }

    private static int detectFinderPx(BitMatrix matrix, int quietZone) {
        for (int x = quietZone; x < matrix.getWidth(); x++) if (!matrix.get(x, quietZone)) return x - quietZone;
        return 0;
    }

    private static Bitmap render(BitMatrix matrix, int quietZone, int finderSize, int foreground, int background) {
        int width = matrix.getWidth(), height = matrix.getHeight();
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawBackground(canvas, width, height, background);
        float moduleSize = finderSize > 0 ? (float) finderSize / FINDER_MODULES : 1;
        int numModules = finderSize > 0 ? Math.round((width - 2f * quietZone) / moduleSize) : 0;
        drawDataModules(canvas, matrix, quietZone, moduleSize, numModules, width, height, foreground);
        if (finderSize <= 0) return bitmap;
        drawFinderPattern(canvas, quietZone, quietZone, finderSize, foreground, background);
        drawFinderPattern(canvas, width - quietZone - finderSize, quietZone, finderSize, foreground, background);
        drawFinderPattern(canvas, quietZone, height - quietZone - finderSize, finderSize, foreground, background);
        return bitmap;
    }

    private static void drawBackground(Canvas canvas, int width, int height, int color) {
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(color);
        float radius = Math.min(width, height) * BACKGROUND_RADIUS_RATIO;
        canvas.drawRoundRect(new RectF(0, 0, width, height), radius, radius, paint);
    }

    private static void drawDataModules(Canvas canvas, BitMatrix matrix, int quietZone, float moduleSize, int numModules, int width, int height, int color) {
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(color);
        float radius = moduleSize * MODULE_RADIUS_RATIO;
        for (int my = 0; my < numModules; my++) {
            for (int mx = 0; mx < numModules; mx++) {
                if (isFinderRegion(mx, my, numModules)) continue;
                float px = quietZone + (mx + 0.5f) * moduleSize;
                float py = quietZone + (my + 0.5f) * moduleSize;
                if (matrix.get(Math.min((int) px, width - 1), Math.min((int) py, height - 1))) canvas.drawCircle(px, py, radius, paint);
            }
        }
    }

    private static boolean isFinderRegion(int moduleX, int moduleY, int numModules) {
        boolean topLeft = moduleX < FINDER_MODULES && moduleY < FINDER_MODULES;
        boolean topRight = moduleX >= numModules - FINDER_MODULES && moduleY < FINDER_MODULES;
        boolean bottomLeft = moduleX < FINDER_MODULES && moduleY >= numModules - FINDER_MODULES;
        return topLeft || topRight || bottomLeft;
    }

    private static void drawFinderPattern(Canvas canvas, int left, int top, int finderSize, int foreground, int background) {
        float module = finderSize / (float) FINDER_MODULES;
        float cx = left + finderSize / 2f;
        float cy = top + finderSize / 2f;
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(foreground);
        canvas.drawCircle(cx, cy, finderSize / 2f, paint);
        paint.setColor(background);
        canvas.drawCircle(cx, cy, finderSize / 2f - module, paint);
        paint.setColor(foreground);
        canvas.drawCircle(cx, cy, finderSize / 2f - 2 * module, paint);
    }
}
