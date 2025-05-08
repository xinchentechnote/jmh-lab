package com.xinchentechnote;

import com.lmax.disruptor.*;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import com.lmax.disruptor.util.DaemonThreadFactory;
import org.jctools.queues.SpscArrayQueue;
import org.openjdk.jmh.annotations.*;

@State(Scope.Benchmark)
public class SpscBenchmark {

    private static final int CAPACITY = 1 << 16;
    private long[] data;

    // JCTools SPSC 队列
    SpscArrayQueue<Long> jctoolsQueue;
    // Disruptor RingBuffer
    RingBuffer<ValueEvent> ringBuffer;
    SequenceBarrier barrier;
    ValueEventHandler handler;

    @Setup(Level.Trial)
    public void setup() {
        data = new long[CAPACITY];
        for (int i = 0; i < CAPACITY; i++) data[i] = i;

        // JCTools
        jctoolsQueue = new SpscArrayQueue<>(CAPACITY);

        // Disruptor
        Disruptor<ValueEvent> disruptor = new Disruptor<>(
            ValueEvent::new,
            CAPACITY,
            DaemonThreadFactory.INSTANCE,
            ProducerType.SINGLE,
            new BusySpinWaitStrategy()
        );
        handler = new ValueEventHandler();
        disruptor.handleEventsWith(handler);
        ringBuffer = disruptor.start();
        barrier = ringBuffer.newBarrier();
    }

    // 生产者 — JCTools
    @Benchmark
    @Group("jctools")
    @GroupThreads(1)
    public void produceJctools() {
        long v = data[(int)(System.nanoTime() & (CAPACITY - 1))];
        while (!jctoolsQueue.offer(v)) { /* spin */ }
    }

    // 消费者 — JCTools
    @Benchmark
    @Group("jctools")
    @GroupThreads(1)
    public long consumeJctools() {
        Long v;
        while ((v = jctoolsQueue.poll()) == null) { /* spin */ }
        return v;
    }

    // 生产者 — Disruptor
    @Benchmark
    @Group("disruptor")
    @GroupThreads(1)
    public void produceDisruptor() {
        long seq = ringBuffer.next();
        ringBuffer.get(seq).setValue(System.nanoTime());
        ringBuffer.publish(seq);
    }

    // 消费者 — Disruptor
    @Benchmark
    @Group("disruptor")
    @GroupThreads(1)
    public long consumeDisruptor() throws TimeoutException, AlertException, InterruptedException {
        long seq = barrier.waitFor(handler.getProcessedSequence());
        ValueEvent ev = ringBuffer.get(seq);
        long v = ev.getValue();
        handler.setProcessedSequence(seq + 1);
        return v;
    }

    // 事件载体
    public static class ValueEvent {
        private long value;
        public void setValue(long v) { this.value = v; }
        public long getValue() { return value; }
    }

    // 简单 Handler，只记录已消费序号
    public static class ValueEventHandler implements EventHandler<ValueEvent> {
        private volatile long seq = -1;
        @Override
        public void onEvent(ValueEvent event, long sequence, boolean endOfBatch) {
            this.seq = sequence;
        }
        public long getProcessedSequence() { return seq; }
        public void setProcessedSequence(long s) { this.seq = s; }
    }
}
