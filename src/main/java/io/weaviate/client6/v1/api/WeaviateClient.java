package io.weaviate.client6.v1.api;

import java.io.Closeable;
import java.io.IOException;
import java.util.function.Function;

import io.weaviate.client6.v1.api.collections.WeaviateCollectionsClient;
import io.weaviate.client6.v1.internal.ObjectBuilder;
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
    this.restTransport = new DefaultRestTransport(config.restTransportOptions());
    this.grpcTransport = new DefaultGrpcTransport(config.grpcTransportOptions());

    this.collections = new WeaviateCollectionsClient(restTransport, grpcTransport);
  }

  public WeaviateClientAsync async() {
    return new WeaviateClientAsync(config);
  }

  public static WeaviateClient local() {
    return local(ObjectBuilder.identity());
  }

  public static WeaviateClient local(Function<Config.Builder, ObjectBuilder<Config>> fn) {
    var config = new Config.Builder("http", "localhost:8080")
        .grpcHost("locahost:50051");
    return new WeaviateClient(fn.apply(config).build());
  }

  public static WeaviateClient wcd(String clusterUrl, String apiKey) {
    return wcd(clusterUrl, apiKey, ObjectBuilder.identity());
  }

  public static WeaviateClient wcd(String clusterUrl, String apiKey,
      Function<Config.Builder, ObjectBuilder<Config>> fn) {
    var config = new Config.Builder(clusterUrl)
        .grpcPrefix("grpc-")
        .authorization(Authorization.apiKey(apiKey));
    return new WeaviateClient(fn.apply(config).build());
  }

  @Override
  public void close() throws IOException {
    this.restTransport.close();
    this.grpcTransport.close();
  }
}
