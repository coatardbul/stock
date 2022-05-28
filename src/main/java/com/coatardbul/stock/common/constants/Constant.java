package com.coatardbul.stock.common.constants;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

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
                    1,
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
                    1,
                    TimeUnit.MINUTES,
                    new ArrayBlockingQueue<Runnable>(1000),
                    new ThreadFactoryBuilder().setNameFormat("date-emotion-%d").build()
            );


    /**
     * 日期区间情绪统计,统计涨跌幅，
     */
    public static ThreadPoolExecutor emotionByDateRangeThreadPool =
            new ThreadPoolExecutor(
                    Runtime.getRuntime().availableProcessors(),
                    100,
                    1,
                    TimeUnit.MINUTES,
                    new ArrayBlockingQueue<Runnable>(1000),
                    new ThreadFactoryBuilder().setNameFormat("date-range-%d").build()

            );

    /**
     * 日期区间情绪统计,统计涨跌幅，
     */
    public static ThreadPoolExecutor fileThreadPool =
            new ThreadPoolExecutor(
                    Runtime.getRuntime().availableProcessors(),
                    100,
                    1,
                    TimeUnit.MINUTES,
                    new ArrayBlockingQueue<Runnable>(1000),
                    new ThreadFactoryBuilder().setNameFormat("file-range-%d").build()

            );


    /**
     * 异动数据
     */
    public static ThreadPoolExecutor abThreadPool =
            new ThreadPoolExecutor(
                    Runtime.getRuntime().availableProcessors(),
                    100,
                    1,
                    TimeUnit.MINUTES,
                    new ArrayBlockingQueue<Runnable>(1000),
                    new ThreadFactoryBuilder().setNameFormat("ab-pool-%d").build()

            );

    /**
     * 分钟情绪统计,统计涨跌幅，
     */
    public static ThreadPoolExecutor emotionByMinuterThreadPool =
            new ThreadPoolExecutor(
                    Math.max(Runtime.getRuntime().availableProcessors(),12),
                    100,
                    1,
                    TimeUnit.MINUTES,
                    new ArrayBlockingQueue<Runnable>(5000),
                    new ThreadFactoryBuilder().setNameFormat("minute-pool-%d").build()
            );
}
