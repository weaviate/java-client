package io.weaviate.client.v1.misc.model;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

@Getter
@Builder
@ToString
@EqualsAndHashCode
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MultiVectorConfig {
  @Builder.Default
  private boolean enabled = true;
  @Builder.Default
  private Aggregation aggregation = Aggregation.MAX_SIM;

  public enum Aggregation {
    MAX_SIM;
  }
}
