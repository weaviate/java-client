package io.weaviate.client.v1.misc.model;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

@Getter
@Builder
@ToString
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class VectorIndexConfig {
  String distance;
  Integer ef;
  Integer efConstruction;
  Integer maxConnections;
  Integer dynamicEfMin;
  Integer dynamicEfMax;
  Integer dynamicEfFactor;
  Long vectorCacheMaxObjects;
  Integer flatSearchCutoff;
  Integer cleanupIntervalSeconds;
  Boolean skip;
  PQConfig pq;
}
