package com.caojiantao.concurrent.example.spring.service.handler;

import com.caojiantao.concurrent.example.spring.service.TestContext;
import com.github.caojiantao.concurrent.spring.IConcurrentTask;
import com.github.caojiantao.concurrent.spring.annotation.ConcurrentTask;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@ConcurrentTask(taskName = "AAA")
public class AAA implements IConcurrentTask<TestContext> {

    @Override
    public void run(TestContext context) throws Exception {
        log.info("======================================= AAA =======================================");
        int delay = ThreadLocalRandom.current().nextInt(1000);
        TimeUnit.MILLISECONDS.sleep(delay);
    }
}
