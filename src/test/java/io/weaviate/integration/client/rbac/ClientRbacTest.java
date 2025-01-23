package io.weaviate.integration.client.rbac;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.list;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.util.List;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.rules.TestName;

import io.weaviate.client.Config;
import io.weaviate.client.WeaviateAuthClient;
import io.weaviate.client.WeaviateClient;
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

  @Test
  public void testGetUserRoles() {
    Result<List<Role>> responseCurrent = roles.userRolesGetter().run();
    assertThat(responseCurrent.getError()).as("result had errors").isNull();
    Result<List<Role>> responseAdminUser = roles.userRolesGetter().withUser(adminUser).run();
    assertThat(responseAdminUser.getError()).as("result had errors").isNull();

    List<Role> currentRoles = responseCurrent.getResult();
    List<Role> adminRoles = responseAdminUser.getResult();

    Assertions.assertArrayEquals(currentRoles.toArray(), adminRoles.toArray(), "expect same set of roles");
  }

  // TODO: check if I can create a role with a name that's not a valid URL
  // paramter

  @Test
  public void testCreate() {
    String myRole = roleName("VectorOwner");
    String myCollection = "Pizza";

    Permission<?>[] wantPermissions = new Permission<?>[] {
        Permission.backups(BackupsPermission.Action.MANAGE, myCollection),
        Permission.cluster(ClusterPermission.Action.READ),
        Permission.nodes(NodesPermission.Action.READ, Verbosity.MINIMAL, myCollection),
        Permission.roles(RolesPermission.Action.MANAGE, viewerRole),
        Permission.collections(CollectionsPermission.Action.CREATE, myCollection),
        Permission.data(DataPermission.Action.UPDATE, myCollection),
        Permission.tenants(TenantsPermission.Action.DELETE),
    };

    try {
      // Arrange
      roles.deleter().withName(myRole).run();

      // Act
      roles.creator().withName(myRole)
          .withPermissions(wantPermissions)
          .run();
      assumeTrue(checkRoleExists(myRole), "role should exist after creation");

      Result<Role> response = roles.getter().withName(myRole).run();
      Role role = response.getResult();
      assertNull("error fetching a role", response.getError());
      assertThat(role).as("wrong role name").returns(myRole, Role::getName);

      for (int i = 0; i < wantPermissions.length; i++) {
        Permission<?> perm = wantPermissions[i];
        assertTrue("should have permission " + perm, checkHasPermission(myRole, perm));
      }
    } finally {
      roles.deleter().withName(myRole).run();
      assertFalse("should not exist after deletion", checkRoleExists(myRole));
    }
  }

  @Test
  public void testAddPermissions() {
    String myRole = roleName("VectorOwner");
    Permission<?> toAdd = Permission.cluster(ClusterPermission.Action.READ);
    try {
      // Arrange
      roles.creator().withName(myRole)
          .withPermissions(Permission.tenants(TenantsPermission.Action.DELETE))
          .run();
      assumeTrue(checkRoleExists(myRole), "role should exist after creation");

      // Act
      Result<?> addResult = roles.permissionAdder().withRole(myRole)
          .withPermissions(toAdd)
          .run();
      assertNull("add-permissions operation error", addResult.getError());

      // Assert
      assertTrue("should have permission " + toAdd, checkHasPermission(myRole, toAdd));
    } finally {
      roles.deleter().withName(myRole).run();
      assertFalse("should not exist after deletion", checkRoleExists(myRole));
    }
  }

  @Test
  public void testRemovePermissions() {
    String myRole = roleName("VectorOwner");
    Permission<?> toRemove = Permission.tenants(TenantsPermission.Action.DELETE);
    try {
      // Arrange
      roles.creator().withName(myRole)
          .withPermissions(
              Permission.cluster(ClusterPermission.Action.READ),
              Permission.tenants(TenantsPermission.Action.DELETE))
          .run();
      assumeTrue(checkRoleExists(myRole), "role should exist after creation");

      // Act
      Result<?> addResult = roles.permissionRemover().withRole(myRole)
          .withPermissions(toRemove)
          .run();
      assertNull("remove-permissions operation error", addResult.getError());

      // Assert
      assertFalse("should not have permission " + toRemove, checkHasPermission(myRole, toRemove));
    } finally {
      roles.deleter().withName(myRole).run();
      assertFalse("should not exist after deletion", checkRoleExists(myRole));
    }
  }

  /** Prefix the role with the name of the current test for easier debugging */
  private String roleName(String name) {
    return String.format("%s-%s", currentTest.getMethodName(), name);
  }

  private boolean checkHasPermission(String role, Permission<? extends Permission<?>> perm) {
    return roles.permissionChecker().withRole(role).withPermission(perm).run().getResult();
  }

  private boolean checkRoleExists(String role) {
    return roles.exists().withName(role).run().getResult();
  }
}
