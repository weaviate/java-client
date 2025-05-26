package io.weaviate.client6.v1.api.collections;

import java.util.Map;

import io.weaviate.client6.v1.internal.grpc.GrpcTransport;
import io.weaviate.client6.v1.internal.orm.CollectionDescriptor;
import io.weaviate.client6.v1.internal.rest.RestTransport;

public class WeaviateCollectionsClient {
  private final RestTransport restTransport;
  private final GrpcTransport grpcTransport;

  public WeaviateCollectionsClient(RestTransport restTransport, GrpcTransport grpcTransport) {
    this.restTransport = restTransport;
    this.grpcTransport = grpcTransport;
  }

  public WeaviateCollectionClient<Map<String, Object>> use(String collectionName) {
    return new WeaviateCollectionClient<>(restTransport, grpcTransport, CollectionDescriptor.ofMap(collectionName));
  }
}
