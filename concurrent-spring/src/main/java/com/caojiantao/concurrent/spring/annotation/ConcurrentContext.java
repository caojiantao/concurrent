package com.caojiantao.concurrent.spring.annotation;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ConcurrentContext {

    /**
     * 并发模块名称
     */
    String moduleName();

    /**
     * 使用的线程池 beanName
     */
    String threadPool();

    /**
     * 模块执行超时
     */
    long timeout() default 1000L;
}
