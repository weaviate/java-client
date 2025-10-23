package io.weaviate.client6.v1.api.cluster;

import java.util.HashMap;
import java.util.List;
import java.util.function.Function;

import io.weaviate.client6.v1.internal.ObjectBuilder;
import io.weaviate.client6.v1.internal.json.JSON;
import io.weaviate.client6.v1.internal.rest.Endpoint;
import io.weaviate.client6.v1.internal.rest.SimpleEndpoint;

public record ListNodesRequest(String collection, String shard, NodeVerbosity verbosity) {

  static final Endpoint<ListNodesRequest, List<Node>> _ENDPOINT = SimpleEndpoint.noBody(
      __ -> "GET",
      request -> "/nodes" + (request.collection != null
          ? "/" + request.collection
          : ""),
      request -> new HashMap<>() { // HashMap permits null values.
        {
          put("shardName", request.shard);
          put("output", request.verbosity);
        }
      },
      (statusCode, response) -> JSON.deserialize(response, ListNodesResponse.class).nodes());

  public static ListNodesRequest of() {
    return of(ObjectBuilder.identity());
  }

  public static ListNodesRequest of(Function<Builder, ObjectBuilder<ListNodesRequest>> fn) {
    return fn.apply(new Builder()).build();
  }

  public ListNodesRequest(Builder builder) {
    this(builder.collection, builder.shard, builder.verbosity);
  }

  public static class Builder implements ObjectBuilder<ListNodesRequest> {
    private String collection;
    private String shard;
    private NodeVerbosity verbosity;

    public Builder collection(String collection) {
      this.collection = collection;
      return this;
    }

    public Builder shard(String shard) {
      this.shard = shard;
      return this;
    }

    public Builder verbosity(NodeVerbosity verbosity) {
      this.verbosity = verbosity;
      return this;
    }

    @Override
    public ListNodesRequest build() {
      return new ListNodesRequest(this);
    }
  }
}
