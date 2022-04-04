package technology.semi.weaviate.client.v1.misc.model;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Builder
@Getter
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class StopwordConfig {
  String preset;
  String[] additions;
  String[] removals;
}
