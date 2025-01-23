package io.weaviate.integration.client.rbac;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assumptions.assumeFalse;
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

    assertThat(response.getError()).as("get all roles error").isNull();
    assertThat(all).hasSize(2).as("wrong number of roles");
    assertThat(all.get(0)).returns(adminRole, Role::getName);
    assertThat(all.get(1)).returns(viewerRole, Role::getName);
  }

  @Test
  public void testGetUserRoles() {
    Result<List<Role>> responseCurrent = roles.userRolesGetter().run();
    assertThat(responseCurrent.getError()).as("get roles for current user error").isNull();
    Result<List<Role>> responseAdminUser = roles.userRolesGetter().withUser(adminUser).run();
    assertThat(responseAdminUser.getError()).as("get roles for user error").isNull();

    List<Role> currentRoles = responseCurrent.getResult();
    List<Role> adminRoles = responseAdminUser.getResult();

    Assertions.assertArrayEquals(currentRoles.toArray(), adminRoles.toArray(), "expect same set of roles");
  }

  public void testGetAssignedUsers() {
    Result<List<String>> response = roles.assignedUsersGetter().withRole(adminRole).run();
    assertThat(response.getError()).as("get assigned users error").isNull();

    List<String> users = response.getResult();
    assertThat(users).as("users assigned to " + adminRole + " role").hasSize(1);
    assertEquals(adminUser, users.get(0), "wrong user assinged to " + adminRole + " role");
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
      deleteRole(myRole);

      // Act
      createRole(myRole, wantPermissions);

      Result<Role> response = roles.getter().withName(myRole).run();
      Role role = response.getResult();
      assertNull("error fetching a role", response.getError());
      assertThat(role).as("wrong role name").returns(myRole, Role::getName);

      for (int i = 0; i < wantPermissions.length; i++) {
        Permission<?> perm = wantPermissions[i];
        assertTrue("should have permission " + perm, checkHasPermission(myRole, perm));
      }
    } finally {
      deleteRole(myRole);
    }
  }

  @Test
  public void testAddPermissions() {
    String myRole = roleName("VectorOwner");
    Permission<?> toAdd = Permission.cluster(ClusterPermission.Action.READ);
    try {
      // Arrange
      createRole(myRole, Permission.tenants(TenantsPermission.Action.DELETE));

      // Act
      Result<?> response = roles.permissionAdder().withRole(myRole)
          .withPermissions(toAdd)
          .run();
      assertNull("add-permissions operation error", response.getError());

      // Assert
      assertTrue("should have permission " + toAdd, checkHasPermission(myRole, toAdd));
    } finally {
      deleteRole(myRole);
    }
  }

  @Test
  public void testRemovePermissions() {
    String myRole = roleName("VectorOwner");
    Permission<?> toRemove = Permission.tenants(TenantsPermission.Action.DELETE);
    try {
      // Arrange
      createRole(myRole,
          Permission.cluster(ClusterPermission.Action.READ),
          Permission.tenants(TenantsPermission.Action.DELETE));

      // Act
      Result<?> response = roles.permissionRemover().withRole(myRole)
          .withPermissions(toRemove)
          .run();
      assertNull("remove-permissions operation error", response.getError());

      // Assert
      assertFalse("should not have permission " + toRemove, checkHasPermission(myRole, toRemove));
    } finally {
      deleteRole(myRole);
    }
  }

  @Test
  public void testRevokeRole() {
    String myRole = roleName("VectorOwner");
    try {
      // Arrange
      createRole(myRole, Permission.tenants(TenantsPermission.Action.DELETE));
      roles.assigner().withUser(adminUser).witRoles(myRole).run();
      assumeTrue(checkHasRole(adminUser, myRole), adminUser + " should have the assigned role");

      // Act
      Result<?> response = roles.revoker().withUser(adminUser).witRoles(myRole).run();
      assertNull("revoke operation error", response.getError());

      // Assert
      assertFalse("should not have " + myRole + "role", checkHasRole(adminUser, myRole));
    } finally {
      deleteRole(myRole);
    }
  }

  @Test
  public void testAssignRole() {
    String myRole = roleName("VectorOwner");
    try {
      // Arrange
      createRole(myRole, Permission.tenants(TenantsPermission.Action.DELETE));
      assumeFalse(checkHasRole(adminUser, myRole), adminUser + " should not have the new role");

      // Act
      Result<?> response = roles.assigner().withUser(adminUser).witRoles(myRole).run();
      assertNull("assign operation error", response.getError());

      // Assert
      assertTrue("should have " + myRole + "role", checkHasRole(adminUser, myRole));
    } finally {
      deleteRole(myRole);
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

  private boolean checkHasRole(String user, String role) {
    return roles.assignedUsersGetter().withRole(role).run().getResult().contains(user);
  }

  private void createRole(String role, Permission<?>... permissions) {
    roles.creator().withName(role).withPermissions(permissions).run();
    assumeTrue(checkRoleExists(role), "role should exist after creation");

  }

  private void deleteRole(String role) {
    roles.deleter().withName(role).run();
    assertFalse("role should not exist after deletion", checkRoleExists(role));

  }
}
