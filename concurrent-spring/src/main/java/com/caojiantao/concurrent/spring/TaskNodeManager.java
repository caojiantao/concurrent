package com.caojiantao.concurrent.spring;

import com.caojiantao.concurrent.spring.annotation.ExecutorTask;
import com.caojiantao.concurrent.spring.entity.TaskNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class TaskNodeManager {

    private static LinkedMultiValueMap<Class, TaskNode> moduleTaskMap = new LinkedMultiValueMap<>();

    public static List<TaskNode> getTaskNodeList(Class handlerType) {
        return moduleTaskMap.get(handlerType);
    }

    public static void initTaskNodeList(ApplicationContext context) {
        log.info("开始初始化并发任务调用关系...");
        Map<String, IHandler> handlerMap = context.getBeansOfType(IHandler.class);
        MultiValueMap<Class, TaskNode> nextMap = new LinkedMultiValueMap<>();
        List<TaskNode> rootNodeList = new ArrayList<>();
        // class -> handler
        Map<Class<?>, TaskNode<?, ?>> nodeMap = new HashMap<>();
        AtomicInteger taskIdCounter = new AtomicInteger();
        handlerMap.values().forEach(handler -> {
            ExecutorTask annotation = handler.getClass().getAnnotation(ExecutorTask.class);
            Assert.notNull(annotation, "IHandler 实现类必须添加 @ExecutorTask 注解");
            nodeMap.put(handler.getClass(), new TaskNode(taskIdCounter.incrementAndGet(), annotation.name(), handler));
            TaskNode node = nodeMap.get(handler.getClass());
            Class[] depends = annotation.depends();
            if (depends.length == 0) {
                // 根节点
                rootNodeList.add(node);
            } else {
                // 子节点列表
                for (Class depend : depends) {
                    nextMap.add(depend, node);
                }
            }
        });
        log.info("全局任务节点数量({}) 起始任务节点数量({})", handlerMap.size(), rootNodeList.size());
        rootNodeList.forEach(node -> initNext(node, nextMap));
    }

    private static void initNext(TaskNode node, MultiValueMap<Class, TaskNode> nextMap) {
        // 定位该task的实现类class，做聚合moduleTaskMap
        Class<?>[] interfaces = node.getHandler().getClass().getInterfaces();
        Class<?> nodeInterface = null;
        for (Class<?> clazz : interfaces) {
            if (IHandler.class.isAssignableFrom(clazz)) {
                nodeInterface = clazz;
            }
        }
        Assert.notNull(nodeInterface, "没有实现 IHandler 接口");
        for (List<TaskNode> list : moduleTaskMap.values()) {
            for (TaskNode item : list) {
                // 如果该节点已经处理，则跳过
                if (item.getTaskId().equals(node.getTaskId())) {
                    return;
                }
            }
        }
        log.info("新增任务节点[{}]", node.getTaskName());
        moduleTaskMap.add(nodeInterface, node);
        List<TaskNode> nextList = nextMap.get(node.getHandler().getClass());
        if (!CollectionUtils.isEmpty(nextList)) {
            nextList.forEach(item -> {
                item.setPreTask(item.getPreTask() + 1);
                initNext(item, nextMap);
            });
            node.setNextNodeList(nextList);
        }
    }
}
