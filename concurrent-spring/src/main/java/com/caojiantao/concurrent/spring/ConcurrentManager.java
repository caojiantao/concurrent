package com.caojiantao.concurrent.spring;

import com.caojiantao.concurrent.spring.entity.ConcurrentModule;
import com.caojiantao.concurrent.spring.entity.ConcurrentTaskNode;
import com.caojiantao.concurrent.spring.entity.IConcurrentExecutor;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class ConcurrentManager {

    /**
     * 全局的并发模块
     */
    private static final Map<Class<?>, ConcurrentModule<?>> moduleMap = new HashMap<>();

    private static final Map<Class<?>, ConcurrentTaskNode<?>> nodeMap = new HashMap<>();

    private static final ThreadLocal<IConcurrentExecutor> executorLocal = new ThreadLocal<>();

    public static <T> ConcurrentModule getModule(Class<T> clazz) {
        return moduleMap.get(clazz);
    }

    public static Map<Class<?>, ConcurrentModule<?>> getModuleMap() {
        return moduleMap;
    }

    public static Map<Class<?>, ConcurrentTaskNode<?>> getNodeMap() {
        return nodeMap;
    }

    public static void setExecutor(IConcurrentExecutor executor) {
        executorLocal.set(executor);
    }

    public static IConcurrentExecutor getExecutor() {
        return executorLocal.get();
    }

    public static void clearExecutor() {
        executorLocal.remove();
    }
}
