package com.caojiantao.concurrent.example.spring;

import com.caojiantao.concurrent.example.spring.service.TestContext;
import com.caojiantao.concurrent.spring.ConcurrentStarter;
import com.caojiantao.concurrent.spring.entity.ConcurrentModuleInfo;
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
        ConcurrentModuleInfo moduleInfo = ConcurrentStarter.run(testContext);
        log.info("done...");
    }

}
