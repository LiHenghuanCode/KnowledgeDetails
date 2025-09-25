package P2;


/**
 * 创建一个任务，对于传入该任务的变量进行1000次加法
 */
class Task implements Runnable {

    private Counter counter;
    public Task(Counter counter) {
        this.counter = counter;
    }
    public Task(int count) {

    }
    @Override
    public void run() {
        for (int i = 0; i < 3000; i++) {
             counter.increment();
        }
    }
}

class Counter {
    public int value = 0;
    public void increment() {
        value++;
    }
}

public class P2_1_UnsafeTest {


    public static void main(String[] args) throws InterruptedException {
        Counter counter = new Counter();
        // 创建一个包含是个线程的数组
        Thread[] threads = new Thread[10];
        for (int i = 0; i < 10; i++) {
            threads[i] = new Thread(new Task(counter));
            threads[i].start();
        }
        // 等待所有线程执行完
        for (int i = 0; i < 10; i++) {
            threads[i].join();
        }

        System.out.println("The Result is "+counter.value);
    }
}
