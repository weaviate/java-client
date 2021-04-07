package technology.semi.weaviate.client.v1.schema.api.model;

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
  List<String> dataType;
  String description;
  Boolean indexInverted;
  Object moduleConfig;
}
