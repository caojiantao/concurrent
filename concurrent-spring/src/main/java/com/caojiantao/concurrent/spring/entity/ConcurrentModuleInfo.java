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
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.LockSupport;

@Data
public class ConcurrentModuleInfo {

    private final Thread mainThread = Thread.currentThread();

    /**
     * 主线程唤醒额外标记，避免重复 unpark 导致未知异常
     */
    private final AtomicBoolean unparked = new AtomicBoolean(false);

    private String ip;

    private LocalDateTime start;

    private LocalDateTime end;

    private AtomicInteger counter = new AtomicInteger();

    private Map<Class<?>, ConcurrentTaskNodeInfo> taskNodeInfoMap = new ConcurrentHashMap<>();

    private volatile EModuleInfoState state = EModuleInfoState.RUNNING;

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

    public Long getCost() {
        return Duration.between(start, end).toMillis();
    }

    public static ConcurrentModuleInfo build(ConcurrentModule module) {
        ConcurrentModuleInfo moduleInfo = new ConcurrentModuleInfo();
        LocalDateTime start = LocalDateTime.now();
        moduleInfo.setStart(start);
        String ip = IpUtils.getHostIp();
        moduleInfo.setIp(ip);
        AtomicInteger counter = new AtomicInteger(module.getNodeSize());
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

    private void initTaskNodeInfoList(List<ConcurrentTaskNode> nodeList) {
        if (CollectionUtils.isEmpty(nodeList)) {
            return;
        }
        for (ConcurrentTaskNode node : nodeList) {
            if (taskNodeInfoMap.containsKey(node.getTask().getClass())) {
                continue;
            }
            ConcurrentTaskNodeInfo nodeInfo = ConcurrentTaskNodeInfo.build(node);
            taskNodeInfoMap.put(node.getTask().getClass(), nodeInfo);
            initTaskNodeInfoList(node.getNextList());
        }
    }
}
