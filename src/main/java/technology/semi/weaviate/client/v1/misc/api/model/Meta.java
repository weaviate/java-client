package technology.semi.weaviate.client.v1.misc.api.model;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Meta {
  String hostname;
  String version;
  Object modules;
}
