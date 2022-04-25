package technology.semi.weaviate.client.v1.misc.model;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@Builder
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class InvertedIndexConfig {
  BM25Config bm25;
  StopwordConfig stopwords;
  Integer cleanupIntervalSeconds;
  Boolean indexTimestamps;
}
