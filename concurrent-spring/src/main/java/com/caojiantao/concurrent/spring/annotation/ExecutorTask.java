package com.caojiantao.concurrent.spring.annotation;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ExecutorTask {

    String name() default "";

    Class[] depends() default {};
}
