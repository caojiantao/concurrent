package com.caojiantao.concurrent.spring.widget;

import com.caojiantao.concurrent.spring.constant.EModuleState;

/**
 * @author caojiantao
 */
public interface IExecCtrl {

    EModuleState getState();

    void interrupt();
}
