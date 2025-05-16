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

  private final GrpcTransport grpcTransport;

  public WeaviateClient(Config config) {
    this.http = new HttpClient();
    this.grpc = new GrpcClient(config);

    this.grpcTransport = new DefaultGrpcTransport(config);

    this.collections = new CollectionsClient(config, http, grpc, grpcTransport);
  }

  @Override
  public void close() throws IOException {
    this.http.close();
    this.grpc.close();

    this.grpcTransport.close();
  }
}
