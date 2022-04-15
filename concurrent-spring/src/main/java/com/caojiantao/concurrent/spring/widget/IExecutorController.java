package com.caojiantao.concurrent.spring.widget;

import com.caojiantao.concurrent.spring.constant.EModuleState;

/**
 * @author caojiantao
 */
public interface IExecutorController {

    EModuleState getState();

    void stopPropagate();

    void interruptModule();

    void complete();
}
