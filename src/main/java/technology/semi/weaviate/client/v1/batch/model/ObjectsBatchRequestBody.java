package technology.semi.weaviate.client.v1.batch.model;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;
import technology.semi.weaviate.client.v1.data.model.WeaviateObject;

@Getter
@Builder
@ToString
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ObjectsBatchRequestBody {
  String[] fields;
  WeaviateObject[] objects;
}
