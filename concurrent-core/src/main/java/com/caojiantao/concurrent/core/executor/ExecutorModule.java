package com.caojiantao.concurrent.core.executor;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.concurrent.ExecutorService;

@Data
@AllArgsConstructor
public class ExecutorModule {

    private String moduleName;
    private ExecutorService executor;
}
