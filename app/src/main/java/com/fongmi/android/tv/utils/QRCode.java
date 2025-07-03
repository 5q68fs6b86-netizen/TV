package com.fongmi.android.tv.utils;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import java.util.EnumMap;
import java.util.Map;

public class QRCode {

    // --- 可配置的样式常量 ---
    @ColorInt
    private static final int DATA_COLOR = Color.BLACK; // 数据点颜色
    @ColorInt
    private static final int BACKGROUND_COLOR = Color.WHITE; // 背景颜色
    private static final float DATA_DOT_SCALE = 0.9f; // 数据点缩放比例，制造间距感
    private static final float CORNER_RADIUS_SCALE = 0.25f; // 数据点圆角半径比例

    /**
     * 主调用方法，和原来保持一致。
     * 生成一个带有默认美化效果（圆角、间距）的二维码。
     *
     * @param contents 内容字符串
     * @param size     二维码尺寸 (dp)
     * @param margin   边距 (二维码模块数)
     * @return 美化后的二维码 Bitmap，或在失败时返回 null
     */
    public static Bitmap getBitmap(String contents, int size, int margin) {
        return generate(contents, size, margin, null);
    }

    /**
     * 新增的重载方法，用于生成带 Logo 的二维码。
     *
     * @param contents 内容字符串
     * @param size     二维码尺寸 (dp)
     * @param margin   边距 (二维码模块数)
     * @param logo     要嵌入的 Logo Bitmap
     * @return 带 Logo 的美化二维码 Bitmap，或在失败时返回 null
     */
    public static Bitmap getBitmapWithLogo(String contents, int size, int margin, @Nullable Bitmap logo) {
        return generate(contents, size, margin, logo);
    }


    /**
     * 内部核心生成逻辑。
     */
    @Nullable
    private static Bitmap generate(String contents, int size, int margin, @Nullable Bitmap logo) {
        try {
            int pixelSize = ResUtil.dp2px(size);

            // 1. 编码成 BitMatrix
            Map<EncodeHintType, Object> hints = new EnumMap<>(EncodeHintType.class);
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
            hints.put(EncodeHintType.MARGIN, margin);
            // 如果有 logo，使用最高容错率
            hints.put(EncodeHintType.ERROR_CORRECTION, logo != null ? ErrorCorrectionLevel.H : ErrorCorrectionLevel.Q);

            BitMatrix matrix = new MultiFormatWriter().encode(contents, BarcodeFormat.QR_CODE, pixelSize, pixelSize, hints);

            // 2. 通过 Canvas 绘制美化后的 Bitmap
            return drawOnCanvas(matrix, logo);

        } catch (WriterException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 使用 Canvas 绘制美化效果。
     */
    private static Bitmap drawOnCanvas(BitMatrix matrix, @Nullable Bitmap logo) {
        int width = matrix.getWidth();
        int height = matrix.getHeight();
        float moduleSize = (float) width / matrix.getWidth();

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        // 绘制背景
        canvas.drawColor(BACKGROUND_COLOR);

        // 绘制数据点
        Paint dataPaint = new Paint();
        dataPaint.setAntiAlias(true);
        dataPaint.setColor(DATA_COLOR);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                // 跳过定位点和背景点
                if (isFinderPattern(x, y, width) || !matrix.get(x, y)) {
                    continue;
                }
                
                // 绘制带缩放和圆角的数据点
                float scaledSize = moduleSize * DATA_DOT_SCALE;
                float left = x * moduleSize + (moduleSize - scaledSize) / 2;
                float top = y * moduleSize + (moduleSize - scaledSize) / 2;
                RectF rect = new RectF(left, top, left + scaledSize, top + scaledSize);
                float cornerRadius = scaledSize * CORNER_RADIUS_SCALE;
                canvas.drawRoundRect(rect, cornerRadius, cornerRadius, dataPaint);
            }
        }
        
        // 重新绘制定位点，确保它们是标准的方块
        drawFinderPatterns(canvas, width, moduleSize, dataPaint);

        // 如果有 logo，嵌入它
        if (logo != null) {
            addLogo(canvas, width, height, logo);
        }

        return bitmap;
    }

    /**
     * 检查一个点是否属于定位图案（三个大方块）。
     */
    private static boolean isFinderPattern(int x, int y, int matrixWidth) {
        int finderSize = 7;
        // 左上角
        if (x < finderSize && y < finderSize) return true;
        // 右上角
        if (x >= matrixWidth - finderSize && y < finderSize) return true;
        // 左下角
        if (x < finderSize && y >= matrixWidth - finderSize) return true;

        return false;
    }

    /**
     * 准确地绘制三个定位图案，覆盖掉可能被圆角化的部分。
     */
    private static void drawFinderPatterns(Canvas canvas, int matrixWidth, float moduleSize, Paint paint) {
        int finderSize = 7;
        
        // 左上角
        canvas.drawRect(0, 0, finderSize * moduleSize, finderSize * moduleSize, paint);
        canvas.drawRect((matrixWidth - finderSize) * moduleSize, 0, matrixWidth * moduleSize, finderSize * moduleSize, paint);
        canvas.drawRect(0, (matrixWidth - finderSize) * moduleSize, finderSize * moduleSize, matrixWidth * moduleSize, paint);
        // 如果要更精细，可以只画黑块，但对于默认黑白二维码，直接画实心矩形更简单高效
    }


    /**
     * 在二维码中心添加 Logo。
     */
    private static void addLogo(Canvas canvas, int width, int height, @NonNull Bitmap logo) {
        float logoScale = 0.2f; // Logo 占二维码总宽度的比例
        float logoSize = width * logoScale;
        float logoX = (width - logoSize) / 2;
        float logoY = (height - logoSize) / 2;

        // 绘制 Logo 背景 (提供一个"安全区")
        Paint bgPaint = new Paint();
        bgPaint.setColor(BACKGROUND_COLOR);
        float bgMargin = 4f; // 背景比 logo 大一点
        float bgSize = logoSize + bgMargin * 2;
        float bgX = (width - bgSize) / 2;
        float bgY = (height - bgSize) / 2;
        float bgCornerRadius = 8f;
        canvas.drawRoundRect(new RectF(bgX, bgY, bgX + bgSize, bgY + bgSize), bgCornerRadius, bgCornerRadius, bgPaint);

        // 绘制 Logo
        canvas.drawBitmap(logo, null, new RectF(logoX, logoY, logoX + logoSize, logoY + logoSize), null);
    }
}