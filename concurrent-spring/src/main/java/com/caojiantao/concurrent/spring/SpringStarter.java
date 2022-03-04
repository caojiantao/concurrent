package com.caojiantao.concurrent.spring;

import com.caojiantao.concurrent.core.executor.ExecutorModule;
import com.caojiantao.concurrent.spring.entity.TaskNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.LockSupport;
import java.util.stream.Collectors;

@Slf4j
public class SpringStarter<P, Q extends IHandler<P>> {

    private ExecutorModule executorModule;
    private Class<Q> handlerType;
    private P moduleContext;

    private final AtomicInteger undoTasks = new AtomicInteger();
    private final Map<TaskNode, AtomicInteger> preTasksMap = new HashMap<>();
    private final Thread mainThread = Thread.currentThread();
    private volatile EStatus status = EStatus.RUNNING;

    public static <P, Q extends IHandler<P>> SpringStarter<P, Q> build(Class<Q> clazz) {
        SpringStarter<P, Q> starter = new SpringStarter<>();
        starter.handlerType = clazz;
        return starter;
    }

    public SpringStarter<P, Q> executorModule(ExecutorModule executorModule) {
        this.executorModule = executorModule;
        return this;
    }

    public SpringStarter<P, Q> moduleContext(P moduleContext) {
        this.moduleContext = moduleContext;
        return this;
    }

    public void setStatus(EStatus status) {
        this.status = status;
    }

    public EStatus sync(long timeout) {
        List<TaskNode> taskNodeList = TaskNodeManager.getTaskNodeList(handlerType);
        undoTasks.set(taskNodeList.size());
        List<TaskNode> headList = taskNodeList.stream().filter(item -> item.getPreTasks() == 0).collect(Collectors.toList());
        Assert.notEmpty(headList, handlerType.getSimpleName() + " 没有找到任务起始结点");
        taskNodeList.forEach(item -> preTasksMap.put(item, new AtomicInteger(item.getPreTasks())));
        log.info("[{}]并发模块 任务总数({})，起始任务({}) ", executorModule.getModuleName(), undoTasks.get(), headList.size());
        headList.forEach(this::submitTask);
        LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(timeout));
        return status;
    }

    private void submitTask(TaskNode taskNode) {
        int pre = preTasksMap.get(taskNode).get();
        log.info("[{}]开始提交 全局待完成({}) 前置待完成({})", taskNode.getTaskName(), undoTasks.get(), pre);
        if (pre == 0) {
            executorModule.getExecutor().submit(() -> {
                try {
                    log.info("[{}]开始执行 待完成任务数量({})", taskNode.getTaskName(), undoTasks.get());
                    taskNode.getHandler().doHandler(this, moduleContext);
                } catch (Exception e) {
                    log.error("[{}]出现异常", taskNode.getTaskName(), e);
                    taskNode.getHandler().onError(this, e);
                } finally {
                    finishTask(taskNode);
                }
            });
        }
    }

    private void finishTask(TaskNode taskNode) {
        if (status != EStatus.RUNNING) {
            log.info("[{}]执行中断({})，开始唤醒主线程...", executorModule.getModuleName(), status);
            LockSupport.unpark(mainThread);
            return;
        }

        int undo = undoTasks.decrementAndGet();
        List<TaskNode> nextNodeList = taskNode.getNextNodeList();
        log.info("[{}]执行完毕，开始驱动后续任务执行，后续任务数量({})...", taskNode.getTaskName(), nextNodeList.size());
        if (undo == 0) {
            log.info("所有任务执行完毕，开始唤醒主线程...");
            LockSupport.unpark(mainThread);
            return;
        }
        if (!CollectionUtils.isEmpty(nextNodeList)) {
            nextNodeList.forEach(item -> {
                preTasksMap.get(item).decrementAndGet();
                this.submitTask(item);
            });
        }
    }
}
