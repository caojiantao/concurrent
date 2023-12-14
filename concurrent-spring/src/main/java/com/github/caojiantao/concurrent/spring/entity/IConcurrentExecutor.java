package com.github.caojiantao.concurrent.spring.entity;

public interface IConcurrentExecutor<T> {

    /**
     * 中断当前模块
     */
    void interruptModule();
}
