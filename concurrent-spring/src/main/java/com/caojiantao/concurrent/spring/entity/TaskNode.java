package com.caojiantao.concurrent.spring.entity;

import com.caojiantao.concurrent.spring.IHandler;
import lombok.Data;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

@Data
public class TaskNode<P, Q extends IHandler<P>> {

    private String taskName;

    private int preTasks;
    private Q handler;

    private List<TaskNode> nextNodeList;

    public TaskNode(String taskName, Q handler) {
        this.taskName = taskName;
        this.handler = handler;
        this.nextNodeList = new ArrayList<>();
    }

    public String toString() {
        String fmt = "TaskNode(taskName={0} handler={2} next={3})";
        return MessageFormat.format(fmt, taskName, handler.getClass().getSimpleName(), nextNodeList);
    }
}
