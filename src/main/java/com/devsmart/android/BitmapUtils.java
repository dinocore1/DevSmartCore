package com.devsmart.android;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;

import java.io.InputStream;

public class BitmapUtils {

    public static final Paint BitmapPaint = new Paint();

    static {
        BitmapPaint.setFilterBitmap(true);
    }

    public static Bitmap createResized(Bitmap source, float rotateDegrees, int width, int height, Bitmap.Config config) {

        Matrix matrix = new Matrix();
        RectF src = new RectF(0, 0, source.getWidth(), source.getHeight());

        matrix.setRotate(rotateDegrees, src.centerX(), src.centerY());
        matrix.mapRect(src);

        RectF dest = new RectF(0, 0, width, height);

        matrix.setRectToRect(src, dest, Matrix.ScaleToFit.CENTER);
        matrix.preRotate(rotateDegrees, src.centerX(), src.centerY());

        Bitmap destBmp = Bitmap.createBitmap(width, height, config);
        Canvas canvas = new Canvas(destBmp);
        canvas.drawBitmap(source, matrix, BitmapPaint);

        return destBmp;
    }

    public static BitmapFactory.Options createBitmapScaledOptions(int targetResolution, int actualResolution) {
        double scaleFactor = Math.log((double)actualResolution / (double)targetResolution) / Math.log(2);

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = Math.max(1, (int)Math.floor(scaleFactor));

        return options;
    }

}
