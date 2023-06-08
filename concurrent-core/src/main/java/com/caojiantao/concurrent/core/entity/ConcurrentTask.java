package com.caojiantao.concurrent.core.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ConcurrentTask {

    private String name;
    private UnCheckedRunnable runnable;
}
