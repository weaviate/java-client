package io.weaviate.client6.v1.collections;

import java.util.HashMap;
import java.util.Map;

public record NoneVectorIndex(Object vectorizer, String indexType, Object indexConfiguration)
    implements CollectionDefinition.VectorConfig {

  public NoneVectorIndex() {
    this(Map.of("none", new Object()), "flat", new HashMap<>());
  }
}
