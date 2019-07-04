package cn.xydzjnq.tetris.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.View;

import java.util.Timer;
import java.util.TimerTask;

import cn.xydzjnq.tetris.R;

public class GameOverView extends View {
    //运行状态
    private static final int ANIM_RUN = 0;
    //停止状态
    private static final int ANIM_STOP = 1;
    private Context mContext;
    private Handler mHandler;
    private Bitmap mBitmap;
    private int animCurrentPage = 0;
    //动画帧数
    private int animMaxPage = 4;
    private int animState = ANIM_STOP;
    private int mTimeInterval = 100;
    private Timer mTimer;

    public GameOverView(Context context) {
        this(context, null);
    }

    public GameOverView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public GameOverView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        mContext = context;
        mBitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.icon_wait);
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    default:
                        animCurrentPage = 0;
                        invalidate();
                        break;
                    case ANIM_RUN:
                        if (animCurrentPage < animMaxPage) {
                            animCurrentPage++;
                        } else {
                            animCurrentPage = 1;
                        }
                        invalidate();
                        break;
                    case ANIM_STOP:
                        animCurrentPage = 0;
                        invalidate();
                        break;
                }
            }
        };
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int sideLength = mBitmap.getWidth() / animMaxPage;
        int bitmapHeight = mBitmap.getHeight();
        Rect src = new Rect(sideLength * (animCurrentPage - 1), 0, sideLength * animCurrentPage, sideLength);
        Rect dst = new Rect(0, 0, sideLength, bitmapHeight);
        canvas.drawBitmap(mBitmap, src, dst, null);
    }

    public void start() {
        if (animState == ANIM_RUN) {
            return;
        }
        animState = ANIM_RUN;
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
        mTimer = new Timer();
        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                mHandler.sendEmptyMessage(ANIM_RUN);
            }
        }, 0, mTimeInterval);
    }

    public void stop() {
        if (animState == ANIM_STOP) {
            return;
        }
        animState = ANIM_STOP;
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
        mHandler.sendEmptyMessage(ANIM_STOP);
    }

    public void setTimeInterval(int timeInterval) {
        mTimeInterval = timeInterval;
    }

    public int getAnimState() {
        return animState;
    }

    public boolean isRunning() {
        return animState == ANIM_RUN;
    }
}
