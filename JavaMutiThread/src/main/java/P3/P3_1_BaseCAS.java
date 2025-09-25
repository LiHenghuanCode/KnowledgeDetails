package P3;

import sun.misc.Unsafe;

import java.lang.reflect.Field;

class Counter3 {
    volatile int value = 0;
}

public class P3_1_BaseCAS {

    public static void main(String[] args) throws Exception {

        //1. 反射获取 Unsafe 实例，注意会产生异常
        Field f = Unsafe.class.getDeclaredField("theUnsafe");
        f.setAccessible(true);
        Unsafe unsafe = (Unsafe) f.get(null);

    // 2. 获取 value 字段在 Counter 对象中的偏移量
        Counter3 counter = new Counter3();
        long offset = unsafe.objectFieldOffset(Counter3.class.getDeclaredField("value"));

        // 3. CAS 操作：如果 counter.value == 0，则更新为 1
        boolean success = unsafe.compareAndSwapInt(counter, offset, 0, 1);
        System.out.println("第一次 CAS 是否成功: " + success + "，当前值 = " + counter.value);

        // 4. CAS 操作：尝试用错误的期望值 0 更新为 2
        success = unsafe.compareAndSwapInt(counter, offset, 0, 2);
        System.out.println("第二次 CAS 是否成功: " + success + "，当前值 = " + counter.value);

    }
}
