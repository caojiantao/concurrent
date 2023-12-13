package com.caojiantao.concurrent.spring.listener;

import com.caojiantao.concurrent.spring.ConcurrentManager;
import com.caojiantao.concurrent.spring.IConcurrentTask;
import com.caojiantao.concurrent.spring.annotation.ConcurrentContext;
import com.caojiantao.concurrent.spring.annotation.ConcurrentTask;
import com.caojiantao.concurrent.spring.constant.ENodeState;
import com.caojiantao.concurrent.spring.entity.ConcurrentModule;
import com.caojiantao.concurrent.spring.entity.ConcurrentTaskNode;
import com.caojiantao.concurrent.spring.util.GenericUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.util.Assert;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

@Slf4j
public class ConcurrentTaskNodeInit implements ApplicationListener<ContextRefreshedEvent> {

    private ApplicationContext applicationContext;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        this.applicationContext = event.getApplicationContext();
        this.init();
    }

    /**
     * 构建任务节点依赖关系图，并保存 context 对应的任务节点集合
     */
    public void init() {
        Map<String, IConcurrentTask> taskMap = applicationContext.getBeansOfType(IConcurrentTask.class);
        log.info("开始初始化全局任务节点");
        MultiValueMap<Class, ConcurrentTaskNode> nextMap = new LinkedMultiValueMap<>();
        List<ConcurrentTaskNode<?>> rootNodeList = new ArrayList<>();
        Map<Class<?>, ConcurrentTaskNode<?>> taskNodeMap = ConcurrentManager.getNodeMap();
        for (IConcurrentTask task : taskMap.values()) {
            ConcurrentTask annotation = task.getClass().getAnnotation(ConcurrentTask.class);
            Assert.notNull(annotation, "Task 必须用 @ConcurrentTask 注解修饰");
            ThreadPoolExecutor threadPool = null;
            if (applicationContext.containsBean(annotation.threadPool())) {
                threadPool = applicationContext.getBean(annotation.threadPool(), ThreadPoolExecutor.class);
            }
            ConcurrentTaskNode<?> node = ConcurrentTaskNode.builder()
                    .name(annotation.taskName())
                    .threadPool(threadPool)
                    .task(task)
                    .depends(0)
                    .nextList(new ArrayList<>())
                    .state(ENodeState.INITIAL)
                    .build();
            taskNodeMap.put(task.getClass(), node);
            Class<?>[] depends = annotation.depends();
            if (depends.length == 0) {
                // 根节点
                rootNodeList.add(node);
            } else {
                // 子节点列表
                for (Class<?> depend : depends) {
                    nextMap.add(depend, node);
                }
            }
        }
        rootNodeList.forEach(node -> initTaskNode(node, nextMap));
        Map<Class<?>, ConcurrentModule<?>> moduleMap = ConcurrentManager.getModuleMap();
        log.info("初始化全局任务节点完成，{} 个并发模块，共 {} 个并发任务", moduleMap.size(), taskNodeMap.size());
    }

    private void initTaskNode(ConcurrentTaskNode node, MultiValueMap<Class, ConcurrentTaskNode> nextMap) {
        if (!Objects.equals(node.getState(), ENodeState.INITIAL)) {
            return;
        }
        List<ConcurrentTaskNode> nextList = nextMap.getOrDefault(node.getTask().getClass(), new ArrayList<>());
        for (ConcurrentTaskNode<?> child : nextList) {
            child.setDepends(child.getDepends() + 1);
            // 递归初始化子节点
            initTaskNode(child, nextMap);
        }
        node.setState(ENodeState.NORMAL);
        node.setNextList(nextList);
        // 节点初始化完，需要尝试初始化所在模块
        ConcurrentModule module = initTaskModule(node);

        String nexts = nextList.stream().map(ConcurrentTaskNode::getName).collect(Collectors.joining(","));
        log.info("初始化任务节点成功，模块“{}”，任务节点为“{}”，后置节点为”{}“", module.getName(), node.getName(), nexts);
    }

    private ConcurrentModule initTaskModule(ConcurrentTaskNode node) {
        Map<Class<?>, ConcurrentModule<?>> taskModuleMap = ConcurrentManager.getModuleMap();
        Class<?> contextClass = GenericUtils.getInterfaceGeneric(node.getTask(), IConcurrentTask.class);
        Assert.notNull(contextClass, "解析并发模块上下文 Class 失败");
        ConcurrentModule taskModule = taskModuleMap.computeIfAbsent(contextClass, clazz -> {
            ConcurrentContext annotation = clazz.getAnnotation(ConcurrentContext.class);
            Assert.notNull(annotation, "并发模块上下文必须用 @ConcurrentContext 注解修饰");
            ThreadPoolExecutor threadPool = applicationContext.getBean(annotation.threadPool(), ThreadPoolExecutor.class);
            return ConcurrentModule.builder()
                    .name(annotation.moduleName())
                    .timeout(annotation.timeout())
                    .threadPool(threadPool)
                    .rootNodeList(new ArrayList<>())
                    .nodeSize(0)
                    .build();
        });
        if (node.getDepends() == 0) {
            taskModule.getRootNodeList().add(node);
        }
        taskModule.setNodeSize(taskModule.getNodeSize() + 1);
        return taskModule;
    }
}
