package io.weaviate.client.v1.rbac;

import io.weaviate.client.Config;
import io.weaviate.client.base.http.HttpClient;
import io.weaviate.client.v1.rbac.api.AssignedUsersGetter;
import io.weaviate.client.v1.rbac.api.PermissionAdder;
import io.weaviate.client.v1.rbac.api.PermissionChecker;
import io.weaviate.client.v1.rbac.api.PermissionRemover;
import io.weaviate.client.v1.rbac.api.RoleAllGetter;
import io.weaviate.client.v1.rbac.api.RoleCreator;
import io.weaviate.client.v1.rbac.api.RoleDeleter;
import io.weaviate.client.v1.rbac.api.RoleExists;
import io.weaviate.client.v1.rbac.api.RoleGetter;
import io.weaviate.client.v1.rbac.api.UserAssignmentsGetter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class Roles {

  private final HttpClient httpClient;
  private final Config config;

  /** Create a new role. */
  public RoleCreator creator() {
    return new RoleCreator(httpClient, config);
  }

  /** Delete a role. */
  public RoleDeleter deleter() {
    return new RoleDeleter(httpClient, config);
  }

  /**
   * Add permissions to an existing role.
   * Note: This method is an upsert operation. If the permission already exists,
   * it will be updated. If it does not exist, it will be created.
   */
  public PermissionAdder permissionAdder() {
    return new PermissionAdder(httpClient, config);
  }

  /**
   * Remove permissions from a role.
   * Note: This method is a downsert operation. If the permission does not
   * exist, it will be ignored. If these permissions are the only permissions of
   * the role, the role will be deleted.
   */
  public PermissionRemover permissionRemover() {
    return new PermissionRemover(httpClient, config);
  }

  /** Check if a role has a permission. */
  public PermissionChecker permissionChecker() {
    return new PermissionChecker(httpClient, config);
  }

  /** Get all existing roles. */
  public RoleAllGetter allGetter() {
    return new RoleAllGetter(httpClient, config);
  };

  /** Get role and its associated permissions. */
  public RoleGetter getter() {
    return new RoleGetter(httpClient, config);
  };

  /** Get users assigned to a role. */
  public AssignedUsersGetter assignedUsersGetter() {
    return new AssignedUsersGetter(httpClient, config);
  };

  /** Get users assigned to a role. */
  public UserAssignmentsGetter userAssignmentsGetter() {
    return new UserAssignmentsGetter(httpClient, config);
  };

  /** Check if a role exists. */
  public RoleExists exists() {
    return new RoleExists(httpClient, config);
  }
}
