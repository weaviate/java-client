package io.weaviate.client6.v1.api.rbac.users;

import java.util.concurrent.CompletableFuture;

import io.weaviate.client6.v1.internal.rest.RestTransport;

public class WeaviateUsersClientAsync {
  private final RestTransport restTransport;

  /**
   * Client for managing {@link UserType#DB_USER} and {@link UserType#DB_ENV_USER}
   * users.
   */
  public final DbUsersClientAsync db;

  /** Client for managing {@link UserType#OIDC} users. */
  public final OidcUsersClientAsync oidc;

  public WeaviateUsersClientAsync(RestTransport restTransport) {
    this.restTransport = restTransport;
    this.db = new DbUsersClientAsync(restTransport);
    this.oidc = new OidcUsersClientAsync(restTransport);
  }

  /** Get my user info. */
  public CompletableFuture<User> myUser() {
    return this.restTransport.performRequestAsync(null, GetMyUserRequest._ENDPOINT);
  }
}
