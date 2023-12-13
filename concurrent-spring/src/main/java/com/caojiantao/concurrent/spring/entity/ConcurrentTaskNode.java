package com.caojiantao.concurrent.spring.entity;

import com.caojiantao.concurrent.spring.IConcurrentTask;
import com.caojiantao.concurrent.spring.constant.ENodeState;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 并发任务节点
 *
 * @param <T> 并发模块上下文
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConcurrentTaskNode<T> {

    /**
     * 任务名
     */
    private String name;
    /**
     * 该任务关联线程池，默认使用所在模块线程池
     */
    private ThreadPoolExecutor threadPool;
    /**
     * 实际任务处理类
     */
    private IConcurrentTask<T> task;
    /**
     * 依赖前置任务数
     */
    private Integer depends;
    /**
     * 后继任务节点列表
     */
    private List<ConcurrentTaskNode<T>> nextList;
    /**
     * 当前节点状态
     */
    private ENodeState state;
}
