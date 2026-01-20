package io.weaviate.client6.v1.api;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import io.weaviate.client6.v1.api.alias.WeaviateAliasClientAsync;
import io.weaviate.client6.v1.api.backup.WeaviateBackupClientAsync;
import io.weaviate.client6.v1.api.cluster.WeaviateClusterClientAsync;
import io.weaviate.client6.v1.api.collections.WeaviateCollectionsClient;
import io.weaviate.client6.v1.api.collections.WeaviateCollectionsClientAsync;
import io.weaviate.client6.v1.api.rbac.groups.WeaviateGroupsClientAsync;
import io.weaviate.client6.v1.api.rbac.roles.WeaviateRolesClientAsync;
import io.weaviate.client6.v1.api.rbac.users.WeaviateUsersClientAsync;
import io.weaviate.client6.v1.internal.ObjectBuilder;
import io.weaviate.client6.v1.internal.Timeout;
import io.weaviate.client6.v1.internal.TokenProvider;
import io.weaviate.client6.v1.internal.VersionSupport;
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

  /** Client for {@code /aliases} endpoints for managing collection aliases. */
  public final WeaviateAliasClientAsync alias;

  /**
   * Client for {@code /authz/roles} endpoints for managing RBAC roles.
   */
  public final WeaviateRolesClientAsync roles;

  /**
   * Client for {@code /authz/groups} endpoints for managing RBAC groups.
   */
  public final WeaviateGroupsClientAsync groups;

  /**
   * Client for {@code /users} endpoints for managing DB / OIDC users.
   */
  public final WeaviateUsersClientAsync users;

  /** Client for {@code /backups} endpoints for managing backups. */
  public final WeaviateBackupClientAsync backup;

  /**
   * Client for {@code /nodes} and {@code /replication} endpoints
   * for managing replication and sharding.
   */
  public final WeaviateClusterClientAsync cluster;

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
    var _restTransport = new DefaultRestTransport(restOpt.withTimeout(
        new Timeout(restOpt.timeout().initSeconds())));
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

    if (!VersionSupport.isSupported(meta.version())) {
      throw new WeaviateUnsupportedVersionException(meta.version());
    }

    if (meta.grpcMaxMessageSize() != null) {
      grpcOpt = grpcOpt.withMaxMessageSize(meta.grpcMaxMessageSize());
    }

    this.restTransport = _restTransport;
    this.grpcTransport = new DefaultGrpcTransport(grpcOpt);
    this.alias = new WeaviateAliasClientAsync(restTransport);
    this.backup = new WeaviateBackupClientAsync(restTransport);
    this.roles = new WeaviateRolesClientAsync(restTransport);
    this.groups = new WeaviateGroupsClientAsync(restTransport);
    this.users = new WeaviateUsersClientAsync(restTransport);
    this.cluster = new WeaviateClusterClientAsync(restTransport);
    this.collections = new WeaviateCollectionsClientAsync(restTransport, grpcTransport);
  }

  /**
   * Connect to a local Weaviate instance.
   *
   * <p>
   * This call is blocking if {@link Authentication} configured,
   * as the client will need to do the initial token exchange.
   */
  public static WeaviateClientAsync connectToLocal() {
    return connectToLocal(ObjectBuilder.identity());
  }

  /**
   * Connect to a local Weaviate instance.
   *
   * <p>
   * This call is blocking if {@link Authentication} configured,
   * as the client will need to do the initial token exchange.
   */
  public static WeaviateClientAsync connectToLocal(Function<Config.Local, ObjectBuilder<Config>> fn) {
    return new WeaviateClientAsync(fn.apply(new Config.Local()).build());
  }

  /**
   * Connect to a Weaviate Cloud instance.
   *
   * <p>
   * This call is blocking if {@link Authentication} configured,
   * as the client will need to do the initial token exchange.
   */
  public static WeaviateClientAsync connectToWeaviateCloud(String httpHost, String apiKey) {
    return connectToWeaviateCloud(httpHost, apiKey, ObjectBuilder.identity());
  }

  /**
   * Connect to a Weaviate Cloud instance.
   *
   * <p>
   * This call is blocking if {@link Authentication} configured,
   * as the client will need to do the initial token exchange.
   */
  public static WeaviateClientAsync connectToWeaviateCloud(String httpHost, String apiKey,
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
  public static WeaviateClientAsync connectToCustom(Function<Config.Custom, ObjectBuilder<Config>> fn) {
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
