package com.caojiantao.concurrent.spring;

import com.caojiantao.concurrent.spring.annotation.ExecutorTask;
import com.caojiantao.concurrent.spring.entity.TaskNode;
import com.caojiantao.concurrent.spring.widget.IModuleTask;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.core.ResolvableType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class TaskNodeManager {

    /**
     * context -> taskList
     */
    private static LinkedMultiValueMap<Class, TaskNode> moduleContextMap = new LinkedMultiValueMap<>();

    /**
     * 获取 context 对应的所有任务节点
     */
    public static <T> List<TaskNode> getTaskNodeList(Class<T> contextClass) {
        return moduleContextMap.get(contextClass);
    }

    /**
     * taskId 生成器
     */
    private static AtomicInteger taskIdCounter = new AtomicInteger();

    /**
     * 构建任务节点依赖关系图，并保存 context 对应的任务节点集合
     */
    public static void initTaskNodeList(ApplicationContext context) {
        Map<String, Object> handlerMap = context.getBeansWithAnnotation(ExecutorTask.class);
        log.info("[concurrent-spring] 初始化全局任务节点({})", handlerMap.size());
        MultiValueMap<Class, TaskNode> nextMap = new LinkedMultiValueMap<>();
        List<TaskNode> rootNodeList = new ArrayList<>();
        for (Object handler : handlerMap.values()) {
            ExecutorTask annotation = handler.getClass().getAnnotation(ExecutorTask.class);
            int taskId = getNextTaskId();
            TaskNode node = new TaskNode(taskId, annotation.name(), (IModuleTask) handler);
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
        }
        rootNodeList.forEach(node -> initNext(node, nextMap));
    }

    private static void initNext(TaskNode node, MultiValueMap<Class, TaskNode> nextMap) {
        if (hasInitNext(node)) {
            return;
        }
        // 获取该节点的 context 类型
        Class<?> contextClass = getContextClass(node);
        moduleContextMap.add(contextClass, node);
        List<TaskNode> nextList = nextMap.getOrDefault(node.getHandler().getClass(), new ArrayList<>());
        for (TaskNode child : nextList) {
            // 初始 child 一次，需要增加 preTask
            child.setAddr(child.getAddr() + 1);
            // 递归初始化子节点
            initNext(child, nextMap);
        }
        node.setNextList(nextList);
    }

    /**
     * 获取 Handler 实现类 Context 类型
     */
    private static Class<?> getContextClass(TaskNode node) {
        ResolvableType[] interfaces = ResolvableType.forInstance(node.getHandler()).getInterfaces();
        ResolvableType handleType = null;
        for (ResolvableType resolvableType : interfaces) {
            if (Objects.equals(resolvableType.toClass(), IModuleTask.class)) {
                handleType = resolvableType;
                break;
            }
        }
        return handleType.getGeneric(0).toClass();
    }

    private static boolean hasInitNext(TaskNode node) {
        return moduleContextMap.values()
                .stream()
                .flatMap(List::stream)
                .anyMatch(item -> item.getTaskId().equals(node.getTaskId()));

    }

    private static Integer getNextTaskId() {
        return taskIdCounter.incrementAndGet();
    }
}
