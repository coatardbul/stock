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
    /**
     * 当天方差，标准差线程池统计
     */
    public static ThreadPoolExecutor dateJobThreadPool =
            new ThreadPoolExecutor(
                    Runtime.getRuntime().availableProcessors(),
                    100,
                    30,
                    TimeUnit.MINUTES,
                    new ArrayBlockingQueue<Runnable>(1000));


    public static ThreadPoolExecutor dateTimeJobThreadPool =
            new ThreadPoolExecutor(
                    Runtime.getRuntime().availableProcessors(),
                    100,
                    30,
                    TimeUnit.MINUTES,
                    new ArrayBlockingQueue<Runnable>(1000));

    /**
     * 每日情绪统计
     */
    public static ThreadPoolExecutor emotionJobThreadPool =
            new ThreadPoolExecutor(
                    Runtime.getRuntime().availableProcessors(),
                    100,
                    30,
                    TimeUnit.MINUTES,
                    new ArrayBlockingQueue<Runnable>(1000));
}
