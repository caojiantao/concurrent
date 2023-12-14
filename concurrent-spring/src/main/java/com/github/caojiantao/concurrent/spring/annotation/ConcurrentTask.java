package com.github.caojiantao.concurrent.spring.annotation;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ConcurrentTask {

    /**
     * 任务名称
     */
    String taskName();

    /**
     * 前置任务列表
     */
    Class[] depends() default {};

    /**
     * 使用的线程池 beanName
     */
    String threadPool() default "";
}
