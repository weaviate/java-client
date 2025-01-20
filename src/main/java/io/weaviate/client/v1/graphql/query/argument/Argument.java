package io.weaviate.client.v1.graphql.query.argument;

import io.weaviate.client.grpc.protocol.v1.WeaviateProtoSearchGet.SearchRequest;

public interface Argument {
  String build();

  default void addToSearch(SearchRequest.Builder search) {
  }
}
