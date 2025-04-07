package com.fongmi.android.tv.utils;

import android.graphics.Bitmap;
import android.graphics.Color; // 引入 Color 以便解析颜色字符串 (虽然这里直接用 int)

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
     * 将 BitMatrix 转换为 Bitmap。
     * 修改：将白色部分设为您指定的背景色。
     *
     * @param matrix ZXing BitMatrix
     * @return 代表二维码的 Bitmap，白色部分为 CUSTOM_BACKGROUND_COLOR
     */
    public static Bitmap createBitmap(BitMatrix matrix) {
        int width = matrix.getWidth();
        int height = matrix.getHeight();
        int[] pixels = new int[width * height];
        for (int y = 0; y < height; y++) {
            int offset = y * width;
            for (int x = 0; x < width; x++) {
                // true -> 黑色, false -> 自定义背景色
                pixels[offset + x] = matrix.get(x, y) ? BLACK : CUSTOM_BACKGROUND_COLOR;
            }
        }
        // 创建 Bitmap (不需要 Alpha 通道也可以，但 ARGB_8888 最常用)
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
        return bitmap;
    }

     // encodeToBitMatrix 方法保持不变 (来自上一个回答)
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
     * 生成带有指定纯色背景 (#FFE3E9E9) 的二维码 Bitmap。
     *
     * @param contents 内容
     * @param size     二维码尺寸 (dp)
     * @param margin   边距 (模块数)
     * @return 二维码 Bitmap 或 null
     */
    public static Bitmap getBitmap(String contents, int size, int margin) {
        BitMatrix bitMatrix = encodeToBitMatrix(contents, size, margin);
        if (bitMatrix != null) {
            // 调用修改后的 createBitmap，白色部分将是 CUSTOM_BACKGROUND_COLOR
            return createBitmap(bitMatrix);
        } else {
            return null;
        }
    }

    // getBitmapWithBackground 方法可以保留，用于支持图片背景，
    // 但它内部调用的 createBitmap 现在也会产生 CUSTOM_BACKGROUND_COLOR
    // 而不是透明色，这可能不是图片背景想要的。
    // 如果同时需要图片背景和纯色背景，推荐使用方法二。

    /* (getBitmapWithBackground 方法可以注释掉或删除，如果不再需要图片背景功能)
    public static Bitmap getBitmapWithBackground(String contents, int qrCodeSizeDp, int qrCodeMargin, Bitmap backgroundBitmap) {
       // ... (这个方法如果保留，其行为会受 createBitmap 修改的影响)
    }
    */
}