package com.coatardbul.stock.common.test;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveTask;

public class CountAddTask extends RecursiveTask<Integer> {
    private static final int THRESHOLD = 2;
    private int start;
    private int end;

    public CountAddTask(int start, int end) {
        this.start = start;
        this.end = end;
    }

    @Override
    protected Integer compute() {
        int sum = 0;
        if (end - start <= THRESHOLD) {
            for (int i = start; i <= end; i++) {
                sum += i;
            }
        } else {
            int middle = (start + end) / 2;
            CountAddTask c1 = new CountAddTask(start, middle);
            CountAddTask c2 = new CountAddTask(middle, end);
            c1.fork();
            c2.fork();

            Integer j1 = c1.join();
            Integer j2 = c2.join();

            sum = j1 + j2;
        }
        return sum;
    }

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        long start = System.currentTimeMillis();
        ForkJoinPool f=new ForkJoinPool();
        CountAddTask countAddTask = new CountAddTask(1, 10000);
        ForkJoinTask<Integer> submit = f.submit(countAddTask);
        System.out.println(submit.get());
        long end = System.currentTimeMillis();
        System.out.println(end-start);



    }

}
