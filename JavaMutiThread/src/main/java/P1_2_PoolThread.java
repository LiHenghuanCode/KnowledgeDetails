import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * a. 标准构造器ThreadPoolExecutor去构造工作线程池
 * s1--建议手写ThreadFactory threadFactory
 * s2--自定类implement Runnable接口，此时该类相当于一个任务
 * s3--implement Runnable接口的对象，使用线程池的execute()方法
 *
 * s2-- 或者自定类implement Callable接口
 * implement Callable的对象使用线程池的submit()方法
 * s3--注意新建一个Future对象接收submit()返回的参数
 *
 * attention: 注意线程池要shutdown关闭
 *
 */

class RunTask1 implements Runnable {
    @Override
    public void run() {
        for (int i = 0; i < 100; i++) {
            System.out.println(Thread.currentThread().getName() + "闹麻了闹麻了" + i);
        }
    }
}

class RunTask2 implements Runnable {
    @Override
    public void run() {
        for (int i = 0; i < 100; i++) {
            System.out.println(Thread.currentThread().getName() + "真好玩真好玩" + i);
        }
    }
}

class CallTask1 implements Callable<Integer> {
    @Override
    public Integer call() throws Exception {
        int result = 0;
        for (int i = 0; i < 100; i++) {
            result += i;
            System.out.println(Thread.currentThread().getName()+"加几家加啊"+i);
        }
        return result;
    }
}

class MyThreadFactory implements ThreadFactory
{
    private final AtomicInteger count = new AtomicInteger(1);
	@Override
	public Thread newThread(Runnable r)
	{
        Thread t = new Thread(r);
        t.setName("MyThread-" + count.getAndIncrement()+"AHAHHAH");
        return t;
    }
}



public class P1_2_PoolThread {
    public static void main(String[] args) {
        // pool用默认的线程工厂
        ThreadPoolExecutor pool1 = new ThreadPoolExecutor(
                5,
                10,
                60,
                TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(10));

        RunTask1 r1 = new RunTask1();
        RunTask1 r1r1 = new RunTask1();
        RunTask2 r2 = new RunTask2();
        CallTask1 c1 = new CallTask1();

        pool1.execute(r1);
        pool1.execute(r1r1);
        pool1.execute(r2);
        Future<Integer> future = pool1.submit(c1);
        try {
            System.out.println("the result from future is"+future.get());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }

        System.out.println("Now ThreadPoolExecutor 2 is coming");
        System.out.println("Now ThreadPoolExecutor 2 is coming");
        System.out.println("Now ThreadPoolExecutor 2 is coming");
        System.out.println("Now ThreadPoolExecutor 2 is coming");


        ThreadPoolExecutor pool2 = new ThreadPoolExecutor(
                5,
                10,
                60,
                TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(10),
                // 使用自定义的工厂
                new MyThreadFactory());

        RunTask1 p2r1r1 = new RunTask1();
        RunTask1 p2r1r2 = new RunTask1();
        RunTask2 p2r2r1 = new RunTask2();
        CallTask1 p2c2c1 = new CallTask1();
        pool2.execute(p2r1r1);
        pool2.execute(p2r1r2);
        pool2.execute(p2r2r1);
        Future<Integer> future1 = pool2.submit(p2c2c1);
        try {
            System.out.println("the result from future is"+future.get());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

}
