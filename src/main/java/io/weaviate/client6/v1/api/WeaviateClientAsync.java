package io.weaviate.client6.v1.api;

import java.io.Closeable;
import java.io.IOException;
import java.util.function.Function;

import io.weaviate.client6.v1.api.collections.WeaviateCollectionsClientAsync;
import io.weaviate.client6.v1.internal.ObjectBuilder;
import io.weaviate.client6.v1.internal.grpc.DefaultGrpcTransport;
import io.weaviate.client6.v1.internal.grpc.GrpcTransport;
import io.weaviate.client6.v1.internal.rest.DefaultRestTransport;
import io.weaviate.client6.v1.internal.rest.RestTransport;

public class WeaviateClientAsync implements Closeable {
  private final RestTransport restTransport;
  private final GrpcTransport grpcTransport;

  public final WeaviateCollectionsClientAsync collections;

  public WeaviateClientAsync(Config config) {
    this.restTransport = new DefaultRestTransport(config.restTransportOptions());
    this.grpcTransport = new DefaultGrpcTransport(config.grpcTransportOptions());

    this.collections = new WeaviateCollectionsClientAsync(restTransport, grpcTransport);
  }

  public static WeaviateClientAsync local() {
    return local(ObjectBuilder.identity());
  }

  public static WeaviateClientAsync local(Function<Config.Builder, ObjectBuilder<Config>> fn) {
    var config = new Config.Builder("http", "localhost:8080")
        .grpcHost("locahost:50051");
    return new WeaviateClientAsync(fn.apply(config).build());
  }

  public static WeaviateClientAsync wcd(String clusterUrl, String apiKey) {
    return wcd(clusterUrl, apiKey, ObjectBuilder.identity());
  }

  public static WeaviateClientAsync wcd(String clusterUrl, String apiKey,
      Function<Config.Builder, ObjectBuilder<Config>> fn) {
    var config = new Config.Builder(clusterUrl)
        .grpcPrefix("grpc-")
        .authorization(Authorization.apiKey(apiKey));
    return new WeaviateClientAsync(fn.apply(config).build());
  }

  @Override
  public void close() throws IOException {
    this.restTransport.close();
    this.grpcTransport.close();
  }
}
