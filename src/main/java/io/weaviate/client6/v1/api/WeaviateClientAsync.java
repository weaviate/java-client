package io.weaviate.client6.v1.api;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import io.weaviate.client6.v1.api.alias.WeaviateAliasClientAsync;
import io.weaviate.client6.v1.api.collections.WeaviateCollectionsClient;
import io.weaviate.client6.v1.api.collections.WeaviateCollectionsClientAsync;
import io.weaviate.client6.v1.internal.ObjectBuilder;
import io.weaviate.client6.v1.internal.TokenProvider;
import io.weaviate.client6.v1.internal.grpc.DefaultGrpcTransport;
import io.weaviate.client6.v1.internal.grpc.GrpcChannelOptions;
import io.weaviate.client6.v1.internal.grpc.GrpcTransport;
import io.weaviate.client6.v1.internal.rest.DefaultRestTransport;
import io.weaviate.client6.v1.internal.rest.RestTransport;
import io.weaviate.client6.v1.internal.rest.RestTransportOptions;

public class WeaviateClientAsync implements AutoCloseable {
  private final RestTransport restTransport;
  private final GrpcTransport grpcTransport;

  /**
   * Client for {@code /schema} endpoints for managing Weaviate collections.
   * See {@link WeaviateCollectionsClient#use} for populating and querying
   * collections.
   */
  public final WeaviateCollectionsClientAsync collections;

  /**
   * This constructor is blocking if {@link Authentication} configured,
   * as the client will need to do the initial token exchange.
   */
  public WeaviateClientAsync(Config config) {
    RestTransportOptions restOpt;
    GrpcChannelOptions grpcOpt;
    if (config.authentication() == null) {
      restOpt = config.restTransportOptions();
      grpcOpt = config.grpcTransportOptions();
    } else {
      TokenProvider tokenProvider;
      try (final var noAuthRest = new DefaultRestTransport(config.restTransportOptions())) {
        tokenProvider = config.authentication().getTokenProvider(noAuthRest);
      } catch (Exception e) {
        // Generally IOExceptions are caught in TokenProvider internals.
        // This one may be thrown when noAuthRest transport is auto-closed.
        throw new WeaviateOAuthException(e);
      }
      restOpt = config.restTransportOptions(tokenProvider);
      grpcOpt = config.grpcTransportOptions(tokenProvider);
    }

    this.restTransport = new DefaultRestTransport(restOpt);
    this.grpcTransport = new DefaultGrpcTransport(grpcOpt);

    this.alias = new WeaviateAliasClientAsync(restTransport);
    this.collections = new WeaviateCollectionsClientAsync(restTransport, grpcTransport);
  }

  /**
   * Connect to a local Weaviate instance.
   *
   * <p>
   * This call is blocking if {@link Authentication} configured,
   * as the client will need to do the initial token exchange.
   */
  public static WeaviateClientAsync local() {
    return local(ObjectBuilder.identity());
  }

  /**
   * Connect to a local Weaviate instance.
   *
   * <p>
   * This call is blocking if {@link Authentication} configured,
   * as the client will need to do the initial token exchange.
   */
  public static WeaviateClientAsync local(Function<Config.Local, ObjectBuilder<Config>> fn) {
    return new WeaviateClientAsync(fn.apply(new Config.Local()).build());
  }

  /**
   * Connect to a Weaviate Cloud instance.
   *
   * <p>
   * This call is blocking if {@link Authentication} configured,
   * as the client will need to do the initial token exchange.
   */
  public static WeaviateClientAsync wcd(String httpHost, String apiKey) {
    return wcd(httpHost, apiKey, ObjectBuilder.identity());
  }

  /**
   * Connect to a Weaviate Cloud instance.
   *
   * <p>
   * This call is blocking if {@link Authentication} configured,
   * as the client will need to do the initial token exchange.
   */
  public static WeaviateClientAsync wcd(String httpHost, String apiKey,
      Function<Config.WeaviateCloud, ObjectBuilder<Config>> fn) {
    var config = new Config.WeaviateCloud(httpHost, Authentication.apiKey(apiKey));
    return new WeaviateClientAsync(fn.apply(config).build());
  }

  /**
   * Connect to a Weaviate instance with custom configuration.
   *
   * <p>
   * This call is blocking if {@link Authentication} configured,
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

  /** Get deployement metadata for the target Weaviate instance. */
  public CompletableFuture<InstanceMetadata> meta() throws IOException {
    return this.restTransport.performRequestAsync(null, InstanceMetadataRequest._ENDPOINT);
  }

  /**
   * Close {@link #restTransport} and {@link #grpcTransport}
   * and release associated resources.
   */
  @Override
  public void close() throws Exception {
    this.restTransport.close();
    this.grpcTransport.close();
  }
}
