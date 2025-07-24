package com.fongmi.android.tv.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.view.View;

public class Blur {

    public static Bitmap apply(Context context, View view) {
        Bitmap bitmap = getBitmap(view);
        if (bitmap == null) return null;
        RenderScript rs = RenderScript.create(context);
        Bitmap blurredBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Allocation input = Allocation.createFromBitmap(rs, bitmap, Allocation.MipmapControl.MIPMAP_FULL, Allocation.USAGE_SHARED);
        Allocation output = Allocation.createTyped(rs, input.getType());
        ScriptIntrinsicBlur script = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs));
        script.setInput(input);
        script.setRadius(25);
        script.forEach(output);
        output.copyTo(blurredBitmap);
        return blurredBitmap;
    }

    private static Bitmap getBitmap(View view) {
        if (view.getWidth() <= 0 || view.getHeight() <= 0) return null;
        Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);
        return bitmap;
    }
} 