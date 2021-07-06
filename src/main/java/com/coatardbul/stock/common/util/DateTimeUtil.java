package com.coatardbul.stock.common.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * @author: suxiaolei
 * @date: 2019/7/1
 */
public class DateTimeUtil {

    /**
     * 获取当前日期  yyyyMMdd
     *
     * @return
     */
    public static String getCurrentDateFormat() {
        return new SimpleDateFormat("yyyyMMdd").format(new Date());
    }

    /**
     * 获取当前日期  yyyy-MM-dd
     *
     * @return
     */
    public static String getCurrentDateFormatSlash() {
        return new SimpleDateFormat("yyyy-MM-dd").format(new Date());
    }

    /**
     * 获取当前时间  HH:mm:ss
     *
     * @return
     */
    public static String getCurrentTimeFormatSemicolon() {
        return new SimpleDateFormat("HH:mm:ss").format(new Date());
    }

    /**
     * 获取当前时间  HHmmss
     *
     * @return
     */
    public static String getCurrentTimeFormat() {
        return new SimpleDateFormat("HHmmss").format(new Date());
    }

    /**
     * 获取前几天的日期 yyyyMMdd
     *
     * @param day
     * @return
     */
    public static String getBeforeDayDateFormat(int day) {
        day = 0 - day;
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.DATE, day);
        return new SimpleDateFormat("yyyyMMdd").format(calendar.getTime());
    }


    /**
     * 获取当前Date
     *
     * @return
     */
    public static Date getCurrentDate() {
        return new Date();
    }

}
