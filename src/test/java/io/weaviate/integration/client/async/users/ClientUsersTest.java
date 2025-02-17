package io.weaviate.integration.client.async.users;

import java.util.List;

import io.weaviate.client.Config;
import io.weaviate.client.WeaviateAuthClient;
import io.weaviate.client.base.Result;
import io.weaviate.client.v1.async.users.Users;
import io.weaviate.client.v1.auth.exception.AuthException;
import io.weaviate.client.v1.rbac.model.Role;
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
  public Result<List<Role>> getUserRoles(String user) {
    return rethrow(() -> users.userRolesGetter().withUser(user).run().get());
  }

  @Override
  public Result<?> assignRoles(String user, String... roles) {
    return rethrow(() -> this.users.assigner().withUser(user).witRoles(roles).run().get());
  }

  @Override
  public Result<?> revokeRoles(String user, String... roles) {
    return rethrow(() -> this.users.revoker().withUser(user).witRoles(roles).run().get());
  }
}
