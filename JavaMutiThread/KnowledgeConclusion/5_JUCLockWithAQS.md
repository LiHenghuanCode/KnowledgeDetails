# 显式锁和AQS
“显式锁”指的是程序员手动控制加锁和解锁的锁机制，
不是由 Java 关键字（synchronized）隐式管理的锁。

换句话说：

synchronized 是 隐式锁（implicit lock）：JVM 自动获取与释放；

Lock 接口的实现类（如 ReentrantLock）是 显式锁：可以自己自己 lock() 和 unlock()

## 可重入锁ReentrantLock
ReentrantLock是JUC包提供的显式锁的一个基础实现类，
ReentrantLock类实现了Lock接口，它拥有与synchronized相同的并发性和内存语义，
但是拥有了限时抢占、可中断抢占等一些高级锁特性。此外，ReentrantLock基于内置的抽象队列同步器（Abstract Queued Synchronized，AQS）实现，
在争用激烈的场景下，能表现出表内置锁更佳的性能。

ReentrantLock是一个可重入的独占（或互斥）锁，其中两个修饰词的含义为：

（1）可重入的含义：表示该锁能够支持一个线程对资源的重复加锁，也就是说，一个线程可以多次进入同一个锁所同步的临界区代码块。
比如，同一线程在外层函数获得锁后，在内层函数能再次获取该锁，甚至多次抢占到同一把锁。
2）独占的含义：在同一时刻只能有一个线程获取到锁，而其他获取锁的线程只能等待，
只有拥有锁的线程释放了锁后，其他的线程才能够获取锁

ReentrantLock 并不是靠 JVM monitor，而是靠一个通用同步框架 ——
AQS（AbstractQueuedSynchronizer）

## AQS
AQS 是 Java 并发包（java.util.concurrent）的基础框架，它用一个可原子更新的 state 
和一个 FIFO 队列管理线程获取与释放资源的过程，
封装了阻塞、唤醒、超时、中断等底层逻辑，子类只需定义资源的获取和释放条件。

用来构建各种 同步器（Synchronizer） 的抽象模板。
比如这些常见工具，全都是基于 AQS：

| 工具类                       | 模式    | 作用     |
| ------------------------- | ----- | ------ |
| `ReentrantLock`           | 独占    | 互斥锁    |
| `ReentrantReadWriteLock`  | 共享+独占 | 读写分离   |
| `Semaphore`               | 共享    | 信号量    |
| `CountDownLatch`          | 共享    | 倒计时锁   |
| `FutureTask`              | 独占    | 异步结果同步 |
| `ReentrantLock.Condition` | 条件队列  | 精准等待唤醒 |
| `Phaser`                  | 共享    | 多阶段同步  |


换句话说：

AQS 是所有并发控制工具的“锁引擎”。
其他同步器只是它的“壳子”和“语义包装”。

传统做法（如自己写锁）需要：  
管理一个 state 状态；  
手写等待队列；  
处理中断、超时、唤醒逻辑；  
管理公平/非公平顺序。  

AQS = 状态 + 队列 + 模板方法。
它用一个 state 表示资源状态，用一个 FIFO 队列排队等待，并用模板封装了获取与释放逻辑

AQS 有两种同步模式：

模式	含义	示例
独占模式 (Exclusive)	只有一个线程能获取成功	ReentrantLock、FutureTask
共享模式 (Shared)	多个线程可同时通过	Semaphore、CountDownLatch、ReadLock  
只需要实现这四个方法之一：

| 方法                          | 作用     | 模式 |
| --------------------------- | ------ | -- |
| `tryAcquire(int arg)`       | 尝试获取资源 | 独占 |
| `tryRelease(int arg)`       | 释放资源   | 独占 |
| `tryAcquireShared(int arg)` | 尝试共享获取 | 共享 |
| `tryReleaseShared(int arg)` | 释放共享资源 | 共享 |

AQS 用 LockSupport.park() / unpark() 来实现线程挂起与唤醒（底层基于 Unsafe 的系统调用）。  
为什么不用 wait/notify？ park/unpark 不需要锁对象；  
可“先 unpark 后 park”；  
不会丢信号； 性能更高。
