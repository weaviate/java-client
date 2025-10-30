package io.weaviate.client6.v1.api.cluster.replication;

import java.util.Collections;

import com.google.gson.annotations.SerializedName;

import io.weaviate.client6.v1.internal.json.JSON;
import io.weaviate.client6.v1.internal.rest.Endpoint;
import io.weaviate.client6.v1.internal.rest.SimpleEndpoint;

public record CreateReplicationRequest(
    @SerializedName("collection") String collection,
    @SerializedName("shard") String shard,
    @SerializedName("sourceNode") String sourceNode,
    @SerializedName("targetNode") String targetNode,
    @SerializedName("type") ReplicationType type) {

  public static final Endpoint<CreateReplicationRequest, Replication> _ENDPOINT = new SimpleEndpoint<>(
      request -> "POST",
      request -> "/replication/replicate",
      request -> Collections.emptyMap(),
      request -> JSON.serialize(request),
      (__, response) -> JSON.deserialize(response, Replication.class));
}
