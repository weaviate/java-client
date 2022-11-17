package technology.semi.weaviate.client.v1.batch.model;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;
import technology.semi.weaviate.client.v1.data.model.Deprecation;
import technology.semi.weaviate.client.v1.data.model.WeaviateObject;

@Getter
@Setter
@ToString
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ObjectGetResponse {
  Deprecation[] deprecations;
  WeaviateObject object;
  ObjectsGetResponseAO2Result result;
}
