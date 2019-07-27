package cn.xydzjnq.tetris.util;

import java.text.SimpleDateFormat;
import java.util.Date;

public class TimeUtils {
    public static final SimpleDateFormat DATE_FORMAT_WHOLE = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    public static final SimpleDateFormat DATE_FORMAT_DATE = new SimpleDateFormat("yyyy-MM-dd");
    public static final SimpleDateFormat DATE_FORMAT_MINIUTE = new SimpleDateFormat("HH:mm");

    public static String getTime(long timeInMillis, SimpleDateFormat dateFormat) {
        return dateFormat.format(new Date(timeInMillis));
    }

    public static String getDefaultTime(long timeInMills) {
        String today = getTime(getCurrentTimeInLong(), DATE_FORMAT_DATE);
        String timeDate = getTime(timeInMills, DATE_FORMAT_DATE);
        if (today.equals(timeDate)) {
            return getTime(timeInMills, DATE_FORMAT_MINIUTE);
        } else {
            return timeDate;
        }
    }

    public static long getCurrentTimeInLong() {
        return System.currentTimeMillis();
    }
}
