package com.xinchentechnote;

import com.lmax.disruptor.*;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import com.lmax.disruptor.util.DaemonThreadFactory;
import org.jctools.queues.SpscArrayQueue;
import org.openjdk.jmh.annotations.*;

import java.util.concurrent.TimeUnit;

@BenchmarkMode({Mode.Throughput, Mode.AverageTime})
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@State(Scope.Benchmark)
public class SpscBenchmark {

    private static final int CAPACITY = 1 << 16;
    private long[] data;

    // JCTools SPSC 队列
    private SpscArrayQueue<Long> jctoolsQueue;

    // Disruptor
    private RingBuffer<ValueEvent> ringBuffer;
    private Disruptor<ValueEvent> disruptor;

    @Setup(Level.Trial)
    public void setup() {
        data = new long[CAPACITY];
        for (int i = 0; i < CAPACITY; i++) data[i] = i;

        // Setup JCTools SPSC queue
        jctoolsQueue = new SpscArrayQueue<>(CAPACITY);

        // Setup Disruptor
        disruptor = new Disruptor<>(
            ValueEvent::new,
            CAPACITY,
            DaemonThreadFactory.INSTANCE,
            ProducerType.SINGLE,
            new BusySpinWaitStrategy()
        );

        disruptor.handleEventsWith(new ValueEventHandler());
        ringBuffer = disruptor.start();
    }

    @TearDown(Level.Trial)
    public void tearDown() {
        disruptor.shutdown();
    }

    // ==== JCTools Benchmark ====

    @Benchmark
    public void produceJctools() {
        long v = data[(int)(System.nanoTime() & (CAPACITY - 1))];
        while (!jctoolsQueue.offer(v)) {
            // busy spin
        }
    }

    // ==== Disruptor Benchmark ====

    @Benchmark
    public void produceDisruptor() {
        long seq = ringBuffer.next();
        ringBuffer.get(seq).setValue(System.nanoTime());
        ringBuffer.publish(seq);
    }

    // ==== Event container ====

    public static class ValueEvent {
        private long value;
        public void setValue(long v) { this.value = v; }
        public long getValue() { return value; }
    }

    public static class ValueEventHandler implements EventHandler<ValueEvent> {
        @Override
        public void onEvent(ValueEvent event, long sequence, boolean endOfBatch) {
            // Simple consume (simulate processing)
            long v = event.getValue();
            // Blackhole.consumeCPU(1); // optional if you want fake work
        }
    }
}
