package technology.semi.weaviate.client.v1.batch.model;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import technology.semi.weaviate.client.v1.data.model.Object;

@Getter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ObjectsBatchRequestBody {
  String[] fields;
  Object[] objects;
}
