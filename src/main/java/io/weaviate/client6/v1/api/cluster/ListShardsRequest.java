package io.weaviate.client6.v1.api.cluster;

import java.util.HashMap;
import java.util.Optional;
import java.util.function.Function;

import io.weaviate.client6.v1.internal.ObjectBuilder;
import io.weaviate.client6.v1.internal.json.JSON;
import io.weaviate.client6.v1.internal.rest.Endpoint;
import io.weaviate.client6.v1.internal.rest.OptionalEndpoint;

public record ListShardsRequest(String collection, String shard) {

  static final Endpoint<ListShardsRequest, Optional<ShardingState>> _ENDPOINT = OptionalEndpoint.noBodyOptional(
      __ -> "GET",
      __ -> "/replication/sharding-state",
      request -> new HashMap<>() { // `shard` can be null, HashMap permits null values.
        {
          put("collection", request.collection);
          put("shard", request.shard);
        }
      },
      (statusCode, response) -> JSON.deserialize(response, ListShardsResponse.class).shardingState());

  public static ListShardsRequest of(String collection) {
    return of(collection, ObjectBuilder.identity());
  }

  public static ListShardsRequest of(String collection, Function<Builder, ObjectBuilder<ListShardsRequest>> fn) {
    return fn.apply(new Builder(collection)).build();
  }

  public ListShardsRequest(Builder builder) {
    this(builder.collection, builder.shard);
  }

  public static class Builder implements ObjectBuilder<ListShardsRequest> {
    private final String collection;
    private String shard;

    public Builder(String collection) {
      this.collection = collection;
    }

    public Builder shard(String shard) {
      this.shard = shard;
      return this;
    }

    @Override
    public ListShardsRequest build() {
      return new ListShardsRequest(this);
    }
  }
}
