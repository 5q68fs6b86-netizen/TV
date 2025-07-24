package com.fongmi.android.tv.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.Shader;
import androidx.core.content.ContextCompat;

import com.fongmi.android.tv.R;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

import java.util.EnumMap;
import java.util.Map;

public class QRCode {

    // 圆角配置
    private static final float CORNER_RADIUS_DP = 20f;

    /**
     * Material 3 主题枚举 - 使用资源 ID
     */
    public enum Material3Theme {
        LIGHT_PRIMARY(
            R.color.md_theme_onPrimaryContainer,    // 前景色
            R.color.md_theme_primaryContainer,      // 背景色
            R.color.md_theme_primary                // 边框色
        ),
        LIGHT_SURFACE(
            R.color.md_theme_onSurface,
            R.color.md_theme_surface,
            R.color.md_theme_outlineVariant
        ),
        LIGHT_SURFACE_VARIANT(
            R.color.md_theme_onSurfaceVariant,
            R.color.md_theme_surfaceVariant,
            R.color.md_theme_outline
        ),
        DARK_PRIMARY(
            R.color.md_theme_onPrimaryContainer,
            R.color.md_theme_primaryContainer,
            R.color.md_theme_primary
        ),
        DARK_SURFACE(
            R.color.md_theme_onSurface,
            R.color.md_theme_surface,
            R.color.md_theme_outlineVariant
        ),
        DARK_SURFACE_VARIANT(
            R.color.md_theme_onSurfaceVariant,
            R.color.md_theme_surfaceVariant,
            R.color.md_theme_outline
        );

        public final int foregroundColorRes;
        public final int backgroundColorRes;
        public final int accentColorRes;

        Material3Theme(int foregroundColorRes, int backgroundColorRes, int accentColorRes) {
            this.foregroundColorRes = foregroundColorRes;
            this.backgroundColorRes = backgroundColorRes;
            this.accentColorRes = accentColorRes;
        }

        /**
         * 获取实际颜色值
         */
        public int getForegroundColor(Context context) {
            return ContextCompat.getColor(context, foregroundColorRes);
        }

        public int getBackgroundColor(Context context) {
            return ContextCompat.getColor(context, backgroundColorRes);
        }

        public int getAccentColor(Context context) {
            return ContextCompat.getColor(context, accentColorRes);
        }
    }

    /**
     * 创建 Material 3 风格的圆角二维码
     */
    public static Bitmap createMaterial3QR(Context context, BitMatrix matrix, Material3Theme theme) {
        return createMaterial3QR(context, matrix, theme, true, true);
    }

    /**
     * 创建 Material 3 风格的圆角二维码（完整版本）
     */
    public static Bitmap createMaterial3QR(Context context, BitMatrix matrix, Material3Theme theme, 
                                          boolean withElevation, boolean withGradient) {
        int width = matrix.getWidth();
        int height = matrix.getHeight();
        
        // 添加内边距以适应圆角和阴影
        int padding = ResUtil.dp2px(16);
        int totalWidth = width + padding * 2;
        int totalHeight = height + padding * 2;
        
        Bitmap result = Bitmap.createBitmap(totalWidth, totalHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(result);
        
        // 绘制阴影（如果启用）
        if (withElevation) {
            drawMaterial3Shadow(canvas, padding, padding, width, height);
        }
        
        // 绘制圆角背景
        drawMaterial3Background(context, canvas, padding, padding, width, height, theme, withGradient);
        
        // 绘制二维码内容
        drawQRContent(context, canvas, matrix, padding, padding, theme);
        
        return result;
    }

    /**
     * 绘制 Material 3 阴影效果
     */
    private static void drawMaterial3Shadow(Canvas canvas, int x, int y, int width, int height) {
        Paint shadowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        shadowPaint.setColor(0x1A000000); // 10% 透明度的黑色
        
        float radius = ResUtil.dp2px(CORNER_RADIUS_DP);
        float shadowOffset = ResUtil.dp2px(2);
        
        RectF shadowRect = new RectF(
            x + shadowOffset, 
            y + shadowOffset, 
            x + width + shadowOffset, 
            y + height + shadowOffset
        );
        
        canvas.drawRoundRect(shadowRect, radius, radius, shadowPaint);
    }

    /**
     * 绘制 Material 3 背景
     */
    private static void drawMaterial3Background(Context context, Canvas canvas, int x, int y, 
                                               int width, int height, Material3Theme theme, 
                                               boolean withGradient) {
        Paint bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        float radius = ResUtil.dp2px(CORNER_RADIUS_DP);
        RectF bgRect = new RectF(x, y, x + width, y + height);
        
        int backgroundColor = theme.getBackgroundColor(context);
        
        if (withGradient) {
            // 创建微妙的渐变效果
            int startColor = backgroundColor;
            int endColor = adjustColorBrightness(backgroundColor, 0.95f);
            
            LinearGradient gradient = new LinearGradient(
                x, y, x, y + height,
                startColor, endColor,
                Shader.TileMode.CLAMP
            );
            bgPaint.setShader(gradient);
        } else {
            bgPaint.setColor(backgroundColor);
        }
        
        canvas.drawRoundRect(bgRect, radius, radius, bgPaint);
        
        // 绘制边框
        Paint borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(ResUtil.dp2px(1));
        borderPaint.setColor(theme.getAccentColor(context));
        canvas.drawRoundRect(bgRect, radius, radius, borderPaint);
    }

    /**
     * 绘制二维码内容
     */
    private static void drawQRContent(Context context, Canvas canvas, BitMatrix matrix, 
                                     int offsetX, int offsetY, Material3Theme theme) {
        int width = matrix.getWidth();
        int height = matrix.getHeight();
        
        Paint qrPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        qrPaint.setColor(theme.getForegroundColor(context));
        
        // 计算模块大小
        float moduleSize = 1.0f;
        
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (matrix.get(x, y)) {
                    // 绘制圆角的二维码模块
                    float left = offsetX + x * moduleSize;
                    float top = offsetY + y * moduleSize;
                    float right = left + moduleSize;
                    float bottom = top + moduleSize;
                    
                    RectF moduleRect = new RectF(left, top, right, bottom);
                    float moduleRadius = moduleSize * 0.15f; // 15% 圆角
                    canvas.drawRoundRect(moduleRect, moduleRadius, moduleRadius, qrPaint);
                }
            }
        }
    }

    /**
     * 调整颜色亮度
     */
    private static int adjustColorBrightness(int color, float factor) {
        int alpha = (color >> 24) & 0xFF;
        int red = (int) (((color >> 16) & 0xFF) * factor);
        int green = (int) (((color >> 8) & 0xFF) * factor);
        int blue = (int) ((color & 0xFF) * factor);
        
        red = Math.min(255, Math.max(0, red));
        green = Math.min(255, Math.max(0, green));
        blue = Math.min(255, Math.max(0, blue));
        
        return (alpha << 24) | (red << 16) | (green << 8) | blue;
    }

    /**
     * 生成 Material 3 风格的二维码（公共接口）
     */
    public static Bitmap getMaterial3Bitmap(Context context, String contents, int size, int margin, 
                                          Material3Theme theme) {
        BitMatrix bitMatrix = encodeToBitMatrix(contents, size, margin);
        return bitMatrix != null ? createMaterial3QR(context, bitMatrix, theme) : null;
    }

    /**
     * 生成 Material 3 风格的二维码（带完整配置）
     */
    public static Bitmap getMaterial3Bitmap(Context context, String contents, int size, int margin, 
                                          Material3Theme theme, boolean withElevation, boolean withGradient) {
        BitMatrix bitMatrix = encodeToBitMatrix(contents, size, margin);
        return bitMatrix != null ? createMaterial3QR(context, bitMatrix, theme, withElevation, withGradient) : null;
    }

    /**
     * 自动根据系统主题选择合适的二维码样式
     */
    public static Bitmap getAdaptiveMaterial3Bitmap(Context context, String contents, int size, int margin) {
        // 检测当前是否为深色模式
        boolean isDarkMode = (context.getResources().getConfiguration().uiMode & 
                             android.content.res.Configuration.UI_MODE_NIGHT_MASK) == 
                             android.content.res.Configuration.UI_MODE_NIGHT_YES;
        
        Material3Theme theme = isDarkMode ? Material3Theme.DARK_SURFACE_VARIANT : Material3Theme.LIGHT_SURFACE_VARIANT;
        return getMaterial3Bitmap(context, contents, size, margin, theme);
    }

    // 保留原有方法以保持兼容性（需要 Context）
    public static Bitmap createBitmap(Context context, BitMatrix matrix) {
        return createMaterial3QR(context, matrix, Material3Theme.LIGHT_SURFACE_VARIANT);
    }

    // 原有方法的兼容版本（使用默认颜色）
    public static Bitmap createBitmap(BitMatrix matrix) {
        int width = matrix.getWidth();
        int height = matrix.getHeight();
        int[] pixels = new int[width * height];
        for (int y = 0; y < height; y++) {
            int offset = y * width;
            for (int x = 0; x < width; x++) {
                pixels[offset + x] = matrix.get(x, y) ? 0xFF000000 : 0xFFE3E9E9;
            }
        }
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
        return bitmap;
    }

    private static BitMatrix encodeToBitMatrix(String contents, int size, int margin) {
        try {
            Map<EncodeHintType, Object> hints = new EnumMap<>(EncodeHintType.class);
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
            hints.put(EncodeHintType.MARGIN, margin);
            int pixelSize = ResUtil.dp2px(size);
            return new MultiFormatWriter().encode(contents, BarcodeFormat.QR_CODE, pixelSize, pixelSize, hints);
        } catch (WriterException | IllegalArgumentException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Bitmap getBitmap(String contents, int size, int margin) {
        BitMatrix bitMatrix = encodeToBitMatrix(contents, size, margin);
        return bitMatrix != null ? createBitmap(bitMatrix) : null;
    }
}