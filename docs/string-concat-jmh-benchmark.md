# Java 字符串拼接性能实测：基于 JMH 的微基准测试

在 Java 开发中，字符串拼接操作无处不在。你可能会直接使用 `+`，也可能选择 `StringBuilder` 或 `StringBuffer`。它们在性能上究竟有何差别？在循环中拼接多个字符串时，哪种方式更高效？

本文基于 JMH（Java Microbenchmark Harness）进行了系统性测试，并使用 GitHub Actions 在 Ubuntu 环境中实测了不同字符串拼接方式的性能。

---

## 🧪 测试目标

比较以下三种拼接方式在高频场景下的性能差异：

1. `+` 操作符（语法糖，编译期转为 `StringBuilder.append`）
2. `StringBuilder.append`
3. `StringBuffer.append`

---

## 🧰 项目创建与配置（Maven）

### 1. 创建基础工程

```bash
mvn archetype:generate \
  -DgroupId=com.xinchentechnote \
  -DartifactId=string-jmh \
  -DarchetypeArtifactId=maven-archetype-quickstart \
  -DinteractiveMode=false
```

### 2. 配置 `pom.xml`

```xml
<dependencies>
  <dependency>
    <groupId>org.openjdk.jmh</groupId>
    <artifactId>jmh-core</artifactId>
    <version>1.37</version>
  </dependency>
  <dependency>
    <groupId>org.openjdk.jmh</groupId>
    <artifactId>jmh-generator-annprocess</artifactId>
    <version>1.37</version>
    <scope>provided</scope>
  </dependency>
</dependencies>

<build>
  <plugins>
    <plugin>
      <groupId>org.apache.maven.plugins</groupId>
      <artifactId>maven-compiler-plugin</artifactId>
      <version>3.8.1</version>
      <configuration>
        <source>17</source>
        <target>17</target>
        <annotationProcessorPaths>
          <path>
            <groupId>org.openjdk.jmh</groupId>
            <artifactId>jmh-generator-annprocess</artifactId>
            <version>1.37</version>
          </path>
        </annotationProcessorPaths>
      </configuration>
    </plugin>
  </plugins>
</build>
```

---

## 📄 测试代码实现

```java
package com.xinchentechnote;

import org.openjdk.jmh.annotations.*;

import java.util.concurrent.TimeUnit;

@BenchmarkMode({Mode.Throughput, Mode.AverageTime})
@OutputTimeUnit(TimeUnit.MICROSECONDS)
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
```

---

## 🚀 GitHub Actions 自动化测试配置

```yaml
name: JMH Benchmarks

on:
  workflow_dispatch:

jobs:
  benchmark:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - uses: actions/setup-java@v3
        with:
          distribution: temurin
          java-version: '17'
          cache: maven
      - run: mvn clean install -DskipTests
      - run: java -jar target/benchmarks.jar StringConcatBenchmark
```

---

## 📊 实测结果（Ubuntu + GitHub Actions）

### 📈 吞吐量测试（Throughput）

单位：每微秒执行的操作数（ops/us）

| 方法              | 吞吐量              |
| --------------- | ---------------- |
| `StringBuilder` | **0.478 ops/us** |
| `StringBuffer`  | 0.448 ops/us     |
| `+` 操作符         | 🚫 0.199 ops/us  |

### ⏱ 平均耗时测试（AverageTime）

单位：每次操作的平均耗时（us/op）

| 方法              | 平均耗时            |
| --------------- | --------------- |
| `StringBuilder` | **2.021 us/op** |
| `StringBuffer`  | 2.237 us/op     |
| `+` 操作符         | 🚫 5.244 us/op  |

---

## 🔍 原理解析

- `StringBuilder`：非线程安全但性能最好，推荐在循环中使用。
- `StringBuffer`：线程安全但性能略低。
- `+` 操作符：虽然直观，但在循环中极其低效，会频繁创建临时对象，带来 GC 压力。

---

## ✅ 使用建议

| 拼接方式          | 优点   | 缺点        | 推荐场景    |
| ------------- | ---- | --------- | ------- |
| StringBuilder | 性能最佳 | 非线程安全     | 单线程高频拼接 |
| StringBuffer  | 线程安全 | 性能略差      | 多线程拼接场景 |
| `+` 操作符       | 简洁直观 | 慢且 GC 压力大 | 低频简易拼接  |

---

## 🏁 总结

本次 JMH 实测验证了开发经验中的最佳实践：**在高频场景中，推荐使用 `StringBuilder` 进行字符串拼接。**

好的！以下是更新后的 JMH 小知识部分，内容更详细，可直接替换博客中的相关段落：

---

### 💡 JMH 小知识：Java 微基准测试利器

[JMH (Java Microbenchmark Harness)](https://openjdk.org/projects/code-tools/jmh/) 是由 Oracle 和 OpenJDK 团队专为 Java 编写的微基准测试框架，用于衡量 Java 方法在纳秒到微秒级别的性能表现。JMH 特别适用于需要精细分析方法调用开销、编译优化、副作用等对性能影响的场景。

#### 核心术语与注解解释：

| 注解或参数             | 含义                                                                                                                         |
| ----------------- | -------------------------------------------------------------------------------------------------------------------------- |
| `@Benchmark`      | 标记要被测试的方法。每次运行都调用它并收集性能数据。                                                                                                 |
| `@BenchmarkMode`  | 设置基准测试的模式（如吞吐量、平均时间等）。可选值包括：- `Throughput`: 单位时间内操作次数- `AverageTime`: 每个操作平均耗时- `SampleTime`, `SingleShotTime`, `AllModes` |
| `@OutputTimeUnit` | 设置输出结果的时间单位，如 `TimeUnit.MILLISECONDS` 或 `MICROSECONDS`。                                                                    |
| `@State`          | 用于管理基准方法所需的状态变量作用域。常用 `Scope.Thread` 表示每线程独立状态。                                                                            |
| `@Fork`           | 设置执行几轮 JVM 启动来规避 JVM 热身阶段的影响，通常设置为 1~3。                                                                                    |
| `@Warmup`         | 预热次数与每次持续时间（JIT 编译优化发生在此阶段）。避免冷启动影响基准数据。                                                                                   |
| `@Measurement`    | 真正采集性能数据的次数和持续时间。建议至少 `5 次 × 1s+`。                                                                                         |

#### 推荐配置参数说明：

```java
@BenchmarkMode({Mode.Throughput, Mode.AverageTime}) // 同时测吞吐量和平均耗时
@OutputTimeUnit(TimeUnit.MICROSECONDS)              // 输出微秒为单位
@State(Scope.Thread)                                // 每个线程独立状态
@Fork(1)                                             // 启动 1 次 JVM
@Warmup(iterations = 5, time = 1)                   // 预热 5 次，每次 1 秒
@Measurement(iterations = 5, time = 1)              // 采样 5 次，每次 1 秒
```

#### 为什么不能简单用 System.currentTimeMillis？

因为 JVM 启动初期 JIT 编译未完成、内联尚未展开、逃逸分析等优化机制尚未介入，初始运行的耗时非常不稳定。而 JMH 通过：

- 自动 warm-up 预热阶段；

- 多轮 fork JVM 隔离优化影响；

- 统计误差和波动范围（Error）；

确保了测得结果更真实、更接近应用实际表现。

---

希望本文能为你在日常开发与性能优化中提供量化参考！