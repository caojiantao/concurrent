package com.caojiantao.concurrent.spring.entity;

import com.caojiantao.concurrent.spring.widget.IModuleTask;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class TaskNode<T> {

    private Integer taskId;
    private String taskName;

    private Integer addr;
    private IModuleTask<T> handler;

    private List<TaskNode<T>> nextList;

    public TaskNode(Integer taskId, String taskName, IModuleTask<T> handler) {
        this.taskId = taskId;
        this.taskName = taskName;
        this.handler = handler;
        this.addr = 0;
        this.nextList = new ArrayList<>();
    }
}
