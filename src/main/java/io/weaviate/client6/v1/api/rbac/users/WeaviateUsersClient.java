package io.weaviate.client6.v1.api.rbac.users;

import java.io.IOException;

import io.weaviate.client6.v1.api.WeaviateApiException;
import io.weaviate.client6.v1.internal.rest.RestTransport;

public class WeaviateUsersClient {
  private final RestTransport restTransport;

  /**
   * Client for managing {@link UserType#DB_USER} and {@link UserType#DB_ENV_USER}
   * users.
   */
  public final DbUsersClient db;

  /** Client for managing {@link UserType#OIDC} users. */
  public final OidcUsersClient oidc;

  public WeaviateUsersClient(RestTransport restTransport) {
    this.restTransport = restTransport;
    this.db = new DbUsersClient(restTransport);
    this.oidc = new OidcUsersClient(restTransport);
  }

  /**
   * Get my user info.
   *
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   * @throws IOException          in case the request was not sent successfully
   *                              due to a malformed request, a networking error
   *                              or the server being unavailable.
   */
  public User myUser() throws IOException {
    return this.restTransport.performRequest(null, GetMyUserRequest._ENDPOINT);
  }
}
