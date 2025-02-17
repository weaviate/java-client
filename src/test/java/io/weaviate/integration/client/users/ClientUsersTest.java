package io.weaviate.integration.client.users;

import java.util.List;

import io.weaviate.client.Config;
import io.weaviate.client.WeaviateAuthClient;
import io.weaviate.client.base.Result;
import io.weaviate.client.v1.auth.exception.AuthException;
import io.weaviate.client.v1.rbac.model.Role;
import io.weaviate.client.v1.users.Users;
import io.weaviate.client.v1.users.model.User;
import io.weaviate.integration.client.rbac.ClientRbacTest;
import io.weaviate.integration.tests.users.ClientUsersTestSuite;

public class ClientUsersTest extends ClientRbacTest implements ClientUsersTestSuite.Users {
  private Users users;

  public ClientUsersTest(Config config, String apiKey) {
    super(config, apiKey);
    try {
      this.users = WeaviateAuthClient.apiKey(config, apiKey).users();
    } catch (AuthException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public Result<User> getMyUser() {
    return users.myUserGetter().run();
  }

  @Override
  public Result<List<Role>> getUserRoles(String user) {
    return users.userRolesGetter().withUser(user).run();
  }

  @Override
  public Result<?> assignRoles(String user, String... roles) {
    return this.users.assigner().withUser(user).witRoles(roles).run();
  }

  @Override
  public Result<?> revokeRoles(String user, String... roles) {
    return this.users.revoker().withUser(user).witRoles(roles).run();
  }
}
