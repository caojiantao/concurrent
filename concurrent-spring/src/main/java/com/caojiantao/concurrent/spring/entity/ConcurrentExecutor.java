package com.caojiantao.concurrent.spring.entity;

import com.caojiantao.concurrent.spring.constant.EModuleInfoState;
import lombok.Data;

@Data
public class ConcurrentExecutor implements IConcurrentExecutor {

    private ConcurrentModuleInfo concurrentModuleInfo;

    @Override
    public void interruptModule() {
        concurrentModuleInfo.setState(EModuleInfoState.INTERRUPT);
    }
}
