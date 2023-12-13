package com.caojiantao.concurrent.spring.entity;

import com.caojiantao.concurrent.core.util.IpUtils;
import com.caojiantao.concurrent.spring.constant.EModuleInfoState;
import com.caojiantao.concurrent.spring.constant.ETaskEventType;
import lombok.Data;
import org.springframework.util.CollectionUtils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.LockSupport;

/**
 * 并发模块运行时信息
 *
 * @param <T> 模块上下文
 */
@Data
public class ConcurrentModuleInfo<T> {

    /**
     * 主线程
     */
    private Thread mainThread;

    /**
     * 主线程唤醒额外标记，避免重复 unpark 导致未知异常
     */
    private AtomicBoolean unparked = new AtomicBoolean(false);

    /**
     * 当前运行机器 IP
     */
    private String ip;

    /**
     * 并发模块执行开始时间
     */
    private LocalDateTime start;

    /**
     * 并发模块执行结束时间
     */
    private LocalDateTime end;

    /**
     * 当前运行时并发模块计数器，归 0 说明执行完了
     */
    private AtomicInteger counter = new AtomicInteger();

    private Map<Class<?>, ConcurrentTaskNodeInfo> taskNodeInfoMap = new ConcurrentHashMap<>();

    private volatile EModuleInfoState state = EModuleInfoState.RUNNING;

    /**
     * 该模块是否已结束
     */
    public boolean isCompleted() {
        return counter.get() == 0;
    }

    public void setUnparked() {
        unparked.set(true);
    }

    public void unpark() {
        if (!unparked.getAndSet(true)) {
            LockSupport.unpark(mainThread);
        }
    }

    /**
     * 获取模块执行耗时(ms)
     */
    public Long getCost() {
        return Duration.between(start, end).toMillis();
    }

    /**
     * 根据指定的并发模块，构造一个运行时模块
     */
    public static <T> ConcurrentModuleInfo<T> build(ConcurrentModule<T> module) {
        ConcurrentModuleInfo<T> moduleInfo = new ConcurrentModuleInfo<>();
        LocalDateTime start = LocalDateTime.now();
        String ip = IpUtils.getHostIp();
        AtomicInteger counter = new AtomicInteger(module.getNodeSize());
        moduleInfo.setMainThread(Thread.currentThread());
        moduleInfo.setStart(start);
        moduleInfo.setIp(ip);
        moduleInfo.setCounter(counter);
        // 初始化各任务节点执行信息
        moduleInfo.initTaskNodeInfoList(module.getRootNodeList());
        return moduleInfo;
    }

    public ConcurrentTaskNodeInfo getNodeInfo(ConcurrentTaskNode node) {
        return taskNodeInfoMap.get(node.getTask().getClass());
    }

    public void recordTime(ConcurrentTaskNode node, ETaskEventType event) {
        LocalDateTime now = LocalDateTime.now();
        ConcurrentTaskNodeInfo nodeInfo = getNodeInfo(node);
        switch (event) {
            case SUBMIT:
                nodeInfo.setSubmitTime(now);
                return;
            case START:
                nodeInfo.setStartTime(now);
                return;
            case END:
                nodeInfo.setEndTime(now);
                return;
        }
    }

    private void initTaskNodeInfoList(List<ConcurrentTaskNode<T>> nodeList) {
        if (CollectionUtils.isEmpty(nodeList)) {
            return;
        }
        for (ConcurrentTaskNode<T> node : nodeList) {
            if (taskNodeInfoMap.containsKey(node.getTask().getClass())) {
                continue;
            }
            ConcurrentTaskNodeInfo nodeInfo = ConcurrentTaskNodeInfo.build(node);
            taskNodeInfoMap.put(node.getTask().getClass(), nodeInfo);
            initTaskNodeInfoList(node.getNextList());
        }
    }
}
