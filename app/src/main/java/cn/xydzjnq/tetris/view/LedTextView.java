package cn.xydzjnq.tetris.view;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

import java.io.File;

public class LedTextView extends TextView {
    private static final String FONTS_FOLDER = "fonts";
    private static final String FONT_DIGITAL_7 = FONTS_FOLDER + File.separator
            + "digital-7.ttf";

    public LedTextView(Context context) {
        super(context);
        init(context);
    }

    public LedTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public LedTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public LedTextView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    private void init(Context context) {
        AssetManager assetManager = context.getAssets();
        Typeface font = Typeface.createFromAsset(assetManager, FONT_DIGITAL_7);
        setTypeface(font);
    }
}
