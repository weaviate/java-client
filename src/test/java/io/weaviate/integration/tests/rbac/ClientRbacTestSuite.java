package io.weaviate.integration.tests.rbac;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.util.List;
import java.util.function.Supplier;

import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.rules.TestName;
import org.junit.runner.RunWith;

import com.jparams.junit4.JParamsTestRunner;
import com.jparams.junit4.data.DataMethod;

import io.weaviate.client.Config;
import io.weaviate.client.base.Result;
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

@RunWith(JParamsTestRunner.class)
public class ClientRbacTestSuite {

  private static final String adminRole = "admin";
  private static final String viewerRole = "viewer";
  private static final String adminUser = "john-doe";
  private static final String API_KEY = Weaviate.makeSecret(adminUser);

  @Rule
  public TestName currentTest = new TestName();

  @ClassRule
  public static WeaviateDockerCompose compose = WeaviateDockerCompose.rbac(adminUser);

  public static Config config() {
    return new Config("http", compose.getHttpHostAddress());
  }

  public static Object[][] clients() {
    try {
      return new Object[][] {
          { (Supplier<Rbac>) () -> new io.weaviate.integration.client.rbac.ClientRbacTest(config(), API_KEY) },
          { (Supplier<Rbac>) () -> new io.weaviate.integration.client.async.rbac.ClientRbacTest(config(), API_KEY) }
      };
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * By default the admin user which we use to run the tests
   * will have 'admin' and 'viewer' roles.
   */
  @DataMethod(source = ClientRbacTestSuite.class, method = "clients")
  @Test
  public void testGetAll(Supplier<Rbac> rbac) {
    Rbac roles = rbac.get();
    Result<List<Role>> response = roles.getAll();
    List<Role> all = response.getResult();

    assertThat(response.getError()).as("get all roles error").isNull();
    assertThat(all).hasSize(2).as("wrong number of roles");
    assertThat(all.get(0)).returns(adminRole, Role::getName);
    assertThat(all.get(1)).returns(viewerRole, Role::getName);
  }

  /**
   * Roles retrieved for "current user" should be identical to the ones
   * retrieved for them explicitly (by passing the username).
   */
  @DataMethod(source = ClientRbacTestSuite.class, method = "clients")
  @Test
  public void testGetUserRoles(Supplier<Rbac> rbac) {
    Rbac roles = rbac.get();
    Result<List<Role>> responseCurrent = roles.getUserRoles();
    assertThat(responseCurrent.getError()).as("get roles for current user error").isNull();
    Result<List<Role>> responseAdminUser = roles.getUserRoles(adminUser);
    assertThat(responseAdminUser.getError()).as("get roles for user error").isNull();

    List<Role> currentRoles = responseCurrent.getResult();
    List<Role> adminRoles = responseAdminUser.getResult();

    Assertions.assertArrayEquals(currentRoles.toArray(), adminRoles.toArray(), "expect same set of roles");
  }

  /** Admin user should have the admin role assigned to them. */
  public void testGetAssignedUsers(Supplier<Rbac> rbac) {
    Rbac roles = rbac.get();
    Result<List<String>> response = roles.getAssignedUsers(adminRole);
    assertThat(response.getError()).as("get assigned users error").isNull();

    List<String> users = response.getResult();
    assertThat(users).as("users assigned to " + adminRole + " role").hasSize(1);
    assertEquals(adminUser, users.get(0), "wrong user assinged to " + adminRole + " role");
  }

  // TODO: check if I can create a role with a name that's not a valid URL
  // paramter

  /**
   * Created role should have all of the permissions it was created with.
   * Tests addition and fetching the role to.
   */
  @DataMethod(source = ClientRbacTestSuite.class, method = "clients")
  @Test
  public void testCreate(Supplier<Rbac> rbac) {
    Rbac roles = rbac.get();
    String myRole = roleName("VectorOwner");
    String myCollection = "Pizza";

    Permission<?>[][] wantPermissions = new Permission<?>[][] {
        Permission.backups(BackupsPermission.Action.MANAGE, myCollection),
        Permission.cluster(ClusterPermission.Action.READ),
        Permission.nodes(myCollection, Verbosity.MINIMAL, NodesPermission.Action.READ),
        Permission.roles(viewerRole, RolesPermission.Action.MANAGE),
        Permission.collections(myCollection, CollectionsPermission.Action.CREATE),
        Permission.data(myCollection, DataPermission.Action.UPDATE),
        Permission.tenants(TenantsPermission.Action.DELETE),
    };

    try {
      // Arrange
      roles.deleteRole(myRole);

      // Act
      roles.createRole(myRole, wantPermissions);

      Result<Role> response = roles.getRole(myRole);
      Role role = response.getResult();
      assertNull("error fetching a role", response.getError());
      assertThat(role).as("wrong role name").returns(myRole, Role::getName);

      for (int i = 0; i < wantPermissions.length; i++) {
        Permission<?> perm = wantPermissions[i][0]; // We create each permission group with only 1 action
        assertTrue("should have permission " + perm, checkHasPermission(roles, myRole, perm));
      }
    } finally {
      roles.deleteRole(myRole);
    }
  }

  /**
   * Role can be extended with new permissions. We do not test the "upsert"
   * behavior because it is the server's responsibility.
   */
  @DataMethod(source = ClientRbacTestSuite.class, method = "clients")
  @Test
  public void testAddPermissions(Supplier<Rbac> rbac) {
    Rbac roles = rbac.get();
    String myRole = roleName("VectorOwner");
    Permission<?> toAdd = Permission.cluster(ClusterPermission.Action.READ)[0];
    try {
      // Arrange
      roles.createRole(myRole, Permission.tenants(TenantsPermission.Action.DELETE));

      // Act
      Result<?> response = roles.addPermissions(myRole, toAdd);
      assertNull("add-permissions operation error", response.getError());

      // Assert
      assertTrue("should have permission " + toAdd, checkHasPermission(roles, myRole, toAdd));
    } finally {
      roles.deleteRole(myRole);
    }
  }

  /**
   * Check query builder accepts arrays of permissions,
   * which is handy in combination with factory methods that create permissions
   * with multiple actions.
   */
  @DataMethod(source = ClientRbacTestSuite.class, method = "clients")
  @Test
  public void testAddPermissionsMultipleActions(Supplier<Rbac> rbac) {
    Rbac roles = rbac.get();
    String myRole = roleName("VectorOwner");
    Permission<?>[] toAdd = Permission.data("Pizza",
        DataPermission.Action.READ,
        DataPermission.Action.CREATE);
    try {
      // Arrange
      roles.createRole(myRole, Permission.collections("Pizza",
          CollectionsPermission.Action.UPDATE,
          CollectionsPermission.Action.DELETE));

      // Act
      Result<?> response = roles.addPermissions(myRole, toAdd);
      assertNull("add-permissions operation error", response.getError());

      // Assert
      for (Permission<?> perm : toAdd) {
        assertTrue("should have permission " + perm, checkHasPermission(roles, myRole, perm));
      }
    } finally {
      roles.deleteRole(myRole);
    }
  }

  /**
   * Permissions can be removed from a role.
   * We do not test the "downsert" behavior, because it is the server's
   * responsibility.
   */
  @DataMethod(source = ClientRbacTestSuite.class, method = "clients")
  @Test
  public void testRemovePermissions(Supplier<Rbac> rbac) {
    Rbac roles = rbac.get();
    String myRole = roleName("VectorOwner");
    Permission<?> toRemove = Permission.tenants(TenantsPermission.Action.DELETE)[0];
    try {
      // Arrange
      roles.createRole(myRole,
          Permission.cluster(ClusterPermission.Action.READ),
          Permission.tenants(TenantsPermission.Action.DELETE));

      // Act
      Result<?> response = roles.removePermissions(myRole, toRemove);
      assertNull("remove-permissions operation error", response.getError());

      // Assert
      assertFalse("should not have permission " + toRemove, checkHasPermission(roles, myRole, toRemove));
    } finally {
      roles.deleteRole(myRole);
    }
  }

  /**
   * Check query builder accepts arrays of permissions,
   * which is handy in combination with factory methods that create permissions
   * with multiple actions.
   */
  @DataMethod(source = ClientRbacTestSuite.class, method = "clients")
  @Test
  public void testRemovePermissionsMultipleAction(Supplier<Rbac> rbac) {
    Rbac roles = rbac.get();
    String myRole = roleName("VectorOwner");
    Permission<?>[] toRemove = Permission.data("Pizza",
        DataPermission.Action.READ,
        DataPermission.Action.CREATE);
    try {
      // Arrange
      roles.createRole(myRole,
          Permission.data("Pizza",
              DataPermission.Action.READ,
              DataPermission.Action.UPDATE,
              DataPermission.Action.DELETE,
              DataPermission.Action.CREATE),
          Permission.tenants(TenantsPermission.Action.DELETE));

      // Act
      Result<?> response = roles.removePermissions(myRole, toRemove);
      assertNull("remove-permissions operation error", response.getError());

      // Assert
      for (Permission<?> perm : toRemove) {
        assertFalse("should not have permission " + toRemove, checkHasPermission(roles, myRole, perm));
      }
    } finally {
      roles.deleteRole(myRole);
    }
  }

  /** User can be assigned a role and the role can be revoked. */
  @DataMethod(source = ClientRbacTestSuite.class, method = "clients")
  @Test
  public void testAssignRevokeRole(Supplier<Rbac> rbac) {
    Rbac roles = rbac.get();
    String myRole = roleName("VectorOwner");
    try {
      // Arrange
      roles.createRole(myRole, Permission.tenants(TenantsPermission.Action.DELETE));

      // Act: Assign
      roles.assignRoles(adminUser, myRole);
      assumeTrue(checkHasRole(roles, adminUser, myRole), adminUser + " should have the assigned role");

      // Act: Revoke
      Result<?> response = roles.revokeRoles(adminUser, myRole);
      assertNull("revoke operation error", response.getError());

      // Assert
      assertFalse("should not have " + myRole + " role", checkHasRole(roles, adminUser, myRole));
    } finally {
      roles.deleteRole(myRole);
    }
  }

  /** Prefix the role with the name of the current test for easier debugging */
  private String roleName(String name) {
    return String.format("%s-%s", currentTest.getMethodName(), name);
  }

  private boolean checkHasPermission(Rbac roles, String role, Permission<? extends Permission<?>> perm) {
    return roles.hasPermission(role, perm).getResult();
  }

  private boolean checkHasRole(Rbac roles, String user, String role) {
    return roles.getAssignedUsers(role).getResult().contains(user);
  }

  /**
   * Sync and async test suits should provide an implementation of this interface.
   * This way the test suite can be written once with very little
   * boilerplate/overhead.
   */
  public interface Rbac {
    Result<Role> getRole(String role);

    Result<List<Role>> getAll();

    Result<List<Role>> getUserRoles();

    Result<List<Role>> getUserRoles(String user);

    Result<List<String>> getAssignedUsers(String role);

    void createRole(String role, Permission<?>... permissions);

    void createRole(String role, Permission<?>[]... permissions);

    void deleteRole(String role);

    Result<Boolean> hasPermission(String role, Permission<?> perm);

    Result<Boolean> exists(String role);

    Result<?> addPermissions(String role, Permission<?>... permissions);

    Result<?> addPermissions(String role, Permission<?>[]... permissions);

    Result<?> removePermissions(String role, Permission<?>... permissions);

    Result<?> removePermissions(String role, Permission<?>[]... permissions);

    Result<?> assignRoles(String user, String... roles);

    Result<?> revokeRoles(String user, String... roles);
  }

}
