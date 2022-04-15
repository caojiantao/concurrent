package com.caojiantao.concurrent.spring.entity;

import com.caojiantao.concurrent.spring.constant.ETaskState;
import com.caojiantao.concurrent.spring.widget.IHandler;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class TaskNode<T> {

    private Integer taskId;
    private String taskName;
    private ETaskState taskState;

    private Integer addr;
    private IHandler<T> handler;

    private List<TaskNode<T>> nextList;

    public TaskNode(Integer taskId, String taskName, IHandler<T> handler) {
        this.taskId = taskId;
        this.taskName = taskName;
        this.taskState = ETaskState.RUNNING;
        this.handler = handler;
        this.addr = 0;
        this.nextList = new ArrayList<>();
    }
}
