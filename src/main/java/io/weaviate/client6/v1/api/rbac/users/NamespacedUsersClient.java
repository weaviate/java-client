package io.weaviate.client6.v1.api.rbac.users;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import io.weaviate.client6.v1.api.WeaviateApiException;
import io.weaviate.client6.v1.api.rbac.Role;
import io.weaviate.client6.v1.internal.ObjectBuilder;
import io.weaviate.client6.v1.internal.rest.RestTransport;

public abstract class NamespacedUsersClient {
  protected final RestTransport restTransport;
  private final UserType userType;

  public NamespacedUsersClient(RestTransport restTransport, UserType userType) {
    this.restTransport = restTransport;
    this.userType = userType;
  }

  /**
   * Get the roles assigned a user with type {@link #userType}.
   *
   * @param userId OIDC group ID.
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   * @throws IOException          in case the request was not sent successfully
   *                              due to a malformed request, a networking error
   *                              or the server being unavailable.
   */
  public List<Role> assignedRoles(String userId) throws IOException {
    return this.restTransport.performRequest(GetAssignedRolesRequest.of(userId, userType),
        GetAssignedRolesRequest._ENDPOINT);
  }

  /**
   * Get the roles assigned a user with type {@link #userType}.
   *
   * @param userId OIDC group ID.
   * @param fn     Lambda expression for optional parameters.
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   * @throws IOException          in case the request was not sent successfully
   *                              due to a malformed request, a networking error
   *                              or the server being unavailable.
   */
  public List<Role> assignedRoles(String userId,
      Function<GetAssignedRolesRequest.Builder, ObjectBuilder<GetAssignedRolesRequest>> fn) throws IOException {
    return this.restTransport.performRequest(GetAssignedRolesRequest.of(userId, userType, fn),
        GetAssignedRolesRequest._ENDPOINT);
  }

  /**
   * Assing roles to a user with type {@link #userType}.
   *
   * @param userId    User ID.
   * @param roleNames Role names.
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   * @throws IOException          in case the request was not sent successfully
   *                              due to a malformed request, a networking error
   *                              or the server being unavailable.
   */
  public void assignRoles(String userId, String... roleNames) throws IOException {
    this.restTransport.performRequest(new AssignRolesRequest(userId, userType, Arrays.asList(roleNames)),
        AssignRolesRequest._ENDPOINT);
  }

  /**
   * Revoke roles from a user with type {@link #userType}.
   *
   * @param userId    User ID.
   * @param roleNames Role names.
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   * @throws IOException          in case the request was not sent successfully
   *                              due to a malformed request, a networking error
   *                              or the server being unavailable.
   */
  public void revokeRoles(String userId, String... roleNames) throws IOException {
    this.restTransport.performRequest(new RevokeRolesRequest(userId, userType, Arrays.asList(roleNames)),
        RevokeRolesRequest._ENDPOINT);
  }
}
