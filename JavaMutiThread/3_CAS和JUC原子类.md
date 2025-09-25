# CAS

## 1. 高级语言的原子操作不等于操作系统底层的原子操作

原子操作指一条不可再分的指令
比如Java的 i++，实现变量的+1，并且这个操作就Java语言层面而言不可再分
然而从汇编指令的角度来看看++操作的还可细分。具体的可以看

## 2.CAS原理

CAS是一种无锁算法，该算法关键依赖两个值——期望值（旧值）和新值，
底层CPU利用原子操作判断内存原值与期望值是否相等，
如果相等就给内存地址赋新值，否则不做任何操作。

具体操作如下
（1）获得字段的期望值（oldValue）。

（2）计算出需要替换的新值（newValue）。

（3）通过CAS将新值（newValue）放在字段的内存地址上，
如果CAS失败就重复第（1）步到第（2）步，一直到CAS成功，这种重复俗称CAS自旋

例子解释：
假如某个内存地址（某对象的属性）的值为100，
现在有两个线程（线程A和线程B）使用CAS无锁编程对该内存地址进行更新，
线程A欲将其值更新为200，线程B欲将其值更新为300，

线程是并发执行的，谁都有可能先执行。
但是CAS是原子操作，对同一个内存地址的CAS操作在同一时刻只能执行一个。


因此，在这个例子中，要么线程A先执行，要么线程B先执行。假设线程A的CAS(100,200)执行在前，
由于内存地址的旧值100与该CAS的期望值100相等，因此线程A会操作成功，内存地址的值被更新为200。

接下来执行线程B的CAS(100,300)操作，此时内存地址的值为200，
不等于CAS的期望值100，线程B操作失败。线程B只能自旋，开始新的循环，
这一轮循环首先获取到内存地址的值200，然后进行CAS(200,300)操作，
这一次内存地址的值与CAS的预期值（oldValue）相等，线程B操作成功。

## 2.CAS具体操作实现Unsafe的使用
Java应用层的CAS操作主要涉及Unsafe方法的调用，具体如下：

（1）获取Unsafe实例。

（2）调用Unsafe提供的CAS方法，这些方法主要封装了底层CPU的CAS原子操作。

（3）调用Unsafe提供的字段偏移量方法，
    这些方法用于获取对象中的**字段（属性）偏移量**，
    此偏移量值需要作为参数提供给CAS操作

Unsafe类是一个final修饰的不允许继承的最终类,因此，我们无法在外部对Unsafe进行实例化，
那么怎么获取Unsafe的实例呢？可以**通过反射的方式自定义**地获取Unsafe实例的辅助方法
示例代码：[CAS基本操作](src/main/java/P3/P3_1_BaseCAS.java)

## 3. 用unsafe类和CAS原理改造不安全的自增运算

改动速览：
(a) 计数字段改动
```java
public int value = 0; // 原来的Counter类属性
```
```java
private volatile int value = 0;
```
准备 Unsafe 及偏移量（一次性静态初始化）
```java
private static final Unsafe U;
private static final long VALUE_OFFSET;
static {
    Field f = Unsafe.class.getDeclaredField("theUnsafe");
    f.setAccessible(true);
    U = (Unsafe) f.get(null);
    VALUE_OFFSET = U.objectFieldOffset(Counter.class.getDeclaredField("value"));
}
```
通过反射拿到 Unsafe 单例（Unsafe 不能直接 new）。
用 objectFieldOffset 拿到字段 value 在对象内存中的偏移量，
CAS 需要“对象+偏移量”来精确定位要修改的位置

自增逻辑从“非原子 ++”改为“CAS 自旋”
```java
public void increment() {
    value++;                 // 读 -> 改 -> 写（非原子，竞争下会丢失更新）
}
```
```java
public void increment() {
    int prev;
    do {
        prev = value;        // 读（volatile）
    } while (!U.compareAndSwapInt(this, VALUE_OFFSET, prev, prev + 1)); // 比较-交换，失败自旋
}
```

具体源代码看：[CAS改造的安全自增](src/main/java/P3/P3_2_CASincrement.java)

## 3. 封装了CAS操作的JUC
JDK 在 java.util.concurrent.atomic 包里提供了一批 原子类，
这些原子类的关键方法（比如 getAndIncrement()、compareAndSet()）底层就是通过 CAS 指令 实现的
根据操作的目标数据类型，

可以将JUC包中的原子类分为4类：基本原子类、数组原子类、原子引用类和字段更新原子类。
所有这些原子类的底层都基于 CAS + volatile 实现，区别只在于 操作目标不同
| 分类          | 典型类                                                                                           | 特点                                                                        | 应用场景                            |
| ----------- | --------------------------------------------------------------------------------------------- | ------------------------------------------------------------------------- | ------------------------------- |
| **基本类型原子类** | `AtomicInteger`、`AtomicLong`、`AtomicBoolean`                                                  | 提供对基本类型变量的原子操作；常用方法：`get()`、`set()`、`incrementAndGet()`、`compareAndSet()` | 计数器、并发统计（如在线人数、请求数）             |
| **数组原子类**   | `AtomicIntegerArray`、`AtomicLongArray`、`AtomicReferenceArray`                                 | 保证数组某一位置元素的原子更新；不能保证多个位置同时原子                                              | 并发更新数组中的元素，如分段计数、桶统计            |
| **原子引用类**   | `AtomicReference<V>`、`AtomicStampedReference<V>`、`AtomicMarkableReference<V>`                 | 对对象引用的原子更新；支持版本戳（解决 ABA 问题）和标记（逻辑删除）                                      | 实现无锁链表、无锁栈、CAS-based 数据结构       |
| **字段更新原子类** | `AtomicIntegerFieldUpdater<T>`、`AtomicLongFieldUpdater<T>`、`AtomicReferenceFieldUpdater<T,V>` | 通过反射获取字段偏移量，对对象的 `volatile` 字段原子更新；不需额外封装对象                               | 框架/库底层优化，如 AQS、Netty 对对象字段的并发控制 |


## 4. JUC的问题与实践
### 只能保证一个共享变量之间的原子性操作
当对一个共享变量执行操作时，我们可以使用循环CAS的方式来保证原子操作，
但是对多个共享变量操作时，CAS就无法保证操作的原子性。
一个比较简单的规避方法为：把多个共享变量合并成一个共享变量来操作。
JDK提供了AtomicReference类来保证引用对象之间的原子性，
可以把多个变量放在一个AtomicReference实例后再进行CAS操作。
比如有两个共享变量i＝1、j=2，可以将二者合并成一个对象，
然后用CAS来操作该合并对象的AtomicReference引用。

### ABA问题
由于CAS原子操作性能高，因此其在JUC包中被广泛应用，
只不过如果使用得不合理，CAS原子操作就会存在ABA问题

具体例子：

线程 T1 读取值 = A，准备做 CAS(A → C)。

在 T1 执行前，线程 T2 把值从 A 改成 B，再从 B 改回 A。

线程 T1 再做 CAS，看到值还是 A，就以为没被改过，于是 CAS 成功，把值更新为 C。

问题：

虽然 T1 的 CAS 成功了，但实际上这个变量已经被别的线程修改过（A → B → A），

这就可能导致程序错误，上述例子其实影响不大，我们举个影响很大的例子

现有一个LIFO（后进先出）堆栈，该堆栈使用单向链表实现

结构为`head->E2->E1`
假设线程A和线程B是两个在堆栈上进行并发操作的线程，
其中线程A计划从Head位置通过CAS进行元素E2的弹出操作

在线程A刚好启动CAS的执行，但是没有开始之前，线程B抢在前面从Head位置中弹出元素E2、E1，
并压入了一个新元素E3，再压入了E2，线程B完成操作之后，栈帧的Head位置的数据仍然是E2

时切换到线程A的执行，通过CAS操作发现Head位置仍然是E2，线程A操作成功，
元素E2执行弹出操作，堆栈的Head位置变成E1。
尽管线程A的CAS操作成功，但存在一个大的问题。

从头捋一捋
(a)已知的栈顶为E2，这时线程A已经知道E2.next为E1，
然后希望用CAS(E2,E1)将栈顶E2替换为E1，从而将E2从堆栈弹出
A会做的修改: `E2.next =null, head.next = E1`

(b)但是在线程A开始执行CAS(E2,E1)前，CPU的时间片被线程B抢夺。
线程B从Head位置中弹出元素E2、E1，然后压入了元素E3、E2，
最终线程B又将Head位置的数据变成E2
此时栈中：`head->E2->E3`

(c)线程A开始执行CAS(E2,E1),检查到head.next=E2，
于是继续执行 `head.next=E1，E2.next =null`
此时栈中`head->E1`,同时出现了一个没指针指向的游历元素E3(因为原本指向E3的E2的next被改为null)

### ABA问题解决方案
**(a) 用 版本戳：不仅比较指针，还比较版本号**

参考乐观锁的版本号，JDK提供了一个AtomicStampedReference类来解决ABA问题。
AtomicStampReference在CAS的基础上增加了一个Stamp（印戳或标记），
使用这个印戳可以用来觉察数据是否发生变化，给数据带上了一种实效性的检验。
AtomicStampReference的compareAndSet()方法首先检查当前的对象引用值是否等于预期引用，
并且当前印戳（Stamp）标志是否等于预期标志，如果全部相等，
就以原子方式将引用值和印戳（Stamp）标志的值更新为给定的更新值。

**(b)用带标记的引用**

AtomicMarkableReference是AtomicStampedReference的简化版，
不关心修改过几次，只关心是否修改过。因此，其标记属性mark是boolean类型，
而不是数字类型，标记属性mark仅记录值是否修改过。
AtomicMarkableReference适用于只要知道对象是否被修改过，而不适用于对象被反复修改的场景。

### 高并发场景下CAS恶性空自旋
在争用激烈的场景下，会导致大量的CAS空自旋。比如，在大量线程同时并发修改一个AtomicInteger时，
可能有很多线程会不停地自旋，甚至有的线程会进入一个无限重复的循环中

(a) Java 8提供了一个新的类LongAdder，以空间换时间的方式提升高并发场景下CAS操作的性能。
LongAdder的核心思想是热点分离，与ConcurrentHashMap的设计思想类似：
将value值分离成一个数组，当多线程访问时，通过Hash算法将线程映射到数组的一个元素进行操作；
而获取最终的value结果时，则将数组的元素求和。
最终，通过LongAdder将内部操作对象从单个value值“演变”成一系列的数组元素，
从而减小了内部竞争的粒度。

(b) 使用队列削峰，将发生CAS争用的线程加入一个队列中排队，
降低CAS争用的激烈程度。JUC中非常重要的基础类**AQS**（抽象队列同步器）就是这么做的