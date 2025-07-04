package com.xinchentechnote;

import org.openjdk.jmh.annotations.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Thread)
public class DoubleVsBigDecimalBenchmark {

    private double d1 = 123456.789;
    private double d2 = 98765.4321;
    private double d3 = 0.5;

    private BigDecimal bd1 = new BigDecimal("123456.789");
    private BigDecimal bd2 = new BigDecimal("98765.4321");
    private BigDecimal bd3 = new BigDecimal("0.5");

    @Benchmark
    public double doubleCal() {
        return d1 + (d2 - d1 / d2) * d3;
    }

    @Benchmark
    public BigDecimal bigDecimalCal() {
        // bd1 + (bd2 - bd1 / bd2) * bd3
        return bd1.add((bd2.subtract(bd1.divide(bd2, 8, RoundingMode.HALF_UP))).multiply(bd3));
    }

}
