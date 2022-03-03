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

@Slf4j
public class TaskNodeManager {

    private static LinkedMultiValueMap<Class, TaskNode> taskNodeMap = new LinkedMultiValueMap<>();

    public static List<TaskNode> getTaskNodeList(Class handlerType) {
        return taskNodeMap.get(handlerType);
    }

    public static void initTaskNodeList(ApplicationContext context) {
        log.info("开始初始化并发任务调用关系...");
        Map<String, IHandler> handlerMap = context.getBeansOfType(IHandler.class);
        MultiValueMap<Class, TaskNode> nextMap = new LinkedMultiValueMap<>();
        List<TaskNode> rootNodeList = new ArrayList<>();
        Map<Class<?>, TaskNode<?, ?>> nodeMap = new HashMap<>();
        handlerMap.forEach((beanName, handler) -> {
            ExecutorTask annotation = handler.getClass().getAnnotation(ExecutorTask.class);
            Assert.notNull(annotation, "IHandler 实现类必须添加 @Handler 注解");
            nodeMap.putIfAbsent(handler.getClass(), new TaskNode(annotation.name(), handler));
            TaskNode node = nodeMap.get(handler.getClass());
            Class[] depends = annotation.depends();
            if (depends.length == 0) {
                rootNodeList.add(node);
            } else {
                for (Class depend : depends) {
                    nextMap.add(depend, node);
                }
            }
        });
        log.info("全局任务节点数量({}) 起始任务节点数量({})", handlerMap.size(), rootNodeList.size());
        if (CollectionUtils.isEmpty(rootNodeList)) {
            return;
        }
        log.info("开始递归初始化驱动节点链路关系...");
        rootNodeList.forEach(node -> initNext(node, nextMap, nodeMap));
    }

    private static void initNext(TaskNode node, MultiValueMap<Class, TaskNode> nextMap, Map<Class<?>, TaskNode<?, ?>> nodeMap) {
        Class<?>[] interfaces = node.getHandler().getClass().getInterfaces();
        Class<?> nodeInterface = null;
        for (Class<?> clazz : interfaces) {
            if (IHandler.class.isAssignableFrom(clazz)) {
                nodeInterface = clazz;
            }
        }
        Assert.notNull(nodeInterface, "没有实现 IHandler 接口");
        boolean hasInit = Optional.of(nodeInterface)
                .map(taskNodeMap::get)
                .map(list -> list.contains(node))
                .orElse(false);
        if (hasInit) {
            return;
        }
        log.info("新增任务节点 {}", node);
        taskNodeMap.add(nodeInterface, node);
        List<TaskNode> nextList = nextMap.get(node.getHandler().getClass());
        if (!CollectionUtils.isEmpty(nextList)) {
            nextList.forEach(item -> {
                item.setPreTasks(item.getPreTasks() + 1);
                initNext(item, nextMap, nodeMap);
            });
            node.setNextNodeList(nextList);
        }
    }
}
