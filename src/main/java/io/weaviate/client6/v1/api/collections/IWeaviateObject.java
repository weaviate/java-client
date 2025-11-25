package io.weaviate.client6.v1.api.collections;

import java.util.List;
import java.util.Map;

import io.weaviate.client6.v1.api.collections.query.QueryMetadata;

public interface IWeaviateObject<PropertiesT> {
  String uuid();

  String collection();

  Vectors vectors();

  String tenant();

  PropertiesT properties();

  Map<String, List<IReference>> references();

  Long createdAt();

  Long lastUpdatedAt();

  QueryMetadata queryMetadata();
}
