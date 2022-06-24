## Concurrent

基于**事件驱动**，**可视化**的**高可用**的**动态线程池**。

## 系统架构

## 快速开始

### 简单场景：并发工具

```xml
<dependency>
    <groupId>com.caojiantao.concurrent</groupId>
    <artifactId>concurrent-core</artifactId>
    <version>XXX</version>
</dependency>
```

```java
ExecutorStarter.build()
    .addTask("获取基本信息", () -> {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        Thread.sleep(random.nextLong(1000));
        System.out.println("基本信息为xxx");
    }).addTask("获取额外信息", () -> {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        Thread.sleep(random.nextLong(1000));
        System.out.println("额外信息为xxx");
    }).addTask("获取运营推广", () -> {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        Thread.sleep(random.nextLong(1000));
        System.out.println("推广信息为xxx");
    }).sync(800);
```

### 复杂场景：SpringStater

```xml
<dependency>
    <groupId>com.caojiantao.concurrent</groupId>
    <artifactId>concurrent-spring</artifactId>
    <version>XXX</version>
</dependency>
```

1. 定义执行上下文；

```java
public class Context {}
```

2. 编写子任务，构造关联关系

```java
@Slf4j
@Component
@ExecutorTask(name = "商品基本信息")
public class ProductBaseInfoHandler implements IModuleTask<Context> {
    @Override
    public void doHandler(IExecCtrl ctrl, Context o) throws Exception {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        Thread.sleep(random.nextLong(1000));
        log.info(this.toString() + " doHandler...");
    }

    @Override
    public void onError(IExecCtrl ctrl, Context o, Exception e) {
        // 模块中断
        ctrl.interrupt();
    }
}
```

```java
@Slf4j
@Component
@ExecutorTask(name = "相似商品推荐", depends = {ProductBaseInfoHandler.class})
public class ProductSimilarHandler implements IModuleTask<Context> {
    @Override
    public void doHandler(IExecCtrl ctrl, Context o) throws Exception {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        Thread.sleep(random.nextLong(1000));
        log.info(this.toString() + " doHandler...");
    }

    @Override
    public void onError(IExecCtrl ctrl, Context o, Exception e) {

    }
}
```

4. 触发执行器

```java
ExecutorModule executorModule = ExecutorModule.defaultModule();
Context moduleContext = new Context();
SpringStarter.build(moduleContext)
        .module(executorModule)
        .sync(1000);
```

## 事件驱动

## 可视化

## 高可用

## 动态线程池
