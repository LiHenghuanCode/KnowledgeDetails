package P3;


import sun.misc.Unsafe;

import java.lang.reflect.Field;

/**
 * 创建一个任务，对于传入该任务的变量进行1000次加法
 */
class TaskCAS implements Runnable {

    private Counter4 counter;
    public TaskCAS(Counter4 counter) {
        this.counter = counter;
    }
    public TaskCAS(int count) {

    }
    @Override
    public void run() {
        for (int i = 0; i < 10000; i++) {
            counter.increment();
        }
    }
}


class Counter4 {
    volatile int value = 0;
    // ---- Unsafe 准备 ----
    private static final Unsafe U;
    private static final long VALUE_OFFSET;
    static {
        try {
            Field f = Unsafe.class.getDeclaredField("theUnsafe");
            f.setAccessible(true);
            U = (Unsafe) f.get(null);
            // 获取value属性在内存中的位置
            VALUE_OFFSET = U.objectFieldOffset(Counter4.class.getDeclaredField("value"));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void increment() {
        int prev;
        do {
            prev = value; // 读取当前值（volatile）
        } while (!U.compareAndSwapInt(this, VALUE_OFFSET, prev, prev + 1));
        // 根据内存地址取出值，若值没改变，则进行改变
    }
}



public class P3_2_CASincrement {
    public static void main(String[] args) throws InterruptedException {
        Counter4 counter = new Counter4();
        // 创建一个包含是个线程的数组
        Thread[] threads = new Thread[10];
        for (int i = 0; i < 10; i++) {
            threads[i] = new Thread(new TaskCAS(counter));
            threads[i].start();
        }
        // 等待所有线程执行完
        for (int i = 0; i < 10; i++) {
            threads[i].join();
        }

        System.out.println("The Result is "+counter.value);
    }
}


