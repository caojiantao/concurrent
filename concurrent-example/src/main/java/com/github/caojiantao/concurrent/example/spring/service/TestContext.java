package com.github.caojiantao.concurrent.example.spring.service;

import com.github.caojiantao.concurrent.spring.annotation.ConcurrentContext;

/**
 * @author caojiantao
 */
@ConcurrentContext(moduleName = "测试执行模块", threadPool = "testPool", timeout = 10000L)
public class TestContext {

}