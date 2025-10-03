package io.weaviate.client6.v1.api.rbac.groups;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import io.weaviate.client6.v1.api.rbac.Role;
import io.weaviate.client6.v1.internal.ObjectBuilder;
import io.weaviate.client6.v1.internal.rest.RestTransport;

public class WeaviateGroupsClientAsync {
  private final RestTransport restTransport;

  public WeaviateGroupsClientAsync(RestTransport restTransport) {
    this.restTransport = restTransport;
  }

  /**
   * Get the roles assigned an OIDC group.
   *
   * @param groupId OIDC group ID.
   */
  public CompletableFuture<List<Role>> assignedRoles(String groupId) {
    return this.restTransport.performRequestAsync(GetAssignedRolesRequest.of(groupId),
        GetAssignedRolesRequest._ENDPOINT);
  }

  /**
   * Get the roles assigned an OIDC group.
   *
   * @param groupId OIDC group ID.
   * @param fn      Lambda expression for optional parameters.
   */
  public CompletableFuture<List<Role>> assignedRoles(String groupId,
      Function<GetAssignedRolesRequest.Builder, ObjectBuilder<GetAssignedRolesRequest>> fn) {
    return this.restTransport.performRequestAsync(GetAssignedRolesRequest.of(groupId, fn),
        GetAssignedRolesRequest._ENDPOINT);
  }

  /** Get the names of known OIDC groups. */
  public CompletableFuture<List<String>> knownGroupNames() {
    return this.restTransport.performRequestAsync(null, GetKnownGroupNamesRequest._ENDPOINT);
  }

  /**
   * Assign roles to OIDC group.
   *
   * @param groupId   OIDC group ID.
   * @param roleNames Role names.
   */
  public CompletableFuture<Void> assignRoles(String groupId, String... roleNames) {
    return this.restTransport.performRequestAsync(new AssignRolesRequest(groupId, Arrays.asList(roleNames)),
        AssignRolesRequest._ENDPOINT);
  }

  /**
   * Revoke roles from OIDC group.
   *
   * @param groupId   OIDC group ID.
   * @param roleNames Role names.
   */
  public CompletableFuture<Void> revokeRoles(String groupId, String... roleNames) {
    return this.restTransport.performRequestAsync(new RevokeRolesRequest(groupId, Arrays.asList(roleNames)),
        RevokeRolesRequest._ENDPOINT);
  }
}
