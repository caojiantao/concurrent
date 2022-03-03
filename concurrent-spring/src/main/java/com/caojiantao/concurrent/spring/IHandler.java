package com.caojiantao.concurrent.spring;

public interface IHandler<Context> {

    void doHandler(Context context) throws Exception;

    void onError(Exception e);

    default void fallback(Context context) {}
}
