package com.caojiantao.concurrent.spring.entity;

import com.caojiantao.concurrent.spring.IHandler;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class TaskNode<P, Q extends IHandler<P>> {

    private Integer taskId;
    private String taskName;

    private Integer preTask = 0;
    private Q handler;

    private List<TaskNode<P, Q>> nextNodeList;

    public TaskNode(Integer taskId, String taskName, Q handler) {
        this.taskId = taskId;
        this.taskName = taskName;
        this.handler = handler;
        this.nextNodeList = new ArrayList<>();
    }
}
