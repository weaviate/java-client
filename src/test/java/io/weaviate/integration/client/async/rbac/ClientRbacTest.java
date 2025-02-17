package io.weaviate.integration.client.async.rbac;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import io.weaviate.client.Config;
import io.weaviate.client.WeaviateAuthClient;
import io.weaviate.client.base.Result;
import io.weaviate.client.v1.async.rbac.Roles;
import io.weaviate.client.v1.auth.exception.AuthException;
import io.weaviate.client.v1.rbac.model.Permission;
import io.weaviate.client.v1.rbac.model.Role;
import io.weaviate.integration.tests.rbac.ClientRbacTestSuite;
import io.weaviate.integration.tests.users.ClientUsersTestSuite;

/**
 * ClientRbacTest is a {@link ClientRbacTestSuite.Rbac} implementation and a
 * wrapper around WeaviateAsyncClient.Roles client which allows the latter to
 * be used in the ClientRbacTestSuite.
 */
public class ClientRbacTest implements ClientRbacTestSuite.Rbac {
  private Roles roles;

  public ClientRbacTest(Config config, String apiKey) {
    try {
      this.roles = WeaviateAuthClient.apiKey(config, apiKey).async().roles();
    } catch (AuthException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Get Future result and rethrow any exception as a RuntimeException
   * to allow calling AsyncClient methods without clashing with
   * {@link ClientRbacTestSuite.Rbac} and {@link ClientUsersTestSuite.Users}
   * method signatures.
   */
  protected <T> T rethrow(Callable<Future<T>> c) {
    try {
      return c.call().get();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public Result<Role> getRole(String role) {
    return rethrow(() -> roles.getter().withName(role).run());
  }

  @Override
  public Result<List<Role>> getAll() {
    return rethrow(() -> roles.allGetter().run());
  }

  @Override
  public Result<List<String>> getAssignedUsers(String role) {
    return rethrow(() -> roles.assignedUsersGetter().withRole(role).run());
  }

  @Override
  public void createRole(String role, Permission<?>... permissions) {
    rethrow(() -> roles.creator().withName(role).withPermissions(permissions).run());
  }

  @Override
  public void createRole(String role, Permission<?>[]... permissions) {
    rethrow(() -> roles.creator().withName(role).withPermissions(permissions).run());
  }

  @Override
  public void deleteRole(String role) {
    rethrow(() -> roles.deleter().withName(role).run());
  }

  @Override
  public Result<Boolean> hasPermission(String role, Permission<?> perm) {
    return rethrow(() -> roles.permissionChecker().withRole(role).withPermission(perm).run());
  }

  @Override
  public Result<Boolean> exists(String role) {
    return rethrow(() -> roles.exists().withName(role).run());
  }

  @Override
  public Result<?> addPermissions(String role, Permission<?>... permissions) {
    return rethrow(() -> roles.permissionAdder().withRole(role).withPermissions(permissions).run());
  }

  @Override
  public Result<?> addPermissions(String role, Permission<?>[]... permissions) {
    return rethrow(() -> roles.permissionAdder().withRole(role).withPermissions(permissions).run());
  }

  @Override
  public Result<?> removePermissions(String role, Permission<?>... permissions) {
    return rethrow(() -> roles.permissionRemover().withRole(role).withPermissions(permissions).run());
  }

  @Override
  public Result<?> removePermissions(String role, Permission<?>[]... permissions) {
    return rethrow(() -> roles.permissionRemover().withRole(role).withPermissions(permissions).run());
  }
}
