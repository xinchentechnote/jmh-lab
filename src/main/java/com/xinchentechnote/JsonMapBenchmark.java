package com.xinchentechnote;

import com.dslplatform.json.DslJson;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import io.avaje.jsonb.Jsonb;
import org.openjdk.jmh.annotations.*;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.Throughput) // 每秒操作数
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Thread)
public class JsonMapBenchmark {

    private Map<String, Integer> mapData;
    private byte[] jacksonBytes;
    private byte[] gsonBytes;
    private byte[] dslJsonBytes;
    private byte[] avajeBytes;

    private ObjectMapper jackson;
    private Gson gson;
    private DslJson<Object> dslJson;

    @Setup(Level.Trial)
    public void setup() throws Exception {
        // 初始化数据
        mapData = new HashMap<>();
        mapData.put("1", 100);
        mapData.put("2", 200);

        // 初始化 JSON 库
        jackson = new ObjectMapper();
        gson = new Gson();
        dslJson = new DslJson<>();

        // 预序列化数据
        jacksonBytes = jackson.writeValueAsBytes(mapData);
        gsonBytes = gson.toJson(mapData).getBytes(StandardCharsets.UTF_8);

        ByteArrayOutputStream dslOut = new ByteArrayOutputStream();
        dslJson.serialize(mapData, dslOut);
        dslJsonBytes = dslOut.toByteArray();

    }

    // Jackson
    @Benchmark
    public byte[] jacksonSerialize() throws Exception {
        return jackson.writeValueAsBytes(mapData);
    }

    @Benchmark
    public Map<Integer, Integer> jacksonDeserialize() throws Exception {
        return jackson.readValue(jacksonBytes, Map.class);
    }

    // Gson
    @Benchmark
    public byte[] gsonSerialize() {
        return gson.toJson(mapData).getBytes(StandardCharsets.UTF_8);
    }

    @Benchmark
    public Map<Integer, Integer> gsonDeserialize() {
        return gson.fromJson(new String(gsonBytes, StandardCharsets.UTF_8), Map.class);
    }

    // DSL-JSON
    @Benchmark
    public byte[] dslJsonSerialize() throws Exception {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        dslJson.serialize(mapData, os);
        return os.toByteArray();
    }

    @Benchmark
    public Map<Integer, Integer> dslJsonDeserialize() throws Exception {
        return dslJson.deserialize(Map.class, dslJsonBytes, dslJsonBytes.length);
    }

}