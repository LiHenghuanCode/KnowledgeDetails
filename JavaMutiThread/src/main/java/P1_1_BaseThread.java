import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

/**
 * 新建一个类继承thread类，重写run方法
 */

class Thread_01 extends Thread{
    @Override
    public void run() {
        for (int i = 0;i<200;i++){
            System.out.println(Thread.currentThread().getName()+" is coming to "+i);
        }
    }
}

class Thread_02 implements Runnable{
    @Override
    public void run() {
        for (int i = 0;i<200;i++){
            System.out.println(Thread.currentThread().getName()+" is coming to "+i);
        }
    }
}

class Thread_03 implements Callable<Integer> {
    @Override
    public Integer call() throws Exception {
        int result = 0;
        for (int i = 0;i<200;i++){
            System.out.println(Thread.currentThread().getName()+" is coming to "+i);
            result += i;
        }
        return result;
    }
}

public class P1_1_BaseThread {
    public static void main(String[] args) {
        Thread_01 thread_01 = new Thread_01();
        Thread_02 thread_02 = new Thread_02();
        Thread_03 thread_03 = new Thread_03();

        // a. 继承的直接start()
        thread_01.start();

        //b.implement Runnable接口的要再见一个真正的Thread，
        // 然后把implement Runnable接口的类的对象作为参数传入线程的构造器
        Thread realThread = new Thread(thread_02);
        realThread.start();

        //c. implement Callable接口的类对象其实相当于一个任务
        // 这个任务要打包到FutureTask类的对象里
        // 然后再新建一个真正的线程去执行这个任务，
        //任务结果被FutureTask类的对象接收
        // 然后调用FutureTask类的对象的get()方法取出结果，注意要抛出有异常
        FutureTask<Integer> futureTask = new FutureTask<>(thread_03);
        Thread realThread1 = new Thread(futureTask);
        realThread1.start();
        try {
            System.out.println(Thread.currentThread().getName()+" Will give the Result from FutureTask = " + futureTask.get()); // Attention
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }



    }
}
