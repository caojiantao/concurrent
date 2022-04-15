package com.caojiantao.concurrent.spring;

import com.caojiantao.concurrent.core.executor.ExecutorModule;
import com.caojiantao.concurrent.spring.constant.EModuleState;
import com.caojiantao.concurrent.spring.constant.ETaskState;
import com.caojiantao.concurrent.spring.entity.TaskNode;
import com.caojiantao.concurrent.spring.widget.IExecutorController;
import com.caojiantao.concurrent.spring.widget.impl.ExecutorController;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.LockSupport;
import java.util.stream.Collectors;

@Slf4j
public class SpringStarter<T> {

    private ExecutorModule module;
    private T context;

    private final AtomicInteger undoTasks = new AtomicInteger();
    private final Map<Integer, AtomicInteger> addrMap = new HashMap<>();
    private final Thread mainThread = Thread.currentThread();
    private IExecutorController controller;

    public static <T> SpringStarter<T> build(T context) {
        SpringStarter<T> starter = new SpringStarter<>();
        starter.context = context;
        starter.controller = new ExecutorController();
        return starter;
    }

    public SpringStarter<T> module(ExecutorModule module) {
        this.module = module;
        return this;
    }

    public EModuleState sync(long timeout) {
        // 获取 context 关联的任务节点集合
        List<TaskNode> taskNodeList = TaskNodeManager.getTaskNodeList(context.getClass());
        // 设置本次需要完成的任务节点数
        undoTasks.set(taskNodeList.size());
        // 获取根任务节点集合，准备开始并发
        List<TaskNode> headList = taskNodeList.stream().filter(item -> item.getAddr() == 0).collect(Collectors.toList());
        // 保存每个任务节点的前置节点数
        taskNodeList.forEach(item -> addrMap.put(item.getTaskId(), new AtomicInteger(item.getAddr())));
        log.info("【{}】并发模块提交，任务节点总数({})，根任务节点数({})", module.getModuleName(), undoTasks.get(), headList.size());
        headList.forEach(this::submitTask);
        // 基于 LockSupport 实现多线程同步
        LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(timeout));
        log.info("【{}】并发模块完成，任务节点总数({})，根任务节点数({})", module.getModuleName(), undoTasks.get(), headList.size());
        return controller.getState();
    }

    private void submitTask(TaskNode taskNode) {
        int adder = addrMap.get(taskNode.getTaskId()).get();
        log.info("【{}】任务开始提交，全局待完成 {}，前置待完成 {}", taskNode.getTaskName(), undoTasks.get(), adder);
        if (adder > 0) {
            // 还有未完成的前置任务，继续等待
            return;
        }
        module.getExecutor().submit(() -> {
            try {
                log.info("【{}】任务开始执行，待完成任务数量 {}", taskNode.getTaskName(), undoTasks.get());
                if (Objects.equals(ETaskState.FALLBACK, taskNode.getTaskState())) {
                    taskNode.getHandler().fallback(controller, context);
                } else {
                    taskNode.getHandler().doHandler(controller, context);
                }
            } catch (Exception e) {
                log.error("【{}】任务出现异常", taskNode.getTaskName(), e);
                taskNode.getHandler().onError(controller, e);
            } finally {
                finishTask(taskNode);
            }
        });
    }

    private void finishTask(TaskNode taskNode) {
        EModuleState moduleState = controller.getState();
        if (moduleState != EModuleState.RUNNING) {
            log.info("【{}】执行中断 {}，开始唤醒主线程...", module.getModuleName(), moduleState);
            LockSupport.unpark(mainThread);
            return;
        }

        int undo = undoTasks.decrementAndGet();
        List<TaskNode> nextList = taskNode.getNextList();
        log.info("【{}】任务执行完毕，开始驱动后续任务执行，后续任务数量 {}...", taskNode.getTaskName(), nextList.size());
        if (undo == 0) {
            log.info("所有任务执行完毕，开始唤醒主线程...");
            controller.complete();
            LockSupport.unpark(mainThread);
            return;
        }
        for (TaskNode child : nextList) {
            addrMap.get(child.getTaskId()).decrementAndGet();
            this.submitTask(child);
        }
    }
}
