package com.caojiantao.concurrent.core.executor;

import com.caojiantao.concurrent.core.entity.Runnable;
import com.caojiantao.concurrent.core.entity.Task;

import java.util.LinkedList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

public class ExecutorStarter {

    private ExecutorModule executorModule;
    private LinkedList<Task> taskList = new LinkedList<>();
    private CountDownLatch latch;

    public static ExecutorStarter build(String name, ExecutorService executor) {
        ExecutorStarter starter = new ExecutorStarter();
        starter.executorModule = new ExecutorModule(name, executor);
        return starter;
    }

    public ExecutorStarter addTask(String name, Runnable runnable) {
        Task task = new Task(name, runnable);
        taskList.addLast(task);
        return this;
    }

    public void sync(long mills) throws InterruptedException {
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
        boolean complete = latch.await(mills, TimeUnit.MILLISECONDS);
        System.out.println(complete);
    }
}
