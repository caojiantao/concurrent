package com.caojiantao.concurrent.example.spring.service.handler;

import com.caojiantao.concurrent.example.spring.service.TestContext;
import com.caojiantao.concurrent.spring.annotation.ConcurrentTask;
import com.caojiantao.concurrent.spring.entity.IConcurrentExecutor;
import com.caojiantao.concurrent.spring.IConcurrentTask;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@ConcurrentTask(taskName = "DDD", depends = {AAA.class, EEE.class}, threadPool = "testPool2")
public class DDD implements IConcurrentTask<TestContext> {

    @Override
    public void run(TestContext context) throws Exception {
        log.info("======================================= DDD =======================================");
        int delay = ThreadLocalRandom.current().nextInt(1000);
        TimeUnit.MILLISECONDS.sleep(delay);
    }
}
