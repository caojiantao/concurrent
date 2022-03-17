package com.caojiantao.concurrent.core.executor;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.concurrent.*;

@Data
@AllArgsConstructor
public class ExecutorModule {

    private String moduleName;
    private ExecutorService executor;

    public static ExecutorModule defaultModule() {
        int core = Runtime.getRuntime().availableProcessors();
        int coreSize = 2 * core, maxSize = 2 * coreSize, blockLen = 200;
        ThreadPoolExecutor executor = new ThreadPoolExecutor(coreSize, maxSize, 1, TimeUnit.MINUTES,
                new LinkedBlockingQueue<>(blockLen), Executors.defaultThreadFactory(), new ThreadPoolExecutor.AbortPolicy());
        return new ExecutorModule("默认执行模块", executor);
    }
}
