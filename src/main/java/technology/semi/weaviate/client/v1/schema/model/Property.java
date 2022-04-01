package technology.semi.weaviate.client.v1.schema.model;

import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@Builder
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class Property {
  String name;
  List<String> dataType;
  String description;
  String tokenization;
  Boolean indexInverted;
  Object moduleConfig;
}
