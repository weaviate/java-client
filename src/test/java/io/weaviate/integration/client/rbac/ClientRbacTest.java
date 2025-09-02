package io.weaviate.integration.client.rbac;

import java.util.List;

import io.weaviate.client.Config;
import io.weaviate.client.WeaviateAuthClient;
import io.weaviate.client.base.Result;
import io.weaviate.client.v1.auth.exception.AuthException;
import io.weaviate.client.v1.rbac.Roles;
import io.weaviate.client.v1.rbac.model.GroupAssignment;
import io.weaviate.client.v1.rbac.model.Permission;
import io.weaviate.client.v1.rbac.model.Role;
import io.weaviate.client.v1.rbac.model.UserAssignment;
import io.weaviate.integration.tests.rbac.ClientRbacTestSuite;

public class ClientRbacTest implements ClientRbacTestSuite.Rbac {
  private Roles roles;

  public ClientRbacTest(Config config, String apiKey) {
    try {
      this.roles = WeaviateAuthClient.apiKey(config, apiKey).roles();
    } catch (AuthException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public Result<Role> getRole(String role) {
    return roles.getter().withName(role).run();
  }

  @Override
  public Result<List<Role>> getAll() {
    return roles.allGetter().run();
  }

  @Override
  public Result<List<String>> getAssignedUsers(String role) {
    return roles.assignedUsersGetter().withRole(role).run();
  }

  @Override
  public Result<Boolean> createRole(String role, Permission<?>... permissions) {
    return roles.creator().withName(role).withPermissions(permissions).run();
  }

  @Override
  public void deleteRole(String role) {
    roles.deleter().withName(role).run();
  }

  @Override
  public Result<Boolean> hasPermission(String role, Permission<?> perm) {
    return roles.permissionChecker().withRole(role).withPermission(perm).run();
  }

  @Override
  public Result<Boolean> exists(String role) {
    return roles.exists().withName(role).run();
  }

  @Override
  public Result<?> addPermissions(String role, Permission<?>... permissions) {
    return roles.permissionAdder().withRole(role).withPermissions(permissions).run();
  }

  @Override
  public Result<?> removePermissions(String role, Permission<?>... permissions) {
    return roles.permissionRemover().withRole(role).withPermissions(permissions).run();
  }

  @Override
  public Result<List<UserAssignment>> getUserAssignments(String role) {
    return roles.userAssignmentsGetter().withRole(role).run();
  }

  @Override
  public Result<List<GroupAssignment>> getGroupAssignments(String role) {
    return roles.groupAssignmentsGetter().withRole(role).run();
  }
}
