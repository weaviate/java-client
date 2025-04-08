package io.weaviate.client.v1.async.rbac;

import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;

import io.weaviate.client.Config;
import io.weaviate.client.v1.async.rbac.api.AssignedUsersGetter;
import io.weaviate.client.v1.async.rbac.api.PermissionAdder;
import io.weaviate.client.v1.async.rbac.api.PermissionChecker;
import io.weaviate.client.v1.async.rbac.api.PermissionRemover;
import io.weaviate.client.v1.async.rbac.api.RoleAllGetter;
import io.weaviate.client.v1.async.rbac.api.RoleCreator;
import io.weaviate.client.v1.async.rbac.api.RoleDeleter;
import io.weaviate.client.v1.async.rbac.api.RoleExists;
import io.weaviate.client.v1.async.rbac.api.RoleGetter;
import io.weaviate.client.v1.async.rbac.api.UserAssignmentsGetter;
import io.weaviate.client.v1.auth.provider.AccessTokenProvider;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class Roles {

  private final CloseableHttpAsyncClient client;
  private final Config config;
  private final AccessTokenProvider tokenProvider;

  public RoleCreator creator() {
    return new RoleCreator(client, config, tokenProvider);
  }

  /** Get all existing roles. */
  public RoleDeleter deleter() {
    return new RoleDeleter(client, config, tokenProvider);
  }

  /**
   * Add permissions to an existing role.
   * Note: This method is an upsert operation. If the permission already exists,
   * it will be updated. If it does not exist, it will be created.
   */
  public PermissionAdder permissionAdder() {
    return new PermissionAdder(client, config, tokenProvider);
  }

  /**
   * Remove permissions from a role.
   * Note: This method is a downsert operation. If the permission does not
   * exist, it will be ignored. If these permissions are the only permissions of
   * the role, the role will be deleted.
   */
  public PermissionRemover permissionRemover() {
    return new PermissionRemover(client, config, tokenProvider);
  }

  /** Check if a role has a permission. */
  public PermissionChecker permissionChecker() {
    return new PermissionChecker(client, config, tokenProvider);
  }

  /** Get all existing roles. */
  public RoleAllGetter allGetter() {
    return new RoleAllGetter(client, config, tokenProvider);
  };

  /** Get role and its assiciated permissions. */
  public RoleGetter getter() {
    return new RoleGetter(client, config, tokenProvider);
  };

  /** Get users assigned to a role. */
  public AssignedUsersGetter assignedUsersGetter() {
    return new AssignedUsersGetter(client, config, tokenProvider);
  };

  /** Get users assigned to a role. */
  public UserAssignmentsGetter userAssignmentsGetter() {
    return new UserAssignmentsGetter(client, config, tokenProvider);
  };

  /** Check if a role exists. */
  public RoleExists exists() {
    return new RoleExists(client, config, tokenProvider);
  }
}
