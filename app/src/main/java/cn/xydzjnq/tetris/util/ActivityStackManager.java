package cn.xydzjnq.tetris.util;

import android.app.Activity;
import android.content.Context;

import java.util.Iterator;
import java.util.Stack;

import cn.xydzjnq.tetris.BaseApplication;

public class ActivityStackManager {
    private static volatile ActivityStackManager instance = null;
    private Stack<Activity> mStack = null;

    private ActivityStackManager() {
        mStack = new Stack<>();
    }

    public static ActivityStackManager getInstance() {
        if (instance == null) {
            synchronized (ActivityStackManager.class) {
                if (instance == null)
                    instance = new ActivityStackManager();
            }
        }
        return instance;
    }

    /**
     * 获取栈的信息
     */
    public String getStackInfo() {
        StringBuilder sb = new StringBuilder();
        for (Activity temp : mStack) {
            if (temp != null)
                sb.append(temp.toString()).append("\n");
        }
        return sb.toString();
    }

    /**
     * 将activity加入到栈中
     *
     * @param activity 需要加入到栈中的activity
     */
    public void addActivity(Activity activity) {
        mStack.push(activity);
    }

    /**
     * 删除栈中activity
     */
    public void removeActivity(Activity activity) {
        mStack.remove(activity);
    }

    /**
     * @return 栈顶的activity
     */
    public Activity getActivity() {
        if (!mStack.isEmpty())
            return mStack.peek();
        return null;
    }

    /**
     * 关闭并删除掉最上面一个的activity
     */
    public void finishActivity() {
        if (!mStack.isEmpty()) {
            Activity temp = mStack.pop();
            if (temp != null)
                temp.finish();
            return;
        }
    }

    /***
     * 关闭并删除指定 activity
     */
    public void finishActivity(Activity activity) {
        if (mStack.isEmpty()) {
            return;
        }
        try {
            mStack.remove(activity);
        } catch (Exception e) {
        } finally {
            if (activity != null)
                activity.finish();
        }
    }

    /**
     * 删除并关闭栈中该class对应的所有的该activity
     */
    public void finishAllActivity(Class<?> clazz) {
        Iterator<Activity> iterator = mStack.iterator();
        while (iterator.hasNext()) {
            Activity activity = iterator.next();
            if (activity != null && activity.getClass().equals(clazz)) {
                //注意应该通过iterator操作stack，要不然回报ConcurrentModificationException
                iterator.remove();
                activity.finish();
            }
        }
    }

    /**
     * 删除并关闭栈中该class对应的第一个该activity,从栈顶开始
     */
    public void finishLastActivity(Class<?> clazz) {
        Activity activity = null;
        Iterator<Activity> iterator = mStack.iterator();
        while (iterator.hasNext()) {
            Activity temp = iterator.next();
            if (temp != null && temp.getClass().equals(clazz))
                activity = temp;
        }
        if (activity != null)
            finishActivity(activity);
    }

    /**
     * 删除栈上该activity之上的所有activity
     */
    public void finishAfterActivity(Activity activity) {
        if (activity != null && mStack.search(activity) == -1) {
            return;
        }
        while (mStack.peek() != null) {
            Activity temp = mStack.pop();
            if (temp != null && temp.equals(activity)) {
                mStack.push(temp);
                break;
            }
            if (temp != null)
                temp.finish();
        }
    }

    /**
     * 删除栈上该class之上的所有activity
     */
    public void finishAfterActivity(Class<?> clazz) {
        boolean flag = true;
        Activity activity = null;
        Iterator<Activity> iterator = mStack.iterator();
        while (iterator.hasNext()) {
            activity = iterator.next();
            if (activity != null && activity.getClass().equals(clazz)) {
                flag = false;
                break;
            }
        }
        if (flag) {
            return;
        }
        finishAfterActivity(activity);
    }

    /**
     * 弹出关闭所有activity并关闭应用所有进程
     */
    public void finishAllActivityAndClose() {
        while (mStack.size() > 0) {
            Activity temp = mStack.pop();
            if (temp != null)
                temp.finish();
        }
        try {
            android.app.ActivityManager activityManager = (android.app.ActivityManager)
                    BaseApplication.getInstance().getSystemService(Context.ACTIVITY_SERVICE);
            activityManager.killBackgroundProcesses(BaseApplication.getInstance().getPackageName());
        } catch (SecurityException e) {
        }
        System.exit(0);
    }

    /**
     * 弹出关闭所有activity并保留应用后台进程
     */
    public void finishAllActivityWithoutClose() {
        while (mStack.size() > 0) {
            Activity temp = mStack.pop();
            if (temp != null)
                temp.finish();
        }
        System.exit(0);
    }
}
