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
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class ShardingConfig {
  Integer actualCount;
  Integer actualVirtualCount;
  Integer desiredCount;
  Integer desiredVirtualCount;
  String function;
  String key;
  String strategy;
  Integer virtualPerPhysical;
}
