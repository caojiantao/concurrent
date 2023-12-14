package com.github.caojiantao.concurrent.spring.entity;

import com.github.caojiantao.concurrent.spring.constant.EModuleInfoState;
import lombok.Data;

@Data
public class ConcurrentExecutor<T> implements IConcurrentExecutor<T> {

    private ConcurrentModuleInfo<T> concurrentModuleInfo;

    @Override
    public void interruptModule() {
        concurrentModuleInfo.setState(EModuleInfoState.INTERRUPT);
    }
}
