package com.caojiantao.concurrent.spring.entity;

import com.caojiantao.concurrent.spring.constant.EModuleInfoState;
import lombok.Data;

@Data
public class ConcurrentExecutor<T> implements IConcurrentExecutor<T> {

    private ConcurrentModuleInfo<T> concurrentModuleInfo;

    @Override
    public void interruptModule() {
        concurrentModuleInfo.setState(EModuleInfoState.INTERRUPT);
    }
}
