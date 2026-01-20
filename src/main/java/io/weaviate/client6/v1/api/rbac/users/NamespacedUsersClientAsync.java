package io.weaviate.client6.v1.api.rbac.users;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import io.weaviate.client6.v1.api.rbac.Role;
import io.weaviate.client6.v1.internal.ObjectBuilder;
import io.weaviate.client6.v1.internal.rest.RestTransport;

public abstract class NamespacedUsersClientAsync {
  protected final RestTransport restTransport;
  private final UserType userType;

  public NamespacedUsersClientAsync(RestTransport restTransport, UserType userType) {
    this.restTransport = restTransport;
    this.userType = userType;
  }

  /**
   * Get the roles assigned a user with type {@link #userType}.
   *
   * @param userId OIDC group ID.
   */
  public CompletableFuture<List<Role>> assignedRoles(String userId) {
    return this.restTransport.performRequestAsync(GetAssignedRolesRequest.of(userId, userType),
        GetAssignedRolesRequest._ENDPOINT);
  }

  /**
   * Get the roles assigned a user with type {@link #userType}.
   *
   * @param userId OIDC group ID.
   * @param fn     Lambda expression for optional parameters.
   */
  public CompletableFuture<List<Role>> assignedRoles(String userId,
      Function<GetAssignedRolesRequest.Builder, ObjectBuilder<GetAssignedRolesRequest>> fn) {
    return this.restTransport.performRequestAsync(GetAssignedRolesRequest.of(userId, userType, fn),
        GetAssignedRolesRequest._ENDPOINT);
  }

  /**
   * Assing roles to a user with type {@link #userType}.
   *
   * @param userId    User ID.
   * @param roleNames Role names.
   */
  public CompletableFuture<Void> assignRoles(String userId, String... roleNames) {
    return this.restTransport.performRequestAsync(new AssignRolesRequest(userId, userType, Arrays.asList(roleNames)),
        AssignRolesRequest._ENDPOINT);
  }

  /**
   * Revoke roles from a user with type {@link #userType}.
   *
   * @param userId    User ID.
   * @param roleNames Role names.
   */
  public CompletableFuture<Void> revokeRoles(String userId, String... roleNames) {
    return this.restTransport.performRequestAsync(new RevokeRolesRequest(userId, userType, Arrays.asList(roleNames)),
        RevokeRolesRequest._ENDPOINT);
  }
}
