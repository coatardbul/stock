package com.coatardbul.stock.common.constants;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 * Note:
 * <p>
 * Date: 2022/2/8
 *
 * @author Su Xiaolei
 */
public class Constant {


    public static ThreadPoolExecutor onceUpLimitThreadPool =
            new ThreadPoolExecutor(
                    Runtime.getRuntime().availableProcessors(),
                    100,
                    30,
                    TimeUnit.MINUTES,
                    new ArrayBlockingQueue<Runnable>(1000));

    /**
     * 每日情绪统计,
     * 时间间隔存入
     */
    public static ThreadPoolExecutor emotionIntervalByDateThreadPool =
            new ThreadPoolExecutor(
                    Runtime.getRuntime().availableProcessors(),
                    100,
                    30,
                    TimeUnit.MINUTES,
                    new ArrayBlockingQueue<Runnable>(1000));


    /**
     * 日期区间情绪统计,统计涨跌幅，
     */
    public static ThreadPoolExecutor emotionByDateRangeThreadPool =
            new ThreadPoolExecutor(
                    Runtime.getRuntime().availableProcessors(),
                    100,
                    30,
                    TimeUnit.MINUTES,
                    new ArrayBlockingQueue<Runnable>(1000));



    /**
     * 异动数据
     */
    public static ThreadPoolExecutor abThreadPool =
            new ThreadPoolExecutor(
                    Runtime.getRuntime().availableProcessors(),
                    100,
                    30,
                    TimeUnit.MINUTES,
                    new ArrayBlockingQueue<Runnable>(1000));


    /**
     * 每日涨停信息，
     */
}
