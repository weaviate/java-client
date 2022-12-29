package technology.semi.weaviate.client.v1.batch.model;

import com.google.gson.annotations.SerializedName;
import java.util.Map;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;
import technology.semi.weaviate.client.v1.data.model.Deprecation;

@Getter
@Setter
@ToString
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

  Deprecation[] deprecations;
  ObjectsGetResponseAO2Result result;
}
