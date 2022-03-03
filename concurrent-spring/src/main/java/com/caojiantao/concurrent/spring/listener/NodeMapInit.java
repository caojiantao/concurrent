package com.caojiantao.concurrent.spring.listener;

import com.caojiantao.concurrent.spring.TaskNodeManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

@Slf4j
public class NodeMapInit implements ApplicationListener<ContextRefreshedEvent> {

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        TaskNodeManager.initTaskNodeList(event.getApplicationContext());
    }
}
