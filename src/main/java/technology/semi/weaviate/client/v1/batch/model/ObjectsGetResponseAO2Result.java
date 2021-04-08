package technology.semi.weaviate.client.v1.batch.model;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ObjectsGetResponseAO2Result {
  Object errors;
  String status;
}
