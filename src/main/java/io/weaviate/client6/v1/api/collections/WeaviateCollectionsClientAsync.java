package io.weaviate.client6.v1.api.collections;

import java.util.Map;

import io.weaviate.client6.v1.internal.grpc.GrpcTransport;
import io.weaviate.client6.v1.internal.orm.CollectionDescriptor;
import io.weaviate.client6.v1.internal.rest.RestTransport;

public class WeaviateCollectionsClientAsync {
  private final RestTransport restTransport;
  private final GrpcTransport grpcTransport;

  public WeaviateCollectionsClientAsync(RestTransport restTransport, GrpcTransport grpcTransport) {
    this.restTransport = restTransport;
    this.grpcTransport = grpcTransport;
  }

  public WeaviateCollectionClientAsync<Map<String, Object>> use(String collectionName) {
    return new WeaviateCollectionClientAsync<>(restTransport, grpcTransport,
        CollectionDescriptor.ofMap(collectionName));
  }
}
