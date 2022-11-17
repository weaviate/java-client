package technology.semi.weaviate.client.v1.misc.model;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

@Builder
@Getter
@ToString
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class BM25Config {
  Float k1;
  Float b;
}
