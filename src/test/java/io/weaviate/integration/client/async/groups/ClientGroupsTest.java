package io.weaviate.integration.client.async.groups;

import java.util.List;

import io.weaviate.client.Config;
import io.weaviate.client.WeaviateAuthClient;
import io.weaviate.client.base.Result;
import io.weaviate.client.v1.async.groups.Groups;
import io.weaviate.client.v1.auth.exception.AuthException;
import io.weaviate.client.v1.rbac.model.Role;
import io.weaviate.integration.client.async.rbac.ClientRbacTest;
import io.weaviate.integration.tests.groups.ClientGroupsTestSuite;

public class ClientGroupsTest extends ClientRbacTest implements ClientGroupsTestSuite.Oidc {
  private final Groups groups;

  public ClientGroupsTest(Config config, String apiKey) {
    super(config, apiKey);
    try {
      this.groups = WeaviateAuthClient.apiKey(config, apiKey).async().groups();
    } catch (AuthException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public Result<List<Role>> getAssignedRoles(String groupId) {
    return rethrow(() -> groups.oidc().assignedRolesGetter().withGroupId(groupId).run());
  }

  @Override
  public Result<List<String>> getKnownGroupNames() {
    return rethrow(() -> groups.oidc().knownGroupNamesGetter().run());
  }

  @Override
  public Result<?> assignRoles(String groupId, String... roles) {
    return rethrow(() -> groups.oidc().roleAssigner().withGroupId(groupId).witRoles(roles).run());
  }

  @Override
  public Result<?> revokeRoles(String groupId, String... roles) {
    return rethrow(() -> groups.oidc().roleRevoker().withGroupId(groupId).witRoles(roles).run());
  }
}
