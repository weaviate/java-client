package io.weaviate.client6.v1.api;

import java.io.Closeable;
import java.io.IOException;

import io.weaviate.client6.v1.api.collections.WeaviateCollectionsClientAsync;
import io.weaviate.client6.v1.internal.grpc.DefaultGrpcTransport;
import io.weaviate.client6.v1.internal.grpc.GrpcTransport;
import io.weaviate.client6.v1.internal.rest.DefaultRestTransport;
import io.weaviate.client6.v1.internal.rest.RestTransport;

public class WeaviateClientAsync implements Closeable {
  private final RestTransport restTransport;
  private final GrpcTransport grpcTransport;

  public final WeaviateCollectionsClientAsync collections;

  public WeaviateClientAsync(Config config) {
    this.restTransport = new DefaultRestTransport(config.rest());
    this.grpcTransport = new DefaultGrpcTransport(config.grpc());

    this.collections = new WeaviateCollectionsClientAsync(restTransport, grpcTransport);
  }

  @Override
  public void close() throws IOException {
    this.restTransport.close();
    this.grpcTransport.close();
  }
}
