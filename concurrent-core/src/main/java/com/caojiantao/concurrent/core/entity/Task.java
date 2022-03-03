package com.caojiantao.concurrent.core.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Task {

    private String taskName;
    private Runnable runnable;
}
