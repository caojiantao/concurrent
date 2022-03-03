package com.caojiantao.concurrent.core;

import com.caojiantao.concurrent.core.executor.ExecutorStarter;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;

public class Main {

    public static void main(String[] args) throws InterruptedException {
        ExecutorStarter.build("商品详情页", Executors.newFixedThreadPool(10))
                .addTask("获取基本信息", () -> {
                    ThreadLocalRandom random = ThreadLocalRandom.current();
                    Thread.sleep(random.nextLong(1000));
                    System.out.println("基本信息为xxx");
                }).addTask("获取额外信息", () -> {
                    ThreadLocalRandom random = ThreadLocalRandom.current();
                    Thread.sleep(random.nextLong(1000));
                    System.out.println("额外信息为xxx");
                }).addTask("获取运营推广", () -> {
                    ThreadLocalRandom random = ThreadLocalRandom.current();
                    Thread.sleep(random.nextLong(1000));
                    System.out.println("推广信息为xxx");
                }).sync(800);
    }
}
