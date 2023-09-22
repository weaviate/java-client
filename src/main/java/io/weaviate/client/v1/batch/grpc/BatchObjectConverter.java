package io.weaviate.client.v1.batch.grpc;

import com.google.protobuf.Struct;
import com.google.protobuf.Value;
import io.weaviate.client.v1.data.model.WeaviateObject;
import io.weaviate.grpc.protocol.WeaviateProtoBase;
import io.weaviate.grpc.protocol.WeaviateProtoBatch;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class BatchObjectConverter {

  public static WeaviateProtoBatch.BatchObject toBatchObject(WeaviateObject obj) {
    WeaviateProtoBatch.BatchObject.Builder builder = WeaviateProtoBatch.BatchObject.newBuilder();
    if (obj.getId() != null) {
      builder.setUuid(obj.getId());
    }
    if (obj.getClassName() != null) {
      builder.setClassName(obj.getClassName());
    }
    if (obj.getVector() != null) {
      builder.addAllVector(Arrays.asList(obj.getVector()));
    }
    if (obj.getTenant() != null) {
      builder.setTenant(obj.getTenant());
    }
    if (obj.getProperties() != null) {
      builder.setProperties(buildProperties(obj.getProperties()));
    }
    return builder.build();
  }

  private static WeaviateProtoBatch.BatchObject.Properties buildProperties(Map<String, Object> properties) {
    WeaviateProtoBatch.BatchObject.Properties.Builder builder = WeaviateProtoBatch.BatchObject.Properties.newBuilder();
    Map<String, Value> nonRefProperties = new HashMap<>();
    for (Map.Entry<String, Object> e : properties.entrySet()) {
      String propName = e.getKey();
      Object propValue = e.getValue();
      if (propName.equals("_lastUpdateTimeUnix") || propName.equals("_creationTimeUnix")) {
        // ignore for now _creationTimeUnix / _lastUpdateTimeUnix
        continue;
      }
      if (propValue instanceof String) {
        nonRefProperties.put(propName, Value.newBuilder().setStringValue((String) propValue).build());
        continue;
      }
      if (propValue instanceof Boolean) {
        nonRefProperties.put(propName, Value.newBuilder().setBoolValue((Boolean) propValue).build());
        continue;
      }
      if (propValue instanceof Integer) {
        nonRefProperties.put(propName, Value.newBuilder().setNumberValue(((Integer) propValue).doubleValue()).build());
        continue;
      }
      if (propValue instanceof Long) {
        nonRefProperties.put(propName, Value.newBuilder().setNumberValue(((Long) propValue).doubleValue()).build());
        continue;
      }
      if (propValue instanceof Float) {
        nonRefProperties.put(propName, Value.newBuilder().setNumberValue(((Float) propValue).doubleValue()).build());
        continue;
      }
      if (propValue instanceof Double) {
        nonRefProperties.put(propName, Value.newBuilder().setNumberValue((Double) propValue).build());
        continue;
      }
      if (propValue instanceof String[]) {
        // TODO: handle ref properties
        WeaviateProtoBase.TextArrayProperties textArrayProperties = WeaviateProtoBase.TextArrayProperties.newBuilder()
          .setPropName(propName).addAllValues(Arrays.asList((String[]) propValue)).build();
        builder.addTextArrayProperties(textArrayProperties);
        continue;
      }
      if (propValue instanceof Boolean[]) {
        WeaviateProtoBase.BooleanArrayProperties booleanArrayProperties = WeaviateProtoBase.BooleanArrayProperties.newBuilder()
          .setPropName(propName).addAllValues(Arrays.asList((Boolean[]) propValue)).build();
        builder.addBooleanArrayProperties(booleanArrayProperties);
        continue;
      }
      if (propValue instanceof Integer[]) {
        List<Long> value = Arrays.stream((Integer[]) propValue).map(Integer::longValue).collect(Collectors.toList());
        WeaviateProtoBase.IntArrayProperties intArrayProperties = WeaviateProtoBase.IntArrayProperties.newBuilder()
          .setPropName(propName).addAllValues(value).build();
        builder.addIntArrayProperties(intArrayProperties);
        continue;
      }
      if (propValue instanceof Long[]) {
        WeaviateProtoBase.IntArrayProperties intArrayProperties = WeaviateProtoBase.IntArrayProperties.newBuilder()
          .setPropName(propName)
          .addAllValues(Arrays.asList((Long[]) propValue))
          .build();
        builder.addIntArrayProperties(intArrayProperties);
        continue;
      }
      if (propValue instanceof Float[]) {
        List<Double> value = Arrays.stream((Float[]) propValue).map(Float::doubleValue).collect(Collectors.toList());
        WeaviateProtoBase.NumberArrayProperties numberArrayProperties = WeaviateProtoBase.NumberArrayProperties.newBuilder()
          .setPropName(propName).addAllValues(value).build();
        builder.addNumberArrayProperties(numberArrayProperties);
        continue;
      }
      if (propValue instanceof Double[]) {
        WeaviateProtoBase.NumberArrayProperties numberArrayProperties = WeaviateProtoBase.NumberArrayProperties.newBuilder()
          .setPropName(propName).addAllValues(Arrays.asList((Double[]) propValue)).build();
        builder.addNumberArrayProperties(numberArrayProperties);
      }
    }
    if (!nonRefProperties.isEmpty()) {
      builder.setNonRefProperties(Struct.newBuilder().putAllFields(nonRefProperties).build());
    }

    return builder.build();
  }
}
