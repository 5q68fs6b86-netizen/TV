package com.fongmi.android.tv.utils;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

import java.util.EnumMap;
import java.util.Map;

public class QRCode {

    // 二维码黑色块的颜色
    private static final int BLACK = 0xFF000000;
    // 定义你想要的纯色背景
    private static final int CUSTOM_BACKGROUND_COLOR = 0xFFE3E9E9; // #FFE3E9E9

    /**
     * 创建带有更精细圆角效果的二维码（高级版本）
     *
     * @param matrix ZXing BitMatrix
     * @return 美化的二维码 Bitmap
     */
    private static Bitmap createBeautifulBitmap(BitMatrix matrix) {
        int width = matrix.getWidth();
        int height = matrix.getHeight();
        
        // 创建更大的画布以获得更好的抗锯齿效果
        int scale = 2;
        int scaledWidth = width * scale;
        int scaledHeight = height * scale;
        
        Bitmap scaledBitmap = Bitmap.createBitmap(scaledWidth, scaledHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(scaledBitmap);
        
        // 设置抗锯齿
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        
        // 绘制背景
        paint.setColor(CUSTOM_BACKGROUND_COLOR);
        float cornerRadius = Math.min(scaledWidth, scaledHeight) * 0.08f;
        RectF backgroundRect = new RectF(0, 0, scaledWidth, scaledHeight);
        canvas.drawRoundRect(backgroundRect, cornerRadius, cornerRadius, paint);
        
        // 绘制二维码模块
        paint.setColor(BLACK);
        float moduleRadius = scale * 0.3f; // 模块圆角
        
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (matrix.get(x, y)) {
                    float left = x * scale;
                    float top = y * scale;
                    float right = left + scale;
                    float bottom = top + scale;
                    
                    RectF moduleRect = new RectF(left, top, right, bottom);
                    canvas.drawRoundRect(moduleRect, moduleRadius, moduleRadius, paint);
                }
            }
        }
        
        // 缩放回原始大小
        return Bitmap.createScaledBitmap(scaledBitmap, width, height, true);
    }

    private static BitMatrix encodeToBitMatrix(String contents, int size, int margin) {
        try {
            Map<EncodeHintType, Object> hints = new EnumMap<>(EncodeHintType.class);
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
            hints.put(EncodeHintType.MARGIN, margin);
            int pixelSize = ResUtil.dp2px(size);
            return new MultiFormatWriter().encode(contents, BarcodeFormat.QR_CODE, pixelSize, pixelSize, hints);
        } catch (WriterException e) {
            e.printStackTrace();
            return null;
        } catch (IllegalArgumentException e) {
             e.printStackTrace();
             return null;
        }
    }

    /**
     * 生成带有指定纯色背景和圆角效果的二维码 Bitmap。
     *
     * @param contents 内容
     * @param size     二维码尺寸 (dp)
     * @param margin   边距 (模块数)
     * @return 美化的二维码 Bitmap 或 null
     */
    public static Bitmap getBitmap(String contents, int size, int margin) {
        BitMatrix bitMatrix = encodeToBitMatrix(contents, size, margin);
        if (bitMatrix != null) {
            return createBeautifulBitmap(bitMatrix);
        } else {
            return null;
        }
    }
}
