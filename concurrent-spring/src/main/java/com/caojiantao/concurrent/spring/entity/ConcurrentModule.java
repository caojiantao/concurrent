package com.caojiantao.concurrent.spring.entity;

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
public class ConcurrentModule<T> {

    private String name;
    private Long timeout;
    private ThreadPoolExecutor threadPool;

    private List<ConcurrentTaskNode<T>> rootNodeList;
    private Integer nodeSize;
}
