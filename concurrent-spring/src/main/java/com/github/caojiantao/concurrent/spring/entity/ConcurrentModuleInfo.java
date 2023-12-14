package com.github.caojiantao.concurrent.spring.entity;

import com.caojiantao.concurrent.core.util.IpUtils;
import com.github.caojiantao.concurrent.spring.constant.EModuleInfoState;
import com.github.caojiantao.concurrent.spring.constant.ETaskEventType;
import lombok.Data;
import org.springframework.util.CollectionUtils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 并发模块运行时信息，包含当前模块下的所有节点运行时信息
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
     * 模块是否正在运行
     */
    public boolean isRunning() {
        return state == EModuleInfoState.RUNNING;
    }

    /**
     * 该模块是否已结束
     */
    public boolean isCompleted() {
        return counter.get() == 0;
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

    /**
     * 根据指定节点类型，获取该节点运行时信息
     */
    public ConcurrentTaskNodeInfo getNodeInfo(ConcurrentTaskNode<T> node) {
        return taskNodeInfoMap.get(node.getTask().getClass());
    }

    /**
     * 记录任务节点运行时间
     */
    public void recordTaskNodeTime(ConcurrentTaskNode<T> node, ETaskEventType event) {
        LocalDateTime now = LocalDateTime.now();
        ConcurrentTaskNodeInfo nodeInfo = getNodeInfo(node);
        switch (event) {
            case SUBMIT:
                nodeInfo.setSubmitTime(now);
                break;
            case START:
                nodeInfo.setStartTime(now);
                break;
            case END:
                nodeInfo.setEndTime(now);
                break;
        }
    }

    /**
     * 根据当前任务节点，递归初始化当前及后置节点运行时信息
     */
    private void initTaskNodeInfoList(List<ConcurrentTaskNode<T>> nodeList) {
        if (CollectionUtils.isEmpty(nodeList)) {
            return;
        }
        for (ConcurrentTaskNode<T> node : nodeList) {
            if (taskNodeInfoMap.containsKey(node.getTask().getClass())) {
                // 已经初始化过该节点
                continue;
            }
            ConcurrentTaskNodeInfo nodeInfo = ConcurrentTaskNodeInfo.build(node);
            taskNodeInfoMap.put(node.getTask().getClass(), nodeInfo);
            initTaskNodeInfoList(node.getNextList());
        }
    }
}
