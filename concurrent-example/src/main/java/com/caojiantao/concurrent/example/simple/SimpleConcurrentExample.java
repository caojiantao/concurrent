package com.caojiantao.concurrent.example.simple;

import com.caojiantao.concurrent.core.ConcurrentStarter;

import java.util.concurrent.ThreadLocalRandom;

public class SimpleConcurrentExample {

    public static void main(String[] args) throws InterruptedException {
        ConcurrentStarter.build()
                .addTask("获取基本信息", () -> {
                    ThreadLocalRandom random = ThreadLocalRandom.current();
                    Thread.sleep(random.nextLong(1000));
                }).addTask("获取额外信息", () -> {
                    ThreadLocalRandom random = ThreadLocalRandom.current();
                    Thread.sleep(random.nextLong(1000));
                }).addTask("获取运营推广", () -> {
                    ThreadLocalRandom random = ThreadLocalRandom.current();
                    Thread.sleep(random.nextLong(1000));
                }).sync(800);
    }
}
