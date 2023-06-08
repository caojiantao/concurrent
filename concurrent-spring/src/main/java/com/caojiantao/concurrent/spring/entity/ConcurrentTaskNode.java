package com.caojiantao.concurrent.spring.entity;

import com.caojiantao.concurrent.spring.constant.ENodeState;
import com.caojiantao.concurrent.spring.IConcurrentTask;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConcurrentTaskNode<T> {

    private String name;
    private ThreadPoolExecutor threadPool;
    private IConcurrentTask<T> task;

    private Integer depends;
    private List<ConcurrentTaskNode<T>> nextList;

    private ENodeState state;
}
