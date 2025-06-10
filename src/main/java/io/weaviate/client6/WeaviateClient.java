package io.weaviate.client6;

import java.io.Closeable;
import java.io.IOException;

import io.weaviate.client6.internal.GrpcClient;
import io.weaviate.client6.internal.HttpClient;
import io.weaviate.client6.v1.collections.CollectionsClient;
import io.weaviate.client6.v1.internal.grpc.DefaultGrpcTransport;
import io.weaviate.client6.v1.internal.grpc.GrpcTransport;

public class WeaviateClient implements Closeable {
  private final HttpClient http;
  private final GrpcClient grpc;

  public final CollectionsClient collections;
  public final io.weaviate.client6.v1.api.WeaviateClient apiClient;

  private final GrpcTransport grpcTransport;

  public WeaviateClient(Config config) {
    this.http = new HttpClient();
    this.grpc = new GrpcClient(config);

    this.grpcTransport = new DefaultGrpcTransport(config);
    this.collections = new CollectionsClient(config, http, grpc, grpcTransport);
    this.apiClient = new io.weaviate.client6.v1.api.WeaviateClient(
        new io.weaviate.client6.v1.api.Config(config.scheme, config.httpHost, config.grpcHost));

  }

  public io.weaviate.client6.v1.api.WeaviateClient apiClient() {
    return this.apiClient;
  }

  @Override
  public void close() throws IOException {
    this.http.close();
    this.grpc.close();

    this.grpcTransport.close();
  }
}
