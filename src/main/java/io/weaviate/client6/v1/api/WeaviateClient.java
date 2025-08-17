package io.weaviate.client6.v1.api;

import java.io.IOException;
import java.util.function.Function;

import io.weaviate.client6.v1.api.collections.WeaviateCollectionsClient;
import io.weaviate.client6.v1.internal.ObjectBuilder;
import io.weaviate.client6.v1.internal.TokenProvider;
import io.weaviate.client6.v1.internal.grpc.DefaultGrpcTransport;
import io.weaviate.client6.v1.internal.grpc.GrpcChannelOptions;
import io.weaviate.client6.v1.internal.grpc.GrpcTransport;
import io.weaviate.client6.v1.internal.rest.DefaultRestTransport;
import io.weaviate.client6.v1.internal.rest.RestTransport;
import io.weaviate.client6.v1.internal.rest.RestTransportOptions;

public class WeaviateClient implements AutoCloseable {
  /** Store this for {@link #async()} helper. */
  private final Config config;

  private final RestTransport restTransport;
  private final GrpcTransport grpcTransport;

  public final WeaviateCollectionsClient collections;

  public WeaviateClient(Config config) {
    this.config = config;

    RestTransportOptions restOpt;
    GrpcChannelOptions grpcOpt;
    if (config.authorization() == null) {
      restOpt = config.restTransportOptions();
      grpcOpt = config.grpcTransportOptions();
    } else {
      TokenProvider tokenProvider;
      try (final var noAuthRest = new DefaultRestTransport(config.restTransportOptions())) {
        tokenProvider = config.authorization().getTokenProvider(noAuthRest);
      } catch (Exception e) {
        // Generally exceptions are caught in TokenProvider internals.
        // This one may be thrown when noAuthRest transport is auto-closed.
        throw new WeaviateOAuthException(e);
      }
      restOpt = config.restTransportOptions(tokenProvider);
      grpcOpt = config.grpcTransportOptions(tokenProvider);
    }

    this.restTransport = new DefaultRestTransport(restOpt);
    this.grpcTransport = new DefaultGrpcTransport(grpcOpt);

    this.collections = new WeaviateCollectionsClient(restTransport, grpcTransport);
  }

  /**
   * Create {@link WeaviateClientAsync} with identical configurations.
   * It is a shorthand for:
   *
   * <pre>{@code
   * var config = new Config(...);
   * var client = new WeaviateClient(config);
   * var async = new WeaviateClientAsync(config);
   * }</pre>
   *
   * and as such, this does not manage or reuse resources (transport, gRPC
   * channel, etc) used by the original client. Keep that in mind and make
   * sure to close the original and async clients individually.
   *
   * <p>
   * Example:
   *
   * <pre>{@code
   * var client = WeaviateClient.local();
   *
   * // Need to make the next request non-blocking
   * try (final var async = client.async()) {
   *   async.collections.create("Things");
   * }
   * // At this point only `async` resource has been auto-closed.
   *
   * client.close();
   * }</pre>
   *
   *
   * If you only intend to use {@link WeaviateClientAsync}, prefer creating it
   * directly via one of its static factories:
   * <ul>
   * <li>{@link WeaviateClientAsync#local}
   * <li>{@link WeaviateClientAsync#wcd}
   * <li>{@link WeaviateClientAsync#custom}
   * </ul>
   *
   * Otherwise the client wastes time initializing resources it will never use.
   */
  public WeaviateClientAsync async() {
    return new WeaviateClientAsync(config);
  }

  /** Connect to a local Weaviate instance. */
  public static WeaviateClient local() {
    return local(ObjectBuilder.identity());
  }

  /** Connect to a local Weaviate instance. */
  public static WeaviateClient local(Function<Config.Local, ObjectBuilder<Config>> fn) {
    return new WeaviateClient(fn.apply(new Config.Local()).build());
  }

  /** Connect to a Weaviate Cloud instance. */
  public static WeaviateClient wcd(String httpHost, String apiKey) {
    return wcd(httpHost, apiKey, ObjectBuilder.identity());
  }

  /** Connect to a Weaviate Cloud instance. */
  public static WeaviateClient wcd(String httpHost, String apiKey,
      Function<Config.WeaviateCloud, ObjectBuilder<Config>> fn) {
    var config = new Config.WeaviateCloud(httpHost, Authorization.apiKey(apiKey));
    return new WeaviateClient(fn.apply(config).build());
  }

  /** Connect to a Weaviate instance with custom configuration. */
  public static WeaviateClient custom(Function<Config.Custom, ObjectBuilder<Config>> fn) {
    return new WeaviateClient(fn.apply(new Config.Custom()).build());
  }

  /** Ping the server for a liveness check. */
  public boolean isLive() throws IOException {
    return this.restTransport.performRequest(null, IsLiveRequest._ENDPOINT);
  }

  /** Ping the server for a readiness check. */
  public boolean isReady() throws IOException {
    return this.restTransport.performRequest(null, IsReadyRequest._ENDPOINT);
  }

  /** Get deployement metadata for the target Weaviate instance. */
  public InstanceMetadata meta() throws IOException {
    return this.restTransport.performRequest(null, InstanceMetadataRequest._ENDPOINT);
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
