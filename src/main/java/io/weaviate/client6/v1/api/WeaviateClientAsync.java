package io.weaviate.client6.v1.api;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import io.weaviate.client6.v1.api.collections.WeaviateCollectionsClientAsync;
import io.weaviate.client6.v1.internal.ObjectBuilder;
import io.weaviate.client6.v1.internal.TokenProvider;
import io.weaviate.client6.v1.internal.grpc.DefaultGrpcTransport;
import io.weaviate.client6.v1.internal.grpc.GrpcChannelOptions;
import io.weaviate.client6.v1.internal.grpc.GrpcTransport;
import io.weaviate.client6.v1.internal.rest.DefaultRestTransport;
import io.weaviate.client6.v1.internal.rest.RestTransport;
import io.weaviate.client6.v1.internal.rest.RestTransportOptions;

public class WeaviateClientAsync implements Closeable {
  private final RestTransport restTransport;
  private final GrpcTransport grpcTransport;

  public final WeaviateCollectionsClientAsync collections;

  /**
   * This constructor is blocking if {@link Authorization} configured,
   * as the client will need to do the initial token exchange.
   */
  public WeaviateClientAsync(Config config) {
    RestTransportOptions restOpt;
    GrpcChannelOptions grpcOpt;
    if (config.authorization() == null) {
      restOpt = config.restTransportOptions();
      grpcOpt = config.grpcTransportOptions();
    } else {
      TokenProvider tokenProvider;
      try (final var noAuthRest = new DefaultRestTransport(config.restTransportOptions())) {
        tokenProvider = config.authorization().getTokenProvider(noAuthRest);
      } catch (IOException e) {
        // Generally IOExceptions are caught in TokenProvider internals.
        // This one may be thrown when noAuthRest transport is auto-closed.
        throw new WeaviateOAuthException(e);
      }
      restOpt = config.restTransportOptions(tokenProvider);
      grpcOpt = config.grpcTransportOptions(tokenProvider);
    }

    this.restTransport = new DefaultRestTransport(restOpt);
    this.grpcTransport = new DefaultGrpcTransport(grpcOpt);

    this.collections = new WeaviateCollectionsClientAsync(restTransport, grpcTransport);
  }

  /**
   * Connect to a local Weaviate instance.
   *
   * <p>
   * This call is blocking if {@link Authorization} configured,
   * as the client will need to do the initial token exchange.
   */
  public static WeaviateClientAsync local() {
    return local(ObjectBuilder.identity());
  }

  /**
   * Connect to a local Weaviate instance.
   *
   * <p>
   * This call is blocking if {@link Authorization} configured,
   * as the client will need to do the initial token exchange.
   */
  public static WeaviateClientAsync local(Function<Config.Local, ObjectBuilder<Config>> fn) {
    return new WeaviateClientAsync(fn.apply(new Config.Local()).build());
  }

  /**
   * Connect to a Weaviate Cloud instance.
   *
   * <p>
   * This call is blocking if {@link Authorization} configured,
   * as the client will need to do the initial token exchange.
   */
  public static WeaviateClientAsync wcd(String httpHost, String apiKey) {
    return wcd(httpHost, apiKey, ObjectBuilder.identity());
  }

  /**
   * Connect to a Weaviate Cloud instance.
   *
   * <p>
   * This call is blocking if {@link Authorization} configured,
   * as the client will need to do the initial token exchange.
   */
  public static WeaviateClientAsync wcd(String httpHost, String apiKey,
      Function<Config.WeaviateCloud, ObjectBuilder<Config>> fn) {
    var config = new Config.WeaviateCloud(httpHost, Authorization.apiKey(apiKey));
    return new WeaviateClientAsync(fn.apply(config).build());
  }

  /**
   * Connect to a Weaviate instance with custom configuration.
   *
   * <p>
   * This call is blocking if {@link Authorization} configured,
   * as the client will need to do the initial token exchange.
   */
  public static WeaviateClientAsync custom(Function<Config.Custom, ObjectBuilder<Config>> fn) {
    return new WeaviateClientAsync(Config.of(fn));
  }

  /** Ping the server for a liveness check. */
  public CompletableFuture<Boolean> isLive() {
    return this.restTransport.performRequestAsync(null, IsLiveRequest._ENDPOINT);
  }

  /** Ping the server for a readiness check. */
  public CompletableFuture<Boolean> isReady() {
    return this.restTransport.performRequestAsync(null, IsReadyRequest._ENDPOINT);
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
