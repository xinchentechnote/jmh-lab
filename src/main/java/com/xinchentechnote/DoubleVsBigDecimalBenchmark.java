package com.xinchentechnote;

import org.openjdk.jmh.annotations.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Thread)
public class DoubleVsBigDecimalBenchmark {

    @Benchmark
    public double doubleCal() {
        double d1 = 123456.789;
        double d2 = 98765.4321;
        double d3 = 0.5;
        return d1 + (d2 - d1 / d2) * d3;
    }

    @Benchmark
    public BigDecimal bigDecimalCal() {
        // bd1 + (bd2 - bd1 / bd2) * bd3
        BigDecimal bd1 = new BigDecimal("123456.789");
        BigDecimal bd2 = new BigDecimal("98765.4321");
        BigDecimal bd3 = new BigDecimal("0.5");

        return bd1.add((bd2.subtract(bd1.divide(bd2, 8, RoundingMode.HALF_UP))).multiply(bd3));
    }

    @Benchmark
    public double doubleAdd() {
        double d1 = 123456.789;
        double d2 = 98765.4321;
        return d1 + d2;
    }

    @Benchmark
    public BigDecimal bigDecimalAdd() {
        BigDecimal bd1 = new BigDecimal("123456.789");
        BigDecimal bd2 = new BigDecimal("98765.4321");
        return bd1.add(bd2);
    }

    @Benchmark
    public double doubleSubtract() {
        double d1 = 123456.789;
        double d2 = 98765.4321;
        return d1 - d2;
    }

    @Benchmark
    public BigDecimal bigDecimalSubtract() {
        BigDecimal bd1 = new BigDecimal("123456.789");
        BigDecimal bd2 = new BigDecimal("98765.4321");
        return bd1.subtract(bd2);
    }

    @Benchmark
    public double doubleMultiply() {
        double d1 = 123456.789;
        double d2 = 98765.4321;
        return d1 * d2;
    }

    @Benchmark
    public BigDecimal bigDecimalMultiply() {
        BigDecimal bd1 = new BigDecimal("123456.789");
        BigDecimal bd2 = new BigDecimal("98765.4321");
        return bd1.multiply(bd2);
    }

    @Benchmark
    public double doubleDivide() {
        double d1 = 123456.789;
        double d2 = 98765.4321;
        return d1 / d2;
    }

    @Benchmark
    public BigDecimal bigDecimalDivide() {
        BigDecimal bd1 = new BigDecimal("123456.789");
        BigDecimal bd2 = new BigDecimal("98765.4321");
        return bd1.divide(bd2);
    }
}
