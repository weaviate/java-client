package io.weaviate.client6.v1.api.collections.config;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.google.gson.reflect.TypeToken;

import io.weaviate.client6.v1.api.collections.CollectionHandleDefaults;
import io.weaviate.client6.v1.internal.json.JSON;
import io.weaviate.client6.v1.internal.orm.CollectionDescriptor;
import io.weaviate.client6.v1.internal.rest.Endpoint;
import io.weaviate.client6.v1.internal.rest.SimpleEndpoint;

public record GetShardsRequest() {

  @SuppressWarnings("unchecked")
  public static final Endpoint<Void, List<Shard>> endpoint(
      CollectionDescriptor<?> collection,
      CollectionHandleDefaults defaults) {
    return SimpleEndpoint.noBody(
        request -> "GET",
        request -> "/schema/" + collection.collectionName() + "/shards",
        request -> defaults.tenant() != null
            ? Map.of("tenant", defaults.tenant())
            : Collections.emptyMap(),
        (statusCode, response) -> (List<Shard>) JSON.deserialize(response, TypeToken.getParameterized(
            List.class, Shard.class)));
  }
}
