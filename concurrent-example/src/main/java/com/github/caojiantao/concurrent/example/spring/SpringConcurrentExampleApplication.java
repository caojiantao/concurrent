package com.github.caojiantao.concurrent.example.spring;

import com.github.caojiantao.concurrent.example.spring.service.TestContext;
import com.github.caojiantao.concurrent.spring.ConcurrentStarter;
import com.github.caojiantao.concurrent.spring.entity.ConcurrentModuleInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@Slf4j
@SpringBootApplication
public class SpringConcurrentExampleApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(SpringConcurrentExampleApplication.class, args);
        TestContext testContext = new TestContext();
        ConcurrentModuleInfo<TestContext> moduleInfo = ConcurrentStarter.run(testContext);
        log.info("done..." + moduleInfo);
    }

}
