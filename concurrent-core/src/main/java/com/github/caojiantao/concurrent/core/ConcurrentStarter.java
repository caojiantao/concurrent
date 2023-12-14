package com.github.caojiantao.concurrent.core;

import com.github.caojiantao.concurrent.core.entity.ConcurrentModule;
import com.github.caojiantao.concurrent.core.entity.ConcurrentTask;
import com.github.caojiantao.concurrent.core.entity.UnCheckedRunnable;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
public class ConcurrentStarter {

    private ConcurrentModule module = ConcurrentModule.DEFAULT_MODULE;
    private List<ConcurrentTask> taskList = new ArrayList<>();
    private CountDownLatch latch;

    public static ConcurrentStarter build() {
        return new ConcurrentStarter();
    }

    public ConcurrentStarter build(ConcurrentModule module) {
        this.module = module;
        return this;
    }

    public ConcurrentStarter addTask(String name, UnCheckedRunnable runnable) {
        ConcurrentTask task = new ConcurrentTask(name, runnable);
        taskList.add(task);
        return this;
    }

    public boolean sync(long mills) throws InterruptedException {
        latch = new CountDownLatch(taskList.size());
        ExecutorService executor = module.getExecutor();
        long moduleTime = System.currentTimeMillis();
        for (ConcurrentTask task : taskList) {
            executor.submit(() -> {
                long taskTime = System.currentTimeMillis();
                try {
                    task.getRunnable().run();
                } catch (Exception e) {
                    log.error("moduleName={} taskName={}", module.getName(), task.getName(), e);
                } finally {
                    long cost = System.currentTimeMillis() - taskTime;
                    log.info("act=finishTask moduleName={} taskName={} cost={}", module.getName(), task.getName(), cost);
                    latch.countDown();
                }
            });
        }
        boolean result = latch.await(mills, TimeUnit.MILLISECONDS);
        long cost = System.currentTimeMillis() - moduleTime;
        log.info("act=finishModule moduleName={} cost={} result={}", module.getName(), cost, result);
        return result;
    }
}
