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
    private int count = 100;

    @Benchmark
    public String testStringBuilder() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < count; i++) {
            sb.append(str1);
            sb.append(str2);
            sb.append(str3);
        }
        return sb.toString();
    }

    @Benchmark
    public String testStringBuffer() {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < count; i++) {
            sb.append(str1);
            sb.append(str2);
            sb.append(str3);
        }
        return sb.toString();
    }

    @Benchmark
    public String testStringPlus() {
        String result = "";
        for (int i = 0; i < count; i++) {
            result += str1;
            result += str2;
            result += str3;
        }
        return result;
    }
}
