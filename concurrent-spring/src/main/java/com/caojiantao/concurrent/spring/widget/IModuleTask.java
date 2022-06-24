package com.caojiantao.concurrent.spring.widget;

public interface IModuleTask<T> {

    void doHandler(IExecCtrl ctrl, T context) throws Exception;

    void onError(IExecCtrl ctrl, T context, Exception e);
}
