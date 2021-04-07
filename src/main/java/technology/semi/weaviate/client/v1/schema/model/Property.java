package technology.semi.weaviate.client.v1.schema.model;

import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Property {
  String name;
  List<DataType> dataType;
  String description;
  Boolean indexInverted;
  Object moduleConfig;
}
