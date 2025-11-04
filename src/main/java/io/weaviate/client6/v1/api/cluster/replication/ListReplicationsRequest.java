package io.weaviate.client6.v1.api.cluster.replication;

import java.util.HashMap;
import java.util.List;
import java.util.function.Function;

import io.weaviate.client6.v1.internal.ObjectBuilder;
import io.weaviate.client6.v1.internal.json.JSON;
import io.weaviate.client6.v1.internal.rest.Endpoint;
import io.weaviate.client6.v1.internal.rest.SimpleEndpoint;

public record ListReplicationsRequest(String collection, String shard, String targetNode, boolean includeHistory) {

  static final Endpoint<ListReplicationsRequest, List<Replication>> _ENDPOINT = SimpleEndpoint.noBody(
      request -> "GET",
      request -> "/replication/replicate/list",
      request -> new HashMap<>() {
        {
          put("collection", request.collection);
          put("shard", request.shard);
          put("targetNode", request.targetNode);
          put("includeHistory", request.includeHistory);
        }
      },
      (__, response) -> JSON.deserializeList(response, Replication.class));

  public static ListReplicationsRequest of() {
    return of(ObjectBuilder.identity());
  }

  public static ListReplicationsRequest of(Function<Builder, ObjectBuilder<ListReplicationsRequest>> fn) {
    return fn.apply(new Builder()).build();
  }

  public ListReplicationsRequest(Builder builder) {
    this(builder.collection, builder.shard, builder.targetNode, builder.includeHistory);
  }

  public static class Builder implements ObjectBuilder<ListReplicationsRequest> {
    private String collection;
    private String shard;
    private String targetNode;
    private boolean includeHistory = false;

    public Builder collection(String collection) {
      this.collection = collection;
      return this;
    }

    public Builder shard(String shard) {
      this.shard = shard;
      return this;
    }

    public Builder targetNode(String targetNode) {
      this.targetNode = targetNode;
      return this;
    }

    /**
     * Include history of statuses for this replication.
     *
     * @see Replication#history
     */
    public Builder includeHistory(boolean includeHistory) {
      this.includeHistory = includeHistory;
      return this;
    }

    @Override
    public ListReplicationsRequest build() {
      return new ListReplicationsRequest(this);
    }
  }

}
