package com.caojiantao.concurrent.spring.widget;

import com.caojiantao.concurrent.spring.SpringStarter;

public interface IHandler<T> {

    void doHandler(IExecutorController controller, T context) throws Exception;

    void onError(IExecutorController controller, Exception e);

    void fallback(IExecutorController controller, T context);
}
