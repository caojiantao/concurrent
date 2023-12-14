package com.github.caojiantao.concurrent.spring;

/**
 * @param <T> 并行模块上下文
 */
public interface IConcurrentTask<T> {

    void run(T context) throws Exception;

    default void onError(T context, Exception e) {
    }
}
