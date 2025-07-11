package io.weaviate.client.v1.cluster.api;

import java.util.List;

import com.google.gson.annotations.SerializedName;

import lombok.Getter;

@Getter
public class ShardReplicas {
  @SerializedName("shard")
  String name;
  @SerializedName("replicas")
  List<String> replicas;
}
