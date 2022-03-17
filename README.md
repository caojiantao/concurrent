## Concurrent

基于**事件驱动**，**可视化**的**高可用**的**动态线程池**。

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
// 返回是否正常执行（超时、中断）
boolean sync = ExecutorStarter.build(ExecutorModule.defaultModule())
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
        }).sync(600);
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

2. 定义执行接口；

```java
public interface IProductDetailHandler extends IHandler<Context> {}
```

3. 编写子任务，构造关联关系

```java
@Slf4j
@Component
@ExecutorTask(name = "商品基本信息")
public class ProductBaseInfoHandler implements IProductDetailHandler {
    @Override
    public void doHandler(SpringStarter starter, Context o) throws Exception {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        Thread.sleep(random.nextLong(1000));
        log.info(this.toString() + " doHandler...");
    }

    @Override
    public void onError(SpringStarter starter, Exception e) {
        //starter.setModuleStatus(EModuleStatus.ERROR);
    }
}
```

```java
@Slf4j
@Component
@ExecutorTask(name = "相似商品推荐", depends = {ProductBaseInfoHandler.class})
public class ProductSimilarHandler implements IProductDetailHandler {
    @Override
    public void doHandler(SpringStarter starter, Context o) throws Exception {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        Thread.sleep(random.nextLong(1000));
        log.info(this.toString() + " doHandler...");
    }

    @Override
    public void onError(SpringStarter starter, Exception e) {

    }
}
```

4. 触发执行器

```java
ExecutorModule executorModule = ExecutorModule.defaultModule();
Context moduleContext = new Context();
EModuleStatus status = SpringStarter.build(IProductDetailHandler.class, moduleContext)
        .executorModule(executorModule)
        .sync(10000);
log.info("done..." + status);
```

## 系统架构

## 事件驱动

## 可视化

## 高可用

## 动态线程池
