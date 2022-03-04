package com.caojiantao.concurrent.spring;

public interface IHandler<T> {

    void doHandler(SpringStarter starter, T context) throws Exception;

    void onError(SpringStarter starter, Exception e);

    default void fallback(T context) {
    }
}
