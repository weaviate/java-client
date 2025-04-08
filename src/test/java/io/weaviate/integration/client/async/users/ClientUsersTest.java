package io.weaviate.integration.client.async.users;

import java.util.List;

import io.weaviate.client.Config;
import io.weaviate.client.WeaviateAuthClient;
import io.weaviate.client.base.Result;
import io.weaviate.client.v1.async.users.DbUsers;
import io.weaviate.client.v1.async.users.OidcUsers;
import io.weaviate.client.v1.async.users.Users;
import io.weaviate.client.v1.auth.exception.AuthException;
import io.weaviate.client.v1.rbac.model.Role;
import io.weaviate.client.v1.users.model.User;
import io.weaviate.client.v1.users.model.UserDb;
import io.weaviate.integration.client.async.rbac.ClientRbacTest;
import io.weaviate.integration.tests.users.ClientUsersTestSuite;

/**
 * ClientUsersTest is a {@link ClientUsersTestSuite.Users} implementation and a
 * wrapper around WeaviateAsyncClient.Roles client which allows the latter to be
 * used in the ClientUsersTestSuite.
 */
public class ClientUsersTest extends ClientRbacTest implements ClientUsersTestSuite.Users {
  private Users users;

  public ClientUsersTest(Config config, String apiKey) {
    super(config, apiKey);
    try {
      this.users = WeaviateAuthClient.apiKey(config, apiKey).async().users();
    } catch (AuthException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public Result<User> getMyUser() {
    return rethrow(() -> users.myUserGetter().run());
  }

  @Override
  public Result<List<Role>> getUserRoles(String user) {
    return rethrow(() -> users.userRolesGetter().withUserId(user).run());
  }

  @Override
  public Result<?> assignRoles(String user, String... roles) {
    return rethrow(() -> this.users.assigner().withUserId(user).witRoles(roles).run());
  }

  @Override
  public Result<?> revokeRoles(String user, String... roles) {
    return rethrow(() -> this.users.revoker().withUserId(user).witRoles(roles).run());
  }

  @Override
  public ClientUsersTestSuite.DbUsers db() {
    return new NamespacedUsers(false);
  }

  @Override
  public ClientUsersTestSuite.OidcUsers oidc() {
    return new NamespacedUsers(true);
  }

  /**
   * NamespacedUsers uses one of the namespaced clients based on the
   * value of useOidc. This reduces code duplication, allowing us to
   * reuse the same implementation for several test iterfaces.
   */
  private class NamespacedUsers implements
      ClientUsersTestSuite.DbUsers, ClientUsersTestSuite.OidcUsers {
    private final DbUsers db;
    private final OidcUsers oidc;
    private final boolean useOidc;

    public NamespacedUsers(boolean useOidc) {
      this.db = users.db();
      this.oidc = users.oidc();
      this.useOidc = useOidc;
    }

    @Override
    public Result<?> assignRoles(String user, String... roles) {
      return useOidc
          ? rethrow(() -> oidc.assigner().withUserId(user).witRoles(roles).run())
          : rethrow(() -> db.assigner().withUserId(user).witRoles(roles).run());
    }

    @Override
    public Result<?> revokeRoles(String user, String... roles) {
      return useOidc
          ? rethrow(() -> oidc.revoker().withUserId(user).witRoles(roles).run())
          : rethrow(() -> db.revoker().withUserId(user).witRoles(roles).run());
    }

    @Override
    public Result<List<Role>> getAssignedRoles(String user, boolean includePermissions) {
      return useOidc
          ? rethrow(() -> oidc.userRolesGetter().withUserId(user).includePermissions(includePermissions).run())
          : rethrow(() -> db.userRolesGetter().withUserId(user).includePermissions(includePermissions).run());
    }

    @Override
    public Result<String> create(String user) {
      return rethrow(() -> db.creator().withUserId(user).run());
    }

    @Override
    public Result<String> rotateKey(String user) {
      return rethrow(() -> db.keyRotator().withUserId(user).run());
    }

    @Override
    public Result<Boolean> delete(String user) {
      return rethrow(() -> db.deleter().withUserId(user).run());
    }

    @Override
    public Result<Boolean> activate(String user) {
      return rethrow(() -> db.activator().withUserId(user).run());
    }

    @Override
    public Result<Boolean> deactivate(String user, boolean revokeKey) {
      return rethrow(() -> db.deactivator().withUserId(user).run());
    }

    @Override
    public Result<UserDb> getUser(String user) {
      return rethrow(() -> db.getUser().withUserId(user).run());
    }

    @Override
    public Result<List<UserDb>> getAll() {
      return rethrow(() -> db.allGetter().run());
    }
  }
}
