package com.caojiantao.concurrent.core.executor;

import com.caojiantao.concurrent.core.entity.Runnable;
import com.caojiantao.concurrent.core.entity.Task;

import java.util.LinkedList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

public class ExecutorStarter {

    private ExecutorModule executorModule = ExecutorModule.defaultModule();
    private LinkedList<Task> taskList = new LinkedList<>();
    private CountDownLatch latch;

    public static ExecutorStarter build() {
        return new ExecutorStarter();
    }

    public ExecutorStarter build(ExecutorModule executorModule) {
        this.executorModule = executorModule;
        return this;
    }

    public ExecutorStarter addTask(String name, Runnable runnable) {
        Task task = new Task(name, runnable);
        taskList.addLast(task);
        return this;
    }

    public boolean sync(long mills) throws InterruptedException {
        latch = new CountDownLatch(taskList.size());
        ExecutorService executor = executorModule.getExecutor();
        for (Task task : taskList) {
            executor.submit(() -> {
                try {
                    task.getRunnable().run();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    latch.countDown();
                }
            });
        }
        return latch.await(mills, TimeUnit.MILLISECONDS);
    }
}
