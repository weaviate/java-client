package technology.semi.weaviate.client.v1.data.model;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ObjectsResponse {
  Object deprecations;
  Object[] objects;
  int totalResults;
}
