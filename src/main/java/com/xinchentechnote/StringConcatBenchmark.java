package com.xinchentechnote;

import org.openjdk.jmh.annotations.*;

import java.util.concurrent.TimeUnit;

@BenchmarkMode({Mode.Throughput, Mode.AverageTime})
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Thread)
public class StringConcatBenchmark {

    private String str1 = "Hello";
    private String str2 = "World";
    private String str3 = "Java";

    @Benchmark
    public String testStringBuilder() {
        StringBuilder sb = new StringBuilder();
        sb.append(str1);
        sb.append(str2);
        sb.append(str3);
        return sb.toString();
    }

    @Benchmark
    public String testStringBuffer() {
        StringBuffer sb = new StringBuffer();
        sb.append(str1);
        sb.append(str2);
        sb.append(str3);
        return sb.toString();
    }

    @Benchmark
    public String testStringPlus() {
        return str1 + str2 + str3;
    }
}
