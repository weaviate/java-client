package io.weaviate.client.v1.batch.model;

import com.google.gson.annotations.SerializedName;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;
import io.weaviate.client.v1.data.model.Deprecation;

import java.util.Map;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ObjectGetResponse {
  String id;
  @SerializedName("class")
  String className;
  Long creationTimeUnix;
  Long lastUpdateTimeUnix;
  Map<String, Object> properties;
  Map<String, Object> additional;
  Float[] vector;
  Object vectorWeights;
  String tenant;

  Deprecation[] deprecations;
  ObjectsGetResponseAO2Result result;
}
