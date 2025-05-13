package com.xinchentechnote;

import org.openjdk.jmh.annotations.*;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.lang.reflect.Field;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import sun.misc.Unsafe;

@BenchmarkMode({Mode.Throughput, Mode.AverageTime})
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class VisibilityLatencyBenchmark {

    // ========== Unsafe 初始化 ==========
    static Unsafe unsafe;
    static long unsafeOffset;

    static VarHandle vh;

    static {
        try {
            Field f = Unsafe.class.getDeclaredField("theUnsafe");
            f.setAccessible(true);
            unsafe = (Unsafe) f.get(null);

            unsafeOffset = unsafe.objectFieldOffset(BoxUnsafe.class.getDeclaredField("value"));
            vh = MethodHandles.lookup().findVarHandle(BoxVarHandle.class, "value", long.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // ========== 1. volatile 测试 ==========

    @State(Scope.Group)
    public static class BoxVolatile {
        public volatile long value;
    }

    @Group("volatile")
    @GroupThreads(1)
    @Benchmark
    public void writerVolatile(BoxVolatile box) {
        box.value = System.nanoTime();
    }

    @Group("volatile")
    @GroupThreads(1)
    @Benchmark
    public long readerVolatile(BoxVolatile box) {
        return box.value;
    }

    // ========== 2. AtomicLong 测试 ==========

    @State(Scope.Group)
    public static class BoxAtomic {
        public final AtomicLong value = new AtomicLong();
    }

    @Group("atomic")
    @GroupThreads(1)
    @Benchmark
    public void writerAtomic(BoxAtomic box) {
        box.value.set(System.nanoTime());
    }

    @Group("atomic")
    @GroupThreads(1)
    @Benchmark
    public long readerAtomic(BoxAtomic box) {
        return box.value.get();
    }

    // ========== 3. Unsafe putOrdered 测试 ==========

    @State(Scope.Group)
    public static class BoxUnsafe {
        public long value;
    }

    @Group("unsafe")
    @GroupThreads(1)
    @Benchmark
    public void writerUnsafe(BoxUnsafe box) {
        unsafe.putOrderedLong(box, unsafeOffset, System.nanoTime());
    }

    @Group("unsafe")
    @GroupThreads(1)
    @Benchmark
    public long readerUnsafe(BoxUnsafe box) {
        return box.value;
    }

    // ========== 4. VarHandle (setRelease/getAcquire) 测试 ==========

    @State(Scope.Group)
    public static class BoxVarHandle {
        public long value;
    }

    @Group("varhandle")
    @GroupThreads(1)
    @Benchmark
    public void writerVH(BoxVarHandle box) {
        vh.setRelease(box, System.nanoTime());
    }

    @Group("varhandle")
    @GroupThreads(1)
    @Benchmark
    public long readerVH(BoxVarHandle box) {
        return (long) vh.getAcquire(box);
    }
}
