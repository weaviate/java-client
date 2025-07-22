package io.weaviate.integration.tests.rbac;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.junit.runner.RunWith;
import org.testcontainers.weaviate.WeaviateContainer;

import com.jparams.junit4.JParamsTestRunner;
import com.jparams.junit4.data.DataMethod;
import com.jparams.junit4.description.Name;

import io.weaviate.client.Config;
import io.weaviate.client.base.Result;
import io.weaviate.client.v1.rbac.model.AliasPermission;
import io.weaviate.client.v1.rbac.model.BackupsPermission;
import io.weaviate.client.v1.rbac.model.ClusterPermission;
import io.weaviate.client.v1.rbac.model.CollectionsPermission;
import io.weaviate.client.v1.rbac.model.DataPermission;
import io.weaviate.client.v1.rbac.model.NodesPermission;
import io.weaviate.client.v1.rbac.model.Permission;
import io.weaviate.client.v1.rbac.model.Role;
import io.weaviate.client.v1.rbac.model.RolesPermission;
import io.weaviate.client.v1.rbac.model.TenantsPermission;
import io.weaviate.client.v1.rbac.model.UserAssignment;
import io.weaviate.integration.client.WeaviateDockerImage;
import io.weaviate.integration.client.WeaviateWithRbacContainer;

@RunWith(JParamsTestRunner.class)
public class ClientRbacTestSuite {

  private static final String adminRole = "admin";
  private static final String rootRole = "root";
  private static final String viewerRole = "viewer";
  private static final String adminUser = "john-doe";
  private static final String API_KEY = WeaviateWithRbacContainer.makeSecret(adminUser);

  @Rule
  public TestName currentTest = new TestName();

  @ClassRule
  public static WeaviateContainer weaviate = new WeaviateWithRbacContainer(
      WeaviateDockerImage.WEAVIATE_DOCKER_IMAGE,
      adminUser);

  public static Config config() {
    return new Config("http", weaviate.getHttpHostAddress());
  }

  public static Object[][] clients() {
    try {
      return new Object[][] {
          { "sync",
              (Supplier<Rbac>) () -> new io.weaviate.integration.client.rbac.ClientRbacTest(config(), API_KEY) },
          { "async",
              (Supplier<Rbac>) () -> new io.weaviate.integration.client.async.rbac.ClientRbacTest(config(), API_KEY) }
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
  @Name("{class}/client={0} ")
  @Test
  public void testGetAll(String _name, Supplier<Rbac> rbac) {
    Rbac roles = rbac.get();
    Result<List<Role>> response = roles.getAll();
    List<Role> all = response.getResult();

    assertThat(response.getError()).as("get all roles error").isNull();
    assertThat(all).hasSize(3).as("wrong number of roles");
    assertThat(all.get(0)).returns(adminRole, Role::getName);
    assertThat(all.get(1)).returns(rootRole, Role::getName);
    assertThat(all.get(2)).returns(viewerRole, Role::getName);
  }

  /** Admin user should have the admin role assigned to them. */
  @DataMethod(source = ClientRbacTestSuite.class, method = "clients")
  @Name("{class}/client={0} ")
  @Test
  public void testGetAssignedUsers(String _name, Supplier<Rbac> rbac) {
    Rbac roles = rbac.get();
    Result<List<String>> response = roles.getAssignedUsers(rootRole);
    assertThat(response.getError()).as("get assigned users error").isNull();

    List<String> users = response.getResult();
    assertThat(users).as("users assigned to " + rootRole + " role").hasSize(1);
    assertEquals(adminUser, users.get(0), "wrong user assinged to " + rootRole + " role");
  }

  /** Admin user should have the admin role assigned to them. */
  @DataMethod(source = ClientRbacTestSuite.class, method = "clients")
  @Name("{class}/client={0} ")
  @Test
  public void testGetUserAssignments(String _name, Supplier<Rbac> rbac) {
    Rbac roles = rbac.get();
    Result<List<UserAssignment>> response = roles.getUserAssignments(rootRole);
    assertThat(response.getError()).as("get assigned users error").isNull();

    List<UserAssignment> users = response.getResult();
    // If OIDC is enabled, db / db_env users will appear in the list twice:
    // once as 'db" and once as an 'oidc' user.
    assertThat(users).as("users assignments to " + rootRole + " role").hasSize(2);
    assertEquals(adminUser, users.get(0).getUserId(), "wrong user assinged to " + rootRole + " role");
    assertArrayEquals(new String[] { "db_env_user", "oidc" },
        users.stream().map(UserAssignment::getUserType).sorted().toArray());
  }

  /**
   * Created role should have all of the permissions it was created with.
   * Tests addition and fetching the role to.
   */
  @DataMethod(source = ClientRbacTestSuite.class, method = "clients")
  @Name("{class}/client={0} ")
  @Test
  public void testCreate(String _name, Supplier<Rbac> rbac) {
    Rbac roles = rbac.get();
    String myRole = roleName("VectorOwner");
    String myCollection = "Pizza";
    String myCollectionAlias = "PizzaAlias";

    Permission<?>[] wantPermissions = new Permission<?>[] {
        Permission.alias(myCollectionAlias, myCollection, AliasPermission.Action.CREATE),
        Permission.backups(myCollection, BackupsPermission.Action.MANAGE),
        Permission.cluster(ClusterPermission.Action.READ),
        Permission.nodes(myCollection, NodesPermission.Action.READ),
        Permission.roles(viewerRole, RolesPermission.Action.CREATE),
        Permission.collections(myCollection, CollectionsPermission.Action.CREATE),
        Permission.data(myCollection, DataPermission.Action.UPDATE),
        Permission.tenants(TenantsPermission.Action.DELETE),
    };

    try {
      // Arrange
      roles.deleteRole(myRole);

      // Act
      Result<Boolean> create = roles.createRole(myRole, wantPermissions);
      assertNull("error creating role", create.getError());
      assertTrue("created successfully", create.getResult());

      Result<Role> response = roles.getRole(myRole);
      Role role = response.getResult();
      assertNull("error fetching a role", response.getError());
      assertThat(role).as("wrong role name").returns(myRole, Role::getName);

      Arrays.stream(wantPermissions).forEach(perm -> {
        assertTrue("should have permission " + perm, checkHasPermission(roles, myRole, perm));
      });
    } finally {
      roles.deleteRole(myRole);
    }
  }

  /**
   * Role can be extended with new permissions. We do not test the "upsert"
   * behavior because it is the server's responsibility.
   */
  @DataMethod(source = ClientRbacTestSuite.class, method = "clients")
  @Name("{class}/client={0} ")
  @Test
  public void testAddPermissions(String _name, Supplier<Rbac> rbac) {
    Rbac roles = rbac.get();
    String myRole = roleName("VectorOwner");
    Permission<?> toAdd = Permission.cluster(ClusterPermission.Action.READ);
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
  @Name("{class}/client={0} ")
  @Test
  public void testAddPermissionsMultipleActions(String _name, Supplier<Rbac> rbac) {
    Rbac roles = rbac.get();
    String myRole = roleName("VectorOwner");
    Permission<?> toAdd = Permission.data("Pizza",
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
      assertTrue("should have permission " + toAdd, checkHasPermission(roles, myRole, toAdd));
    } finally

    {
      roles.deleteRole(myRole);
    }
  }

  /**
   * Permissions can be removed from a role.
   * We do not test the "downsert" behavior, because it is the server's
   * responsibility.
   */
  @DataMethod(source = ClientRbacTestSuite.class, method = "clients")
  @Name("{class}/client={0} ")
  @Test
  public void testRemovePermissions(String _name, Supplier<Rbac> rbac) {
    Rbac roles = rbac.get();
    String myRole = roleName("VectorOwner");
    Permission<?> toRemove = Permission.tenants(TenantsPermission.Action.DELETE);
    try {
      // Arrange
      roles.createRole(myRole,
          // Create an extra permission so that the role would not be
          // deleted with its otherwise only permission is removed.
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
  @Name("{class}/client={0} ")
  @Test
  public void testRemovePermissionsMultipleAction(String _name, Supplier<Rbac> rbac) {
    Rbac roles = rbac.get();
    String myRole = roleName("VectorOwner");
    Permission<?> toRemove = Permission.data("Pizza",
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
      assertFalse("should not have permission " + toRemove, checkHasPermission(roles, myRole, toRemove));
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

  /**
   * Sync and async test suits should provide an implementation of this interface.
   * This way the test suite can be written once with very little
   * boilerplate/overhead.
   */
  public interface Rbac {
    Result<Role> getRole(String role);

    Result<List<Role>> getAll();

    Result<List<String>> getAssignedUsers(String role);

    Result<List<UserAssignment>> getUserAssignments(String role);

    Result<Boolean> createRole(String role, Permission<?>... permissions);

    void deleteRole(String role);

    Result<Boolean> hasPermission(String role, Permission<?> perm);

    Result<Boolean> exists(String role);

    Result<?> addPermissions(String role, Permission<?>... permissions);

    Result<?> removePermissions(String role, Permission<?>... permissions);

  }
}
