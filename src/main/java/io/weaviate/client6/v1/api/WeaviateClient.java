package io.weaviate.client6.v1.api;

import java.io.Closeable;
import java.io.IOException;

import io.weaviate.client6.v1.api.collections.WeaviateCollectionsClient;
import io.weaviate.client6.v1.internal.grpc.DefaultGrpcTransport;
import io.weaviate.client6.v1.internal.grpc.GrpcTransport;
import io.weaviate.client6.v1.internal.rest.DefaultRestTransport;
import io.weaviate.client6.v1.internal.rest.RestTransport;

public class WeaviateClient implements Closeable {
  /** Store this for {@link #async()} helper. */
  private final Config config;

  private final RestTransport restTransport;
  private final GrpcTransport grpcTransport;

  public final WeaviateCollectionsClient collections;

  public WeaviateClient(Config config) {
    this.config = config;
    this.restTransport = new DefaultRestTransport(config.rest());
    this.grpcTransport = new DefaultGrpcTransport(config.grpc());

    this.collections = new WeaviateCollectionsClient(restTransport, grpcTransport);
  }

  public WeaviateClientAsync async() {
    return new WeaviateClientAsync(config);
  }

  @Override
  public void close() throws IOException {
    this.restTransport.close();
    this.grpcTransport.close();
  }
}
