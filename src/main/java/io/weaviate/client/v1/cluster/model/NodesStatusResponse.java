package io.weaviate.client.v1.cluster.model;

import com.google.gson.annotations.SerializedName;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

@Getter
@ToString
@EqualsAndHashCode
@FieldDefaults(level = AccessLevel.PRIVATE)
public class NodesStatusResponse {

  NodeStatus[] nodes;


  @Getter
  @ToString
  @EqualsAndHashCode
  @FieldDefaults(level = AccessLevel.PRIVATE)
  public static class NodeStatus {

    String name;
    String status;
    String version;
    String gitHash;
    Stats stats;
    ShardStatus[] shards;
  }

  @Getter
  @ToString
  @EqualsAndHashCode
  @FieldDefaults(level = AccessLevel.PRIVATE)
  public static class Stats {

    Long shardCount;
    Long objectCount;
  }

  @Getter
  @ToString
  @EqualsAndHashCode
  @FieldDefaults(level = AccessLevel.PRIVATE)
  public static class ShardStatus {

    String name;
    @SerializedName("class")
    String className;
    Long objectCount;
  }

  public interface Status {

    String HEALTHY = "HEALTHY";
    String UNHEALTHY = "UNHEALTHY";
    String UNAVAILABLE = "UNAVAILABLE";
  }
}
