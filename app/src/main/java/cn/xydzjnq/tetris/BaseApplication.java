package cn.xydzjnq.tetris;

import android.app.Application;
import android.content.Context;

import java.lang.ref.WeakReference;

import cn.xydzjnq.tetris.util.ActivityStackManager;

public class BaseApplication extends Application {
    /**
     * 用来保存当前该Application的context
     */
    private static Context instance;
    /**
     * 用来保存最新打开页面的context
     */
    private volatile static WeakReference<Context> instanceRef = null;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
    }

    /**
     * 该函数用来返回一个context，一般情况下为当前activity的context，如果为空，
     * 就会调用{@linkplain ActivityStackManager#getActivity()}方法去获取栈顶context,
     * 但是如果activity没有调用 {@link #setInstanceRef(Context)}方法去设置context,
     * 就会使用整个Application的context，相当于{@link #getApplicationContext()},
     * 不推荐使用该方法，特别是耗时任务，因为会导致页面销毁时，任务无法回收，导致内存泄露和
     * 其他异常
     *
     * @return context上下文，如果返回Null检测manifest文件是否设置了application的name
     */
    public static Context getInstance() {
        if (instanceRef == null || instanceRef.get() == null) {
            synchronized (BaseApplication.class) {
                if (instanceRef == null || instanceRef.get() == null) {
                    Context context = ActivityStackManager.getInstance().getActivity();
                    if (context != null)
                        instanceRef = new WeakReference<>(context);
                    else {
                        instanceRef = new WeakReference<>(instance);
                    }
                }
            }
        }
        return instanceRef.get();
    }

    /**
     * 将{@link #instanceRef}设置为最新页面的context
     *
     * @param context 最新页面的context
     */
    public static void setInstanceRef(Context context) {
        instanceRef = new WeakReference<>(context);
    }
}
