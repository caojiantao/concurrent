package com.github.caojiantao.concurrent.spring;

import com.github.caojiantao.concurrent.spring.constant.EModuleInfoState;
import com.github.caojiantao.concurrent.spring.constant.ETaskEventType;
import com.github.caojiantao.concurrent.spring.entity.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

@Slf4j
public class ConcurrentStarter<T> {

    /**
     * 模块上下文
     */
    private T context;

    /**
     * 并发模块
     */
    private ConcurrentModule<T> module;

    /**
     * 模块执行信息
     */
    private ConcurrentModuleInfo<T> moduleInfo;

    /**
     * 并发模块操控者
     */
    private IConcurrentExecutor<T> executor;

    /**
     * 并发入口！！并发入口！！并发入口！！
     */
    public static <T> ConcurrentModuleInfo<T> run(T context) {
        ConcurrentStarter<T> starter = buildStarter(context);
        ConcurrentModule<T> module = starter.module;
        log.info("[{}] 开始并发", module.getName());
        starter.run0();
        ConcurrentModuleInfo<T> moduleInfo = starter.moduleInfo;
        log.info("[{}] 结束并发，耗时 {}", module.getName(), moduleInfo.getCost());
        return moduleInfo;
    }

    /**
     * 构建一个并发 starter，用来控制并发流程
     *
     * @param context 并发上下文
     * @param <T>     上下文类型
     * @return starter
     */
    private static <T> ConcurrentStarter<T> buildStarter(T context) {
        ConcurrentModule<T> module = ConcurrentManager.getModule(context.getClass());
        Assert.notNull(module, "找不到并发执行模块");
        ConcurrentStarter<T> starter = new ConcurrentStarter<>();
        starter.context = context;
        starter.module = module;
        starter.moduleInfo = ConcurrentModuleInfo.build(module);
        // todo 暂时匿名内部类，没有想到更优雅的控制器
        starter.executor = new IConcurrentExecutor<T>() {
            @Override
            public void interruptModule() {
                starter.unPark(true);
            }
        };
        return starter;
    }

    private void run0() {
        List<ConcurrentTaskNode<T>> rootNodeList = module.getRootNodeList();
        rootNodeList.forEach(this::submitTaskNode);
        park();
        LocalDateTime end = LocalDateTime.now();
        moduleInfo.setEnd(end);
        if (moduleInfo.getCost() > module.getTimeout()) {
            // 超时
            moduleInfo.setState(EModuleInfoState.TIMEOUT);
        }
    }

    private void submitTaskNode(ConcurrentTaskNode<T> taskNode) {
        log.info("[{}][{}] 提交任务", module.getName(), taskNode.getName());
        moduleInfo.recordTaskNodeTime(taskNode, ETaskEventType.SUBMIT);
        ThreadPoolExecutor threadPool = taskNode.getThreadPool();
        if (threadPool == null) {
            threadPool = module.getThreadPool();
        }
        threadPool.submit(() -> {
            log.info("[{}][{}] 执行任务", module.getName(), taskNode.getName());
            beforeTask(taskNode);
            IConcurrentTask<T> nodeHandler = taskNode.getTask();
            try {
                nodeHandler.run(context);
            } catch (Exception e) {
                log.error("[{}][{}] 出现异常", module.getName(), taskNode.getName(), e);
                nodeHandler.onError(context, e);
            } finally {
                afterTask(taskNode);
            }
        });
    }

    private void beforeTask(ConcurrentTaskNode<T> taskNode) {
        log.info("[{}][{}] 开始执行", module.getName(), taskNode.getName());
        moduleInfo.recordTaskNodeTime(taskNode, ETaskEventType.START);
        ConcurrentManager.setExecutor(executor);
    }

    private void afterTask(ConcurrentTaskNode<T> taskNode) {
        moduleInfo.recordTaskNodeTime(taskNode, ETaskEventType.END);
        ConcurrentTaskNodeInfo nodeInfo = moduleInfo.getNodeInfo(taskNode);
        log.info("[{}][{}] 执行结束，等待时长 {}，执行耗时 {}", module.getName(), taskNode.getName(), nodeInfo.getWaitTime(), nodeInfo.getCost());
        ConcurrentManager.clearExecutor();
        moduleInfo.getCounter().decrementAndGet();
        if (!moduleInfo.isRunning()) {
            // 并发模块已终止（超时、中断）
            return;
        }
        if (moduleInfo.isCompleted()) {
            // 所有任务都执行完毕
            unPark(false);
            return;
        }
        // 提交下游任务
        submitNextTaskNode(taskNode);
    }

    private void submitNextTaskNode(ConcurrentTaskNode<T> taskNode) {
        List<ConcurrentTaskNode<T>> nextList = taskNode.getNextList();
        for (ConcurrentTaskNode<T> child : nextList) {
            // 子任务的依赖计数器减 1
            ConcurrentTaskNodeInfo taskNodeInfo = moduleInfo.getNodeInfo(child);
            int depends = taskNodeInfo.getDepends().decrementAndGet();
            if (depends == 0) {
                // 当子任务没有依赖任务或者依赖的任务都执行完毕才进行提交
                this.submitTaskNode(child);
            }
        }
    }

    private void park() {
        // 基于 LockSupport 实现多线程同步
        LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(module.getTimeout()));
    }

    /**
     * 唤醒主线程
     *
     * @param interrupt 是否终端
     */
    private void unPark(boolean interrupt) {
        EModuleInfoState state = interrupt ? EModuleInfoState.INTERRUPT : EModuleInfoState.SUCCESS;
        moduleInfo.setState(state);
        LockSupport.unpark(moduleInfo.getMainThread());
    }
}
