package io.weaviate.integration.tests.users;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.rules.TestName;
import org.junit.runner.RunWith;
import org.testcontainers.weaviate.WeaviateContainer;

import com.jparams.junit4.JParamsTestRunner;
import com.jparams.junit4.data.DataMethod;
import com.jparams.junit4.description.Name;

import io.weaviate.client.Config;
import io.weaviate.client.WeaviateAuthClient;
import io.weaviate.client.WeaviateClient;
import io.weaviate.client.base.Result;
import io.weaviate.client.base.WeaviateError;
import io.weaviate.client.v1.rbac.model.BackupsPermission;
import io.weaviate.client.v1.rbac.model.Permission;
import io.weaviate.client.v1.rbac.model.Role;
import io.weaviate.client.v1.rbac.model.TenantsPermission;
import io.weaviate.client.v1.users.model.User;
import io.weaviate.client.v1.users.model.UserDb;
import io.weaviate.integration.client.WeaviateDockerImage;
import io.weaviate.integration.client.WeaviateWithRbacContainer;
import io.weaviate.integration.tests.rbac.ClientRbacTestSuite;
import io.weaviate.integration.tests.rbac.ClientRbacTestSuite.Rbac;

@RunWith(JParamsTestRunner.class)
public class ClientUsersTestSuite {

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
              (Supplier<Users>) () -> new io.weaviate.integration.client.users.ClientUsersTest(config(), API_KEY) },
          { "async",
              (Supplier<Users>) () -> new io.weaviate.integration.client.async.users.ClientUsersTest(config(),
                  API_KEY) }
      };
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Roles retrieved for "current user" should be identical to the ones
   * retrieved for them explicitly (by passing the username).
   */
  @DataMethod(source = ClientUsersTestSuite.class, method = "clients")
  @Test
  public void testGetUserRoles(String _kind, Supplier<Users> userHandle) {
    Users users = userHandle.get();
    Result<User> myUser = users.getMyUser();
    assertNull("get my user error", myUser.getError());
    Result<List<Role>> responseAdminUser = users.getUserRoles(adminUser);
    assertNull("get roles for user error", responseAdminUser.getError());

    List<Role> currentRoles = myUser.getResult().getRoles().values().stream().collect(Collectors.toList());
    List<Role> adminRoles = responseAdminUser.getResult();

    Assertions.assertArrayEquals(currentRoles.toArray(), adminRoles.toArray(),
        "expect same set of roles");
  }

  /** User can be assigned a role and the role can be revoked. */
  @DataMethod(source = ClientUsersTestSuite.class, method = "clients")
  @Test
  public void testAssignRevokeRole(String _kind, Supplier<Users> userHandle) {
    Users roles = userHandle.get();
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

  /** Admin can control the entire lifecycle of a 'db' user. */
  @DataMethod(source = ClientUsersTestSuite.class, method = "clients")
  @Name("{0}")
  @Test
  public void testUserLifecycle_db(String _kind, Supplier<Users> usersHandle) {
    DbUsers db = usersHandle.get().db();

    Result<?> created = db.create("dynamic-dave");
    assertNull("create user", created.getError());

    UserDb dave = db.getUser("dynamic-dave").getResult();
    assertTrue("created user is active", dave.isActive());

    boolean ok = db.activate("dynamic-dave").getResult();
    assertTrue("second activation is a no-op", ok);

    db.deactivate("dynamic-dave", true);
    dave = db.getUser("dynamic-dave").getResult();
    assertFalse("user deactivated", dave.isActive());

    ok = db.deactivate("dynamic-dave", true).getResult();
    assertTrue("second deactivation is a no-op", ok);

    db.delete("dynamic-dave");
    WeaviateError error = db.getUser("dynamic-dave").getError();
    assertEquals(404, error.getStatusCode(), "user not found after deletion");
  }

  /** Admin can obtain and rotate API keys for users. */
  @DataMethod(source = ClientUsersTestSuite.class, method = "clients")
  @Name("{0}")
  @Test
  public void testRotateApiKeys_db(String _kind, Supplier<Users> usersHandle) {
    DbUsers db = usersHandle.get().db();

    Result<String> created = db.create("api-ashley");
    assertNull("create user", created.getError());

    String apiKey = created.getResult();
    // It doesn't matter that we're using a sync client here,
    // as we only want to check that the key is valid.
    WeaviateClient clientAshley = assertDoesNotThrow(() -> WeaviateAuthClient.apiKey(config(), apiKey),
        "connect with api key");

    User ashley = clientAshley.users().myUserGetter().run().getResult();
    assertEquals(ashley.getUserId(), "api-ashley");

    String newKey = db.rotateKey("api-ashley").getResult();
    clientAshley = assertDoesNotThrow(() -> WeaviateAuthClient.apiKey(config(), newKey), "connect with new api key");

    ashley = clientAshley.users().myUserGetter().run().getResult();
    assertEquals(ashley.getUserId(), "api-ashley");

    db.delete("api-ashley");
  }

  /** Admin can list dynamic users. */
  @DataMethod(source = ClientUsersTestSuite.class, method = "clients")
  @Name("{0}")
  @Test
  public void testListUsers_db(String _kind, Supplier<Users> usersHandle) {
    DbUsers db = usersHandle.get().db();

    Arrays.asList("jim", "pam", "dwight").forEach(u -> db.create(u));

    List<UserDb> all = db.getAll().getResult();
    // 3 created + admin user defined in WeaviateWithRbacContainer
    assertEquals(4, all.size(), "expected number of dynamic users");

    UserDb pam = db.getUser("pam").getResult();
    assertTrue("pam is one of the users", all.contains(pam));

    Arrays.asList("jim", "pam", "dwight").forEach(u -> db.delete(u));
  }

  @DataMethod(source = ClientUsersTestSuite.class, method = "clients")
  @Name("{0}")
  @Test
  public void testFetchStaticUsers_db(String _kind, Supplier<Users> usersHandle) {
    DbUsers db = usersHandle.get().db();
    UserDb envUser = db.getUser(adminUser).getResult();
    assertEquals("db_env_user", envUser.getUserType());
  }

  @DataMethod(source = ClientUsersTestSuite.class, method = "clients")
  @Name("{0}")
  @Test
  public void testAssignRevokeRole_db(String _kind, Supplier<Users> usersHandle) {
    Rbac rbac = usersHandle.get();
    DbUsers db = usersHandle.get().db();

    db.create("role-rick");
    rbac.createRole("TestRole");

    db.assignRoles("role-rick", "TestRole");
    assertTrue("role-rick has TestRole",
        checkHasRole(rbac, "role-rick", "TestRole"));

    db.revokeRoles("role-rick", "TestRole");
    assertFalse("TestRole is revoked",
        checkHasRole(rbac, "role-rick", "TestRole"));

    db.delete("role-rick");
    rbac.deleteRole("TestRole");
  }

  @DataMethod(source = ClientUsersTestSuite.class, method = "clients")
  @Name("{0}")
  @Test
  public void testAssignRevokeRole_oidc(String _kind, Supplier<Users> usersHandle) {
    Rbac rbac = usersHandle.get();
    OidcUsers oidc = usersHandle.get().oidc();
    rbac.createRole("TestRole");

    oidc.assignRoles("role-rick", "TestRole");
    assertTrue("role-rick has TestRole",
        checkHasRole(rbac, "role-rick", "TestRole"));

    oidc.revokeRoles("role-rick", "TestRole");
    assertFalse("TestRole is revoked",
        checkHasRole(rbac, "role-rick", "TestRole"));

    rbac.deleteRole("TestRole");
  }

  @DataMethod(source = ClientUsersTestSuite.class, method = "clients")
  @Name("{0}")
  @Test
  public void testFetchAssignedRolesWithPermissions(String _kind, Supplier<Users> usersHandle) {
    Rbac rbac = usersHandle.get();
    DbUsers db = usersHandle.get().db();

    rbac.createRole("TestRole",
        Permission.backups("Pizza", BackupsPermission.Action.MANAGE),
        Permission.tenants(TenantsPermission.Action.READ));
    db.create("permission-peter");
    db.assignRoles("permission-peter", "TestRole");

    List<Role> roles = db.assignedRoles("permission-peter", true).getResult();
    assertEquals(1, roles.size(), "expected n. of roles");
    Role testRole = roles.get(0);
    assertEquals(2, testRole.permissions.size(), "expected n. of permissions");

    db.delete("permission-peter");
    rbac.deleteRole("TestRole");
  }

  /** Prefix the role with the name of the current test for easier debugging */
  private String roleName(String name) {
    return String.format("%s-%s", currentTest.getMethodName(), name);
  }

  private boolean checkHasRole(Rbac roles, String user, String role) {
    return roles.getAssignedUsers(role).getResult().contains(user);
  }

  /**
   * Sync and async test suits should provide an implementation of this interface.
   * This way the test suite can be written once with very little
   * boilerplate/overhead.
   *
   * Extends {@link ClientRbacTestSuite.Rbac} because many tests require the
   * functionality for creating / deleting / verifying roles.
   */
  public interface Users extends ClientRbacTestSuite.Rbac {
    Result<User> getMyUser();

    Result<List<Role>> getUserRoles(String user);

    Result<?> assignRoles(String user, String... roles);

    Result<?> revokeRoles(String user, String... roles);

    DbUsers db();

    OidcUsers oidc();
  }

  public interface DbUsers {
    Result<?> assignRoles(String user, String... roles);

    Result<?> revokeRoles(String user, String... roles);

    Result<List<Role>> assignedRoles(String user, boolean includePermissions);

    Result<String> create(String user);

    Result<String> rotateKey(String user);

    Result<Boolean> delete(String user);

    Result<Boolean> activate(String user);

    Result<Boolean> deactivate(String user, boolean revokeKey);

    Result<UserDb> getUser(String user);

    Result<List<UserDb>> getAll();
  }

  public interface OidcUsers {
    Result<?> assignRoles(String user, String... roles);

    Result<?> revokeRoles(String user, String... roles);

    Result<List<Role>> assignedRoles(String user, boolean includePermissions);
  }
}
