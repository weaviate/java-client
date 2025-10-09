package io.weaviate.client6.v1.api.rbac.roles;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import io.weaviate.client6.v1.api.rbac.Permission;
import io.weaviate.client6.v1.api.rbac.Role;
import io.weaviate.client6.v1.internal.rest.RestTransport;

public class WeaviateRolesClientAsync {
  private final RestTransport restTransport;

  public WeaviateRolesClientAsync(RestTransport restTransport) {
    this.restTransport = restTransport;
  }

  /**
   * Create a new role.
   *
   * @param roleName    Role name.
   * @param permissions Permissions granted to the role.
   */
  public CompletableFuture<Void> create(String roleName, Permission... permissions) {
    var role = new Role(roleName, permissions);
    return this.restTransport.performRequestAsync(new CreateRoleRequest(role), CreateRoleRequest._ENDPOINT);
  }

  /**
   * Check if a role with a given name exists.
   *
   * @param roleName Role name.
   */
  public CompletableFuture<Boolean> exists(String roleName) {
    return this.restTransport.performRequestAsync(new RoleExistsRequest(roleName), RoleExistsRequest._ENDPOINT);
  }

  /**
   * Fetch role definition.
   *
   * @param roleName Role name.
   */
  public CompletableFuture<Optional<Role>> get(String roleName) {
    return this.restTransport.performRequestAsync(new GetRoleRequest(roleName), GetRoleRequest._ENDPOINT);
  }

  /** List all existing roles. */
  public CompletableFuture<List<Role>> list() {
    return this.restTransport.performRequestAsync(null, ListRolesRequest._ENDPOINT);
  }

  /**
   * Delete a role.
   *
   * @param roleName Role name.
   */
  public CompletableFuture<Void> delete(String roleName) {
    return this.restTransport.performRequestAsync(new DeleteRoleRequest(roleName), DeleteRoleRequest._ENDPOINT);
  }

  /**
   * Add permissions to a role.
   *
   * @param roleName    Role name.
   * @param permissions Permissions to add to the role.
   */
  public CompletableFuture<Void> addPermissions(String roleName, Permission... permissions) {
    return this.restTransport.performRequestAsync(new AddPermissionsRequest(roleName, Arrays.asList(permissions)),
        AddPermissionsRequest._ENDPOINT);
  }

  /**
   * Remove permissions from a role.
   *
   * @param roleName    Role name.
   * @param permissions Permissions to remove from the role.
   */
  public CompletableFuture<Void> removePermissions(String roleName, Permission... permissions) {
    return this.restTransport.performRequestAsync(new RemovePermissionsRequest(roleName, Arrays.asList(permissions)),
        RemovePermissionsRequest._ENDPOINT);
  }

  /**
   * Check if a role has a set of permissions.
   *
   * @param roleName   Role name.
   * @param permission Permission to check.
   */
  public CompletableFuture<Boolean> hasPermission(String roleName, Permission permission) {
    return this.restTransport.performRequestAsync(new HasPermissionRequest(roleName, permission),
        HasPermissionRequest._ENDPOINT);
  }

  /**
   * Get IDs of all users this role is assigned to.
   *
   * @param roleName Role name.
   */
  public CompletableFuture<List<String>> assignedUserIds(String roleName) {
    return this.restTransport.performRequestAsync(new GetAssignedUsersRequest(roleName),
        GetAssignedUsersRequest._ENDPOINT);
  }

  /**
   * Get IDs of all users this role is assigned to along with their user type.
   *
   * <p>
   * Note that, unlike {@link #assignedUserIds}, this method MAY return multiple
   * entries for the same user ID if OIDCS authentication is enabled: once with
   * "db_*" and another time with "oidc" user type.
   *
   * @param roleName Role name.
   */
  public CompletableFuture<List<UserAssignment>> userAssignments(String roleName) {
    return this.restTransport.performRequestAsync(new GetUserAssignementsRequest(roleName),
        GetUserAssignementsRequest._ENDPOINT);
  }

  /**
   * Get IDs of all groups this role is assigned to along with their group type.
   *
   * @param roleName Role name.
   */
  public CompletableFuture<List<GroupAssignment>> groupAssignments(String roleName) {
    return this.restTransport.performRequestAsync(new GetGroupAssignementsRequest(roleName),
        GetGroupAssignementsRequest._ENDPOINT);
  }
}
