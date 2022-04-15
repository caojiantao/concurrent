package com.caojiantao.concurrent.spring.widget;

public interface ITaskHandler<T> {

    void doHandler(IExecutorController controller, T context) throws Exception;

    void onError(IExecutorController controller, Exception e);

    void fallback(IExecutorController controller, T context);
}
