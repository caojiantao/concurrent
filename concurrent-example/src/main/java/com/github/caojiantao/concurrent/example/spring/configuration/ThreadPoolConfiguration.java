package com.github.caojiantao.concurrent.example.spring.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Configuration
public class ThreadPoolConfiguration {

    @Bean
    public ThreadPoolExecutor testPool() {
        int coreSize = 10, maxSize = 10, blockLen = 10;
        return new ThreadPoolExecutor(coreSize, maxSize, 1, TimeUnit.MINUTES,
                new LinkedBlockingQueue<>(blockLen), Executors.defaultThreadFactory(), new ThreadPoolExecutor.AbortPolicy());
    }

    @Bean
    public ThreadPoolExecutor testPool2() {
        int  coreSize = 10, maxSize = 10, blockLen = 10;
        return new ThreadPoolExecutor(coreSize, maxSize, 1, TimeUnit.MINUTES,
                new LinkedBlockingQueue<>(blockLen), Executors.defaultThreadFactory(), new ThreadPoolExecutor.AbortPolicy());
    }
}
