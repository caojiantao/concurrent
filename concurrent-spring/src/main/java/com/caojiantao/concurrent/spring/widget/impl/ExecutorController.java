package com.caojiantao.concurrent.spring.widget.impl;

import com.caojiantao.concurrent.spring.constant.EModuleState;
import com.caojiantao.concurrent.spring.widget.IExecutorController;

/**
 * @author caojiantao
 */
public class ExecutorController implements IExecutorController {

    private volatile EModuleState state;

    public ExecutorController() {
        this.state = EModuleState.RUNNING;
    }

    @Override
    public EModuleState getState() {
        return state;
    }

    @Override
    public void stopPropagate() {
        // todo 停止向下传播
    }

    @Override
    public void interruptModule() {
        state = EModuleState.INTERRUPT;
    }

    @Override
    public void complete() {
        state = EModuleState.COMPLETE;
    }
}
