package io.weaviate.client.v1.data.model;

import com.google.gson.annotations.SerializedName;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

import java.util.Map;

@Getter
@Setter
@Builder
@ToString
@EqualsAndHashCode
@FieldDefaults(level = AccessLevel.PRIVATE)
public class WeaviateObject {
  String id;
  @SerializedName("class")
  String className;
  Long creationTimeUnix;
  Long lastUpdateTimeUnix;
  Map<String, Object> properties;
  Map<String, Object> additional;
  Float[] vector;
  Map<String, Float[]> vectors;
  Object vectorWeights;
  String tenant;
}
