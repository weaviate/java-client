package io.weaviate.client6.v1.api;

import java.io.IOException;
import java.util.function.Function;

import io.weaviate.client6.v1.api.alias.WeaviateAliasClient;
import io.weaviate.client6.v1.api.backup.WeaviateBackupClient;
import io.weaviate.client6.v1.api.cluster.WeaviateClusterClient;
import io.weaviate.client6.v1.api.collections.WeaviateCollectionsClient;
import io.weaviate.client6.v1.api.rbac.groups.WeaviateGroupsClient;
import io.weaviate.client6.v1.api.rbac.roles.WeaviateRolesClient;
import io.weaviate.client6.v1.api.rbac.users.WeaviateUsersClient;
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

  /**
   * Client for {@code /schema} endpoints for managing Weaviate collections.
   * See {@link WeaviateCollectionsClient#use} for populating and querying
   * collections.
   */
  public final WeaviateCollectionsClient collections;

  /** Client for {@code /aliases} endpoints for managing collection aliases. */
  public final WeaviateAliasClient alias;

  /** Client for {@code /backups} endpoints for managing backups. */
  public final WeaviateBackupClient backup;

  /**
   * Client for {@code /authz/roles} endpoints for managing RBAC roles.
   */
  public final WeaviateRolesClient roles;

  /**
   * Client for {@code /authz/groups} endpoints for managing RBAC groups.
   */
  public final WeaviateGroupsClient groups;

  /**
   * Client for {@code /users} endpoints for managing DB / OIDC users.
   */
  public final WeaviateUsersClient users;

  /**
   * Client for {@code /nodes} and {@code /replication} endpoints
   * for managing replication and sharding.
   */
  public final WeaviateClusterClient cluster;

  public WeaviateClient(Config config) {
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
        // Generally exceptions are caught in TokenProvider internals.
        // This one may be thrown when noAuthRest transport is auto-closed.
        throw new WeaviateOAuthException(e);
      }
      restOpt = config.restTransportOptions(tokenProvider);
      grpcOpt = config.grpcTransportOptions(tokenProvider);
    }

    // Initialize REST transport to a temporary variable to dispose of
    // the associated resources in case we have to throw an exception.
    // Assign to this.restTransport only once we're in the clear to
    // avoid publishing the object before it's fully initialized.
    var _restTransport = new DefaultRestTransport(restOpt);
    boolean isLive = false;
    InstanceMetadata meta = null;
    try {
      isLive = _restTransport.performRequest(null, IsLiveRequest._ENDPOINT);
      meta = _restTransport.performRequest(null, InstanceMetadataRequest._ENDPOINT);
    } catch (IOException e) {
      throw new WeaviateConnectException(e);
    }

    if (!isLive) {
      var ex = new WeaviateConnectException("Weaviate not available at " + restOpt.baseUrl());
      try {
        _restTransport.close();
      } catch (Exception e) {
        ex.addSuppressed(e);
      }
      throw ex;
    }

    if (meta.grpcMaxMessageSize() != null) {
      grpcOpt = grpcOpt.withMaxMessageSize(meta.grpcMaxMessageSize());
    }

    this.restTransport = _restTransport;
    this.grpcTransport = new DefaultGrpcTransport(grpcOpt);
    this.alias = new WeaviateAliasClient(restTransport);
    this.backup = new WeaviateBackupClient(restTransport);
    this.collections = new WeaviateCollectionsClient(restTransport, grpcTransport);
    this.roles = new WeaviateRolesClient(restTransport);
    this.groups = new WeaviateGroupsClient(restTransport);
    this.users = new WeaviateUsersClient(restTransport);
    this.cluster = new WeaviateClusterClient(restTransport);
    this.config = config;
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
   * var client = WeaviateClient.connectToLocal();
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
   * <li>{@link WeaviateClientAsync#connectToLocal}
   * <li>{@link WeaviateClientAsync#connectToWeaviateCloud}
   * <li>{@link WeaviateClientAsync#connectToCustom}
   * </ul>
   *
   * Otherwise the client wastes time initializing resources it will never use.
   */
  public WeaviateClientAsync async() {
    return new WeaviateClientAsync(config);
  }

  /** Connect to a local Weaviate instance. */
  public static WeaviateClient connectToLocal() {
    return connectToLocal(ObjectBuilder.identity());
  }

  /** Connect to a local Weaviate instance. */
  public static WeaviateClient connectToLocal(Function<Config.Local, ObjectBuilder<Config>> fn) {
    return new WeaviateClient(fn.apply(new Config.Local()).build());
  }

  /** Connect to a Weaviate Cloud instance. */
  public static WeaviateClient connectToWeaviateCloud(String httpHost, String apiKey) {
    return connectToWeaviateCloud(httpHost, apiKey, ObjectBuilder.identity());
  }

  /** Connect to a Weaviate Cloud instance. */
  public static WeaviateClient connectToWeaviateCloud(String httpHost, String apiKey,
      Function<Config.WeaviateCloud, ObjectBuilder<Config>> fn) {
    var config = new Config.WeaviateCloud(httpHost, Authentication.apiKey(apiKey));
    return new WeaviateClient(fn.apply(config).build());
  }

  /** Connect to a Weaviate instance with custom configuration. */
  public static WeaviateClient connectToCustom(Function<Config.Custom, ObjectBuilder<Config>> fn) {
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
