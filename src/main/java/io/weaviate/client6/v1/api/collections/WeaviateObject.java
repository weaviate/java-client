package io.weaviate.client6.v1.api.collections;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

import io.weaviate.client6.v1.api.collections.data.WriteWeaviateObject;
import io.weaviate.client6.v1.api.collections.query.QueryMetadata;
import io.weaviate.client6.v1.internal.ObjectBuilder;

public interface WeaviateObject<PropertiesT> {
  String uuid();

  String collection();

  Vectors vectors();

  String tenant();

  PropertiesT properties();

  Map<String, List<IReference>> references();

  Long createdAt();

  Long lastUpdatedAt();

  QueryMetadata queryMetadata();

  public static <PropertiesT> WeaviateObject<PropertiesT> write(
      Function<WriteWeaviateObject.Builder<PropertiesT>, ObjectBuilder<WriteWeaviateObject<PropertiesT>>> fn) {
    return fn.apply(new WriteWeaviateObject.Builder<>()).build();
  }
}
