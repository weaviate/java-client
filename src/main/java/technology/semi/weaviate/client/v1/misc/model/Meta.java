package technology.semi.weaviate.client.v1.misc.model;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Meta {
  String hostname;
  String version;
  Object modules;
}
