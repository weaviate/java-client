package technology.semi.weaviate.client.v1.batch.model;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;
import technology.semi.weaviate.client.v1.data.model.Deprecation;
import technology.semi.weaviate.client.v1.data.model.WeaviateObject;

import java.util.Map;

@Getter
@ToString(callSuper = true)
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class ObjectGetResponse extends WeaviateObject {
  Deprecation[] deprecations;
  ObjectsGetResponseAO2Result result;

  @Builder(builderMethodName = "objectGetResponseBuilder")
  public ObjectGetResponse(String id, String className, Long creationTimeUnix, Long lastUpdateTimeUnix,
                           Map<String, Object> properties, Map<String, Object> additional, Float[] vector,
                           Object vectorWeights, Deprecation[] deprecations, ObjectsGetResponseAO2Result result) {
    super(id, className, creationTimeUnix, lastUpdateTimeUnix, properties, additional, vector, vectorWeights);
    this.deprecations = deprecations;
    this.result = result;
  }
}
