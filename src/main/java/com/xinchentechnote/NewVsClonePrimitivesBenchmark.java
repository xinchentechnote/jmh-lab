package com.xinchentechnote;

import org.openjdk.jmh.annotations.*;

import java.util.concurrent.TimeUnit;

@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 5, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(3)
public class NewVsClonePrimitivesBenchmark {

    private static class DataHolder implements Cloneable {
        // All primitive types
        byte b;
        short s;
        int i;
        long l;
        float f;
        double d;
        char c;
        boolean bool;

        public DataHolder() {
            this.b = (byte) 1;
            this.s = (short) 2;
            this.i = 3;
            this.l = 4L;
            this.f = 5.0f;
            this.d = 6.0;
            this.c = '7';
            this.bool = true;
        }

        @Override
        public DataHolder clone() {
            try {
                return (DataHolder) super.clone();
            } catch (CloneNotSupportedException e) {
                throw new AssertionError();
            }
        }
    }

    private DataHolder prototype;
    private int count = 100;

    @Setup
    public void setup() {
        prototype = new DataHolder();
    }

    @Benchmark
    public void newInstance() {
        for (int i = 0; i < count; i++) {
            DataHolder dataHolder = new DataHolder();
        }
    }

    @Benchmark
    public void cloneInstance() {
        for (int i = 0; i < count; i++) {
            DataHolder clone = prototype.clone();
        }
    }
}
