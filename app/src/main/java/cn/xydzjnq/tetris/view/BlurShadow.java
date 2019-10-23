package cn.xydzjnq.tetris.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.widget.ImageView;

public class BlurShadow {
    private static final float DOWNSCALE_FACTOR = 0.25f;
    private volatile static BlurShadow blurShadow;
    private RenderScript renderScript;

    private BlurShadow() {
    }

    public static BlurShadow getInstance() {
        if (blurShadow == null) {
            synchronized (BlurShadow.class) {
                if (blurShadow == null) {
                    blurShadow = new BlurShadow();
                }
            }
        }
        return blurShadow;
    }

    public void init(Context context) {
        if (renderScript == null) {
            renderScript = RenderScript.create(context);
        }
    }

    public Bitmap blur(ImageView view, int width, int height, float radius) {
        Bitmap src = getBitmapForView(view, DOWNSCALE_FACTOR, width, height);
        Allocation input = Allocation.createFromBitmap(renderScript, src);
        Allocation output = Allocation.createTyped(renderScript, input.getType());
        ScriptIntrinsicBlur script = ScriptIntrinsicBlur.create(renderScript, Element.U8_4(renderScript));
        script.setRadius(radius);
        script.setInput(input);
        script.forEach(output);
        output.copyTo(src);
        return src;
    }

    private Bitmap getBitmapForView(ImageView view, float downscaleFactor, int width, int height) {
        Bitmap bitmap = Bitmap.createBitmap(
                (int) (width * downscaleFactor),
                (int) (height * downscaleFactor),
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Matrix matrix = new Matrix();
        matrix.preScale(downscaleFactor, downscaleFactor);
        canvas.setMatrix(matrix);
        view.draw(canvas);
        return bitmap;
    }
}
