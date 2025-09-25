package P2;


/**
 * 创建一个任务，对于传入该任务的变量进行1000次加法
 */
class Task1 implements Runnable {

    private Counter1 counter;
    public Task1(Counter1 counter) {
        this.counter = counter;
    }

    @Override
    public void run() {
        for (int i = 0; i < 10000; i++) {
            counter.increment();
        }
    }
}

// 注意在这加上了
class Counter1 {
    public int value = 0;
    public synchronized void  increment() {
        value++;
    }
}

public class P2_2_SafeTest {


    public static void main(String[] args) throws InterruptedException {
        Counter1 counter1 = new Counter1();
        // 创建一个包含是个线程的数组
        Thread[] threads = new Thread[10];
        for (int i = 0; i < 10; i++) {
            threads[i] = new Thread(new Task1(counter1));
            threads[i].start();
        }
        // 等待所有线程执行完
        for (int i = 0; i < 10; i++) {
            threads[i].join();
        }

        System.out.println("The Result is "+counter1.value);
    }
}
