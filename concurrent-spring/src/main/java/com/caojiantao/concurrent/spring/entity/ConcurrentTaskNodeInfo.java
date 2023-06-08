package com.caojiantao.concurrent.spring.entity;

import com.caojiantao.concurrent.core.util.IpUtils;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicInteger;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConcurrentTaskNodeInfo {

    private String ip;

    private LocalDateTime submitTime;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private AtomicInteger depends;

    public static ConcurrentTaskNodeInfo build(ConcurrentTaskNode taskNode) {
        ConcurrentTaskNodeInfo nodeInfo = new ConcurrentTaskNodeInfo();
        String ip = IpUtils.getHostIp();
        nodeInfo.setIp(ip);
        AtomicInteger depends = new AtomicInteger(taskNode.getDepends());
        nodeInfo.setDepends(depends);
        return nodeInfo;
    }

    public Long getWaitTime() {
        return Duration.between(submitTime, startTime).toMillis();
    }

    public Long getCost() {
        return Duration.between(startTime, endTime).toMillis();
    }
}
