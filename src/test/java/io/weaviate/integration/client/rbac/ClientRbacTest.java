package io.weaviate.integration.client.rbac;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.list;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import io.weaviate.client.Config;
import io.weaviate.client.WeaviateAuthClient;
import io.weaviate.client.base.Result;
import io.weaviate.client.v1.auth.exception.AuthException;
import io.weaviate.client.v1.rbac.Roles;
import io.weaviate.client.v1.rbac.model.BackupsPermission;
import io.weaviate.client.v1.rbac.model.ClusterPermission;
import io.weaviate.client.v1.rbac.model.CollectionsPermission;
import io.weaviate.client.v1.rbac.model.DataPermission;
import io.weaviate.client.v1.rbac.model.NodesPermission;
import io.weaviate.client.v1.rbac.model.NodesPermission.Verbosity;
import io.weaviate.client.v1.rbac.model.Permission;
import io.weaviate.client.v1.rbac.model.Role;
import io.weaviate.client.v1.rbac.model.RolesPermission;
import io.weaviate.client.v1.rbac.model.TenantsPermission;
import io.weaviate.integration.client.WeaviateDockerCompose;
import io.weaviate.integration.client.WeaviateDockerCompose.Weaviate;

public class ClientRbacTest {
  private static final String adminRole = "admin";
  private static final String viewerRole = "viewer";
  private static final String adminUser = "john-doe";

  private Roles roles;

  @Rule
  public TestName currentTest = new TestName();

  @ClassRule
  public static WeaviateDockerCompose compose = WeaviateDockerCompose.rbac(adminUser);

  @Before
  public void before() throws AuthException {
    Config config = new Config("http", compose.getHttpHostAddress());
    roles = WeaviateAuthClient.apiKey(config, Weaviate.makeSecret(adminUser)).roles();
  }

  public static Object[][] rolesToCreate() {
    return new Object[][] {
    };
  }

  /**
   * By default the admin user which we use to run the tests
   * will have 'admin' and 'viewer' roles.
   */
  @Test
  public void testGetAll() {
    Result<List<Role>> response = roles.allGetter().run();
    List<Role> all = response.getResult();

    assertThat(response.getError()).as("result had errors").isNull();
    assertThat(all).hasSize(2).as("wrong number of roles");
    assertThat(all.get(0)).returns(adminRole, Role::getName);
    assertThat(all.get(1)).returns(viewerRole, Role::getName);
  }

  // TODO: check if I can create a role with a name that's not a valid URL
  // paramter

  @Test
  public void testCreateAndList() {
    String myRole = roleName("VectorOwner");
    String myCollection = "Pizza";

    try {
      // Arrange
      roles.deleter().withName(myRole).run();
      roles.creator().withName(myRole)
          .withPermissions(
              Permission.backups(BackupsPermission.Action.MANAGE, myCollection),
              Permission.cluster(ClusterPermission.Action.READ),
              Permission.nodes(NodesPermission.Action.READ, Verbosity.MINIMAL, myCollection),
              Permission.roles(RolesPermission.Action.MANAGE, viewerRole),
              Permission.collections(CollectionsPermission.Action.CREATE, myCollection),
              Permission.data(DataPermission.Action.UPDATE, myCollection),
              Permission.tenants(TenantsPermission.Action.DELETE))
          .run();

      Result<Role> response = roles.getter().withName(myRole).run();
      Role role = response.getResult();
      assertThat(response.getError()).as("result had errors").isNull();
      assertThat(role).as("wrong role name").returns(myRole, Role::getName);

      List<? extends Permission<?>> permissions = role.getPermissions();
      assertTrue(hasPermissionWithAction(permissions, BackupsPermission.Action.MANAGE.getValue()));
      assertTrue(hasPermissionWithAction(permissions, ClusterPermission.Action.READ.getValue()));
      assertTrue(hasPermissionWithAction(permissions, NodesPermission.Action.READ.getValue()));
      assertTrue(hasPermissionWithAction(permissions, RolesPermission.Action.MANAGE.getValue()));
      assertTrue(hasPermissionWithAction(permissions, CollectionsPermission.Action.CREATE.getValue()));
      assertTrue(hasPermissionWithAction(permissions, DataPermission.Action.UPDATE.getValue()));
      assertTrue(hasPermissionWithAction(permissions, TenantsPermission.Action.DELETE.getValue()));
    } finally {
      roles.deleter().withName(myRole).run();
    }
  }

  /** Prefix the role with the name of the current test for easier debugging */
  private String roleName(String name) {
    return String.format("%s-%s", currentTest.getMethodName(), name);
  }

  private boolean hasPermissionWithAction(List<? extends Permission<?>> permissions, String action) {
    return permissions.stream()
        .filter(perm -> perm.getAction().equals(action))
        .findFirst().isPresent();
  }
}
