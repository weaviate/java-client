package technology.semi.weaviate.client.v1.data.model;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@Builder
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class ObjectsListResponse {
  Deprecation[] deprecations;
  WeaviateObject[] objects;
  int totalResults;
}
