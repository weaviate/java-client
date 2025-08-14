package io.weaviate.client6.v1.api;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import io.weaviate.client6.v1.api.alias.WeaviateAliasClientAsync;
import io.weaviate.client6.v1.api.collections.WeaviateCollectionsClient;
import io.weaviate.client6.v1.api.collections.WeaviateCollectionsClientAsync;
import io.weaviate.client6.v1.internal.ObjectBuilder;
import io.weaviate.client6.v1.internal.grpc.DefaultGrpcTransport;
import io.weaviate.client6.v1.internal.grpc.GrpcTransport;
import io.weaviate.client6.v1.internal.rest.DefaultRestTransport;
import io.weaviate.client6.v1.internal.rest.RestTransport;

public class WeaviateClientAsync implements Closeable {
  private final RestTransport restTransport;
  private final GrpcTransport grpcTransport;

  /**
   * Client for {@code /schema} endpoints for managing Weaviate collections.
   * See {@link WeaviateCollectionsClient#use} for populating and querying
   * collections.
   */
  public final WeaviateCollectionsClientAsync collections;

  /** Client for {@code /aliases} endpoints for managing collection aliases. */
  public final WeaviateAliasClientAsync alias;

  public WeaviateClientAsync(Config config) {
    this.restTransport = new DefaultRestTransport(config.restTransportOptions());
    this.grpcTransport = new DefaultGrpcTransport(config.grpcTransportOptions());

    this.alias = new WeaviateAliasClientAsync(restTransport);
    this.collections = new WeaviateCollectionsClientAsync(restTransport, grpcTransport);
  }

  /** Connect to a local Weaviate instance. */
  public static WeaviateClientAsync local() {
    return local(ObjectBuilder.identity());
  }

  /** Connect to a local Weaviate instance. */
  public static WeaviateClientAsync local(Function<Config.Local, ObjectBuilder<Config>> fn) {
    return new WeaviateClientAsync(fn.apply(new Config.Local()).build());
  }

  /** Connect to a Weaviate Cloud instance. */
  public static WeaviateClientAsync wcd(String httpHost, String apiKey) {
    return wcd(httpHost, apiKey, ObjectBuilder.identity());
  }

  /** Connect to a Weaviate Cloud instance. */
  public static WeaviateClientAsync wcd(String httpHost, String apiKey,
      Function<Config.WeaviateCloud, ObjectBuilder<Config>> fn) {
    var config = new Config.WeaviateCloud(httpHost, Authorization.apiKey(apiKey));
    return new WeaviateClientAsync(fn.apply(config).build());
  }

  /** Connect to a Weaviate instance with custom configuration. */
  public static WeaviateClientAsync custom(Function<Config.Custom, ObjectBuilder<Config>> fn) {
    return new WeaviateClientAsync(Config.of(fn));
  }

  /** Ping the server for a liveness check. */
  public CompletableFuture<Boolean> isLive() {
    return this.restTransport.performRequestAsync(null, IsLiveRequest._ENDPOINT);
  }

  /**
   * Close {@link #restTransport} and {@link #grpcTransport}
   * and release associated resources.
   */
  @Override
  public void close() throws IOException {
    this.restTransport.close();
    this.grpcTransport.close();
  }
}
