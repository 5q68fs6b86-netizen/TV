package com.fongmi.android.tv.utils;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
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
     * 将 BitMatrix 转换为 Bitmap，并添加圆角效果。
     * 修改：将白色部分设为您指定的背景色，并美化二维码样式。
     *
     * @param matrix ZXing BitMatrix
     * @return 代表二维码的 Bitmap，白色部分为 CUSTOM_BACKGROUND_COLOR，带有圆角效果
     */
    public static Bitmap createBitmap(BitMatrix matrix) {
        int width = matrix.getWidth();
        int height = matrix.getHeight();
        int[] pixels = new int[width * height];
        
        // 创建基础二维码像素数据
        for (int y = 0; y < height; y++) {
            int offset = y * width;
            for (int x = 0; x < width; x++) {
                // true -> 黑色, false -> 自定义背景色
                pixels[offset + x] = matrix.get(x, y) ? BLACK : CUSTOM_BACKGROUND_COLOR;
            }
        }
        
        // 创建基础 Bitmap
        Bitmap baseBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        baseBitmap.setPixels(pixels, 0, width, 0, 0, width, height);
        
        // 应用美化效果
        return createRoundedQRCode(baseBitmap);
    }

    /**
     * 创建带有圆角和美化效果的二维码
     *
     * @param originalBitmap 原始二维码 Bitmap
     * @return 美化后的二维码 Bitmap
     */
    private static Bitmap createRoundedQRCode(Bitmap originalBitmap) {
        int width = originalBitmap.getWidth();
        int height = originalBitmap.getHeight();
        
        // 创建输出 Bitmap
        Bitmap output = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        
        // 计算圆角半径 (可以根据需要调整)
        float cornerRadius = Math.min(width, height) * 0.08f; // 8% 的圆角
        
        // 创建圆角路径
        Path roundedPath = new Path();
        RectF rect = new RectF(0, 0, width, height);
        roundedPath.addRoundRect(rect, cornerRadius, cornerRadius, Path.Direction.CW);
        
        // 裁剪画布为圆角形状
        canvas.clipPath(roundedPath);
        
        // 绘制原始二维码
        canvas.drawBitmap(originalBitmap, 0, 0, null);
        
        // 美化二维码块 - 添加轻微的圆角效果到黑色块
        return addRoundedPixels(output, cornerRadius);
    }

    /**
     * 为二维码的黑色像素块添加轻微的圆角效果
     *
     * @param bitmap 原始二维码 Bitmap
     * @param globalCornerRadius 全局圆角半径
     * @return 美化后的 Bitmap
     */
    private static Bitmap addRoundedPixels(Bitmap bitmap, float globalCornerRadius) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        
        Bitmap result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(result);
        
        // 首先绘制背景
        Paint backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        backgroundPaint.setColor(CUSTOM_BACKGROUND_COLOR);
        RectF backgroundRect = new RectF(0, 0, width, height);
        canvas.drawRoundRect(backgroundRect, globalCornerRadius, globalCornerRadius, backgroundPaint);
        
        // 分析像素并绘制圆角的黑色块
        Paint blackPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        blackPaint.setColor(BLACK);
        
        // 计算每个像素块的大小
        int moduleSize = findModuleSize(bitmap);
        float pixelRadius = moduleSize * 0.15f; // 15% 的像素圆角
        
        for (int y = 0; y < height; y += moduleSize) {
            for (int x = 0; x < width; x += moduleSize) {
                if (x < width && y < height) {
                    int pixel = bitmap.getPixel(x, y);
                    if (pixel == BLACK) {
                        // 绘制圆角的黑色块
                        RectF pixelRect = new RectF(x, y, 
                            Math.min(x + moduleSize, width), 
                            Math.min(y + moduleSize, height));
                        canvas.drawRoundRect(pixelRect, pixelRadius, pixelRadius, blackPaint);
                    }
                }
            }
        }
        
        return result;
    }

    /**
     * 估算二维码模块的大小
     *
     * @param bitmap 二维码 Bitmap
     * @return 模块大小（像素）
     */
    private static int findModuleSize(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        
        // 简单估算：假设是标准二维码，通常有21x21到177x177个模块
        // 这里用一个简单的方法来估算模块大小
        int estimatedModules = 25; // 假设大约25x25个模块
        return Math.max(1, Math.min(width, height) / estimatedModules);
    }

    /**
     * 创建带有更精细圆角效果的二维码（高级版本）
     *
     * @param matrix ZXing BitMatrix
     * @return 美化的二维码 Bitmap
     */
    public static Bitmap createBeautifulBitmap(BitMatrix matrix) {
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

    // encodeToBitMatrix 方法保持不变
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
            // 调用美化后的 createBitmap
            return createBitmap(bitMatrix);
        } else {
            return null;
        }
    }

    /**
     * 生成高质量美化二维码 Bitmap。
     *
     * @param contents 内容
     * @param size     二维码尺寸 (dp)
     * @param margin   边距 (模块数)
     * @return 高质量美化的二维码 Bitmap 或 null
     */
    public static Bitmap getBeautifulBitmap(String contents, int size, int margin) {
        BitMatrix bitMatrix = encodeToBitMatrix(contents, size, margin);
        if (bitMatrix != null) {
            // 调用高级美化版本
            return createBeautifulBitmap(bitMatrix);
        } else {
            return null;
        }
    }
}
