package com.xinchentechnote;

import org.openjdk.jmh.annotations.*;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.lang.reflect.Field;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import sun.misc.Unsafe;

@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Thread)
public class MemeryVisibilityBenchmark {

    static final int ITERATIONS = 1_000_000;

    // ========== Shared Variables ==========

    static class Box {
        public long plainValue = 0;
        public volatile long volatileValue = 0;
        public long unsafeValue = 0;
        public long varHandleValue = 0;
        public final AtomicLong atomicValue = new AtomicLong(0);
    }

    Box box = new Box();

    static Unsafe unsafe;
    static long unsafeOffset;

    static VarHandle vh;

    static {
        try {
            Field f = Unsafe.class.getDeclaredField("theUnsafe");
            f.setAccessible(true);
            unsafe = (Unsafe) f.get(null);
            unsafeOffset = unsafe.objectFieldOffset(Box.class.getDeclaredField("unsafeValue"));

            vh = MethodHandles.lookup().findVarHandle(Box.class, "varHandleValue", long.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // ========== Benchmarks ==========

    @Benchmark
    public void testPlain() {
        for (int i = 0; i < ITERATIONS; i++) {
            box.plainValue = i;
            long r = box.plainValue;
        }
    }

    @Benchmark
    public void testVolatile() {
        for (int i = 0; i < ITERATIONS; i++) {
            box.volatileValue = i;
            long r = box.volatileValue;
        }
    }

    @Benchmark
    public void testUnsafePutOrderedObject() {
        for (int i = 0; i < ITERATIONS; i++) {
            unsafe.putOrderedLong(box, unsafeOffset, i);
            long r = box.unsafeValue;
        }
    }

    @Benchmark
    public void testVarHandle() {
        for (int i = 0; i < ITERATIONS; i++) {
            vh.setRelease(box, i);
            long r = (long) vh.getAcquire(box);
        }
    }

    @Benchmark
    public void testAtomic() {
        for (int i = 0; i < ITERATIONS; i++) {
            box.atomicValue.set(i);
            long r = box.atomicValue.get();
        }
    }
}
