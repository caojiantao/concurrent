package com.caojiantao.concurrent.core.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.concurrent.*;

@Data
@AllArgsConstructor
public class ConcurrentModule {

    private String name;
    private ExecutorService executor;

    public static final ConcurrentModule DEFAULT_MODULE;

    static {
        int core = Runtime.getRuntime().availableProcessors();
        int coreSize = 2 * core, maxSize = 2 * coreSize, blockLen = 800;
        ThreadPoolExecutor executor = new ThreadPoolExecutor(coreSize, maxSize, 1, TimeUnit.MINUTES,
                new LinkedBlockingQueue<>(blockLen), Executors.defaultThreadFactory(), new ThreadPoolExecutor.AbortPolicy());
        DEFAULT_MODULE = new ConcurrentModule("默认执行模块", executor);
    }

    public void shutDown() {
        executor.shutdown();
    }
}
