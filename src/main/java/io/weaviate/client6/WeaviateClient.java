package io.weaviate.client6;

import java.io.Closeable;
import java.io.IOException;

import io.weaviate.client6.internal.GrpcClient;
import io.weaviate.client6.internal.HttpClient;
import io.weaviate.client6.v1.collections.Collections;

public class WeaviateClient implements Closeable {
  private final HttpClient http;
  private final GrpcClient grpc;

  public final Collections collections;

  public WeaviateClient(Config config) {
    this.http = new HttpClient();
    this.grpc = new GrpcClient(config);
    this.collections = new Collections(config, http, grpc);
  }

  @Override
  public void close() throws IOException {
    this.http.close();
    this.grpc.close();
  }
}
