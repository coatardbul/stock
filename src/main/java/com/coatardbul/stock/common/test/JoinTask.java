package com.coatardbul.stock.common.test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveTask;

public class JoinTask extends RecursiveTask<List<String>> {

    List<String> urlList;


    public JoinTask(List<String> urlList) {
        this.urlList = urlList;
    }

    @Override
    protected List<String> compute() {
        if (urlList.size() < 2) {
            String s = urlList.get(0);
            System.out.println(s);
            List<String> l=new ArrayList<>();
            l.add(s+1);
            return l;
        } else {
            int size = urlList.size();
            JoinTask j1 = new JoinTask(urlList.subList(0, size / 2));
            JoinTask j2 = new JoinTask(urlList.subList(size / 2, urlList.size()));
            invokeAll(j1, j2);
            List<String> l = j1.join();
            l.addAll(j2.join());
            return l;
        }
    }

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        List<String> urlList = new ArrayList<>();
        urlList.add("a");
        urlList.add("b");
        urlList.add("c");
        urlList.add("d");
        urlList.add("e");
        ForkJoinPool forkJoinPool = new ForkJoinPool(8);
        JoinTask jt = new JoinTask(urlList);
        ForkJoinTask<List<String>> submit = forkJoinPool.submit(jt);
        System.out.println(submit.get());

    }
}
