# 一、多线程基础

该篇主要讲解，Java的线程的创建方式










## 1. 基本的创建线程三种方式

### a. 直接new一个thread
更推荐的方式是：新建一个类继承thread类，重写run方法

### b. 新建一个类implement Runnable接口，
新建一个类implement Runnable接口，
把给类的示例对象作为参数传给thread类的实例对象

### c.（带返回值）新建一个类implement Callable接口
新建一个类implement Callable接口，
实例化该类对象（举例为A）
实例化一个FutureTask 对象，把A作为参数传给FutureTask构造器
FutureTask作为参数传给一个thread类的实例对象

具体写法看：[ P1_BaseThread.java](../src/main/java/P1/P1_1_BaseThread.java)

## 2. 用线程池创建线程的方式

### a.标准构造器ThreadPoolExecutor去构造工作线程池
其核心参数如下
```java
public ThreadPoolExecutor(
       int corePoolSize,            // 核心线程数，即使线程空闲（Idle），也不会回收
       int maximumPoolSize,                 // 线程数的上限
       long keepAliveTime, TimeUnit unit,   // 线程最大空闲（Idle）时长 
       BlockingQueue<Runnable> workQueue,     // 任务的排队队列
       ThreadFactory threadFactory,               // 新线程的产生方式
       RejectedExecutionHandler handler)    // 拒绝策略
```
s1--线程池的任务调度流程（包含接收新任务和执行下一个任务）大致如下：
如果当前工作线程数量小于核心线程数量`corePoolSize`，
执行器总是优先创建一个任务线程，而不是从线程队列中获取一个空闲线程

s2--如果线程池中总的任务数量大于核心线程池数量，新接收的任务将被加入阻塞队列中，
**一直到阻塞队列已满**。 在核心线程池数量已经用完、阻塞队列没有满的场景下，线程池不会为新任务创建一个新线程

s3--当完成一个任务的执行时，执行器总是优先从阻塞队列中获取下一个任务，
并开始执行，一直到阻塞队列为空，其中所有的缓存任务被取光

s4--在核心线程池数量已经用完、阻塞队列也已经满了的场景下，如果线程池接收到新的任务，
将**会为新任务创建一个线程（非核心线程）**，并且立即开始执行新任务

s5--在核心线程都用完、阻塞队列已满的情况下，**一直会创建新线程去执行新任务（非核心线程）**，
直到池内的线程总数超出`maximumPoolSize`。如果线程池的线程总数超过`maximumPoolSize`，
线程池就会拒绝接收任务，当新任务过来时，会为新任务执行拒绝策略`RejectedExecutionHandler handler`

s6--非核心线程在`long keepAliveTime, TimeUnit unit`内如果没有接到任务就会自动销毁

### b.基于Executors工厂类的线程创建方式
Executors 工厂类提供的 4 种快捷方法，本质上都是调用 ThreadPoolExecutor 的不同构造参数而已
(a) newFixedThreadPool(int n) 固定线程数，不会扩容，任务排在无界队列里
```java
return new ThreadPoolExecutor(
    n,                     // corePoolSize
    n,                     // maximumPoolSize
    0L, TimeUnit.MILLISECONDS,   // keepAliveTime
    new LinkedBlockingQueue<Runnable>()  // 无界队列
);
```
(b) newCachedThreadPool() 没有核心线程，用到就新建，空闲 60s 回收
```java
return new ThreadPoolExecutor(
    0,                     // corePoolSize
    Integer.MAX_VALUE,     // maximumPoolSize
    60L, TimeUnit.SECONDS, // 空闲线程存活 60s
    new SynchronousQueue<Runnable>()     // 直接交付，不存队列
);
```
(c) newSingleThreadExecutor() 线程数始终为 1，任务顺序执行。
```java
return new FinalizableDelegatedExecutorService(
    new ThreadPoolExecutor(
        1,                     // corePoolSize
        1,                     // maximumPoolSize
        0L, TimeUnit.MILLISECONDS,
        new LinkedBlockingQueue<Runnable>()  // 无界队列
    )
);
```
(d) newScheduledThreadPool(int n)
而 ScheduledThreadPoolExecutor 又继承了 ThreadPoolExecutor，只是增加了定时调度功能
让线程池里的任务能按照指定时间表自动运行（一次延时或周期执行）
普通线程池：你手动 submit(task) → 马上执行。
定时调度线程池：你只要告诉它什么时候/多长时间执行

## 3. ThreadPoolExecutor线程池的进阶使用



## 4.ThreadPoolExecutor的实战


## 5.
