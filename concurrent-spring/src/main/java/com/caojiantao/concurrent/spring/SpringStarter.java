package com.caojiantao.concurrent.spring;

import com.caojiantao.concurrent.core.executor.ExecutorModule;
import com.caojiantao.concurrent.spring.constant.EModuleState;
import com.caojiantao.concurrent.spring.entity.TaskNode;
import com.caojiantao.concurrent.spring.widget.IExecCtrl;
import com.caojiantao.concurrent.spring.widget.impl.ExecCtrl;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.LockSupport;

@Slf4j
public class SpringStarter<T> {

    private ExecutorModule module = ExecutorModule.defaultModule();
    private T context;

    private final AtomicInteger undoTasks = new AtomicInteger();
    private final Map<Integer, AtomicInteger> addrMap = new HashMap<>();
    private final Thread mainThread = Thread.currentThread();
    private IExecCtrl ctrl = new ExecCtrl();

    public static <T> SpringStarter<T> build(T context) {
        SpringStarter<T> starter = new SpringStarter<>();
        starter.context = context;
        return starter;
    }

    public SpringStarter<T> module(ExecutorModule module) {
        this.module = module;
        return this;
    }

    public void sync(long timeout) {
        long mill = System.currentTimeMillis();
        List<TaskNode> taskNodeList = TaskNodeManager.getTaskNodeList(context.getClass());
        undoTasks.set(taskNodeList.size());
        List<TaskNode> headList = new ArrayList<>();
        for (TaskNode task : taskNodeList) {
            addrMap.put(task.getTaskId(), new AtomicInteger(task.getAddr()));
            if (task.getAddr() == 0) {
                headList.add(task);
            }
        }
        log.info("[concurrent-spring] [{}] 并发模块提交，任务节点总数({})，根任务节点数({})", module.getModuleName(), undoTasks.get(), headList.size());
        headList.forEach(this::submitTask);
        // 基于 LockSupport 实现多线程同步
        LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(timeout));
        long cost = System.currentTimeMillis() - mill;
        log.info("[concurrent-spring] [{}] 并发模块完成，耗时 {} ms", module.getModuleName(), cost);
    }

    private void submitTask(TaskNode taskNode) {
        int adder = addrMap.get(taskNode.getTaskId()).get();
        log.info("[concurrent-spring] [{}] [{}] 任务开始提交，全局待完成({})，前置待完成({})", module.getModuleName(), taskNode.getTaskName(), undoTasks.get(), adder);
        if (adder > 0) {
            // 还有未完成的前置任务，继续等待
            return;
        }
        module.getExecutor().submit(() -> {
            long mill = System.currentTimeMillis();
            try {
                log.info("[concurrent-spring] [{}] [{}] 任务开始执行，待完成任务数量({})", module.getModuleName(), taskNode.getTaskName(), undoTasks.get());
                taskNode.getHandler().doHandler(ctrl, context);
            } catch (Exception e) {
                log.error("[concurrent-spring] [{}] [{}] 任务出现异常", module.getModuleName(), taskNode.getTaskName(), e);
                taskNode.getHandler().onError(ctrl, context, e);
            } finally {
                long cost = System.currentTimeMillis() - mill;
                log.info("[concurrent-spring] [{}] [{}] 任务执行完毕，开始驱动后续任务执行，耗时 {} ms", module.getModuleName(), taskNode.getTaskName(), cost);
                finishTask(taskNode);
            }
        });
    }

    private void finishTask(TaskNode taskNode) {
        EModuleState moduleState = ctrl.getState();
        if (moduleState != EModuleState.NORMAL) {
            log.info("[concurrent-spring] [{}] [{}] 执行中断，开始唤醒主线程...", module.getModuleName(), taskNode.getTaskName());
            LockSupport.unpark(mainThread);
            return;
        }
        int undo = undoTasks.decrementAndGet();
        List<TaskNode> nextList = taskNode.getNextList();
        if (undo == 0) {
            log.info("[concurrent-spring] [{}] [{}] 所有任务执行完毕，开始唤醒主线程...", module.getModuleName(), taskNode.getTaskName());
            LockSupport.unpark(mainThread);
            return;
        }
        for (TaskNode child : nextList) {
            addrMap.get(child.getTaskId()).decrementAndGet();
            this.submitTask(child);
        }
    }
}
