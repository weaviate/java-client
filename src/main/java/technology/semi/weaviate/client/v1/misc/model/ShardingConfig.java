package technology.semi.weaviate.client.v1.misc.model;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@Builder
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
