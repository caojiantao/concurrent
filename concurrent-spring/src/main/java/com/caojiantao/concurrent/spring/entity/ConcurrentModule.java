package com.caojiantao.concurrent.spring.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 并发模块
 *
 * @param <T> 模块上下文
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConcurrentModule<T> {

    /**
     * 模块名
     */
    private String name;
    /**
     * 模块超时
     */
    private Long timeout;
    /**
     * 模块默认线程池
     */
    private ThreadPoolExecutor threadPool;
    /**
     * 模块根节点列表
     */
    private List<ConcurrentTaskNode<T>> rootNodeList;
    /**
     * 模块总节点数
     */
    private Integer nodeSize;
}
