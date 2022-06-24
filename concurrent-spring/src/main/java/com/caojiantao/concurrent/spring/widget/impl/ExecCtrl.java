package com.caojiantao.concurrent.spring.widget.impl;

import com.caojiantao.concurrent.spring.constant.EModuleState;
import com.caojiantao.concurrent.spring.widget.IExecCtrl;

/**
 * @author caojiantao
 */
public class ExecCtrl implements IExecCtrl {

    private volatile EModuleState state;

    public ExecCtrl() {
        this.state = EModuleState.NORMAL;
    }

    @Override
    public EModuleState getState() {
        return state;
    }

    @Override
    public void interrupt() {
        state = EModuleState.INTERRUPT;
    }
}
