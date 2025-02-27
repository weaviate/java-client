package io.weaviate.integration.tests.users;

import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

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

import io.weaviate.client.Config;
import io.weaviate.client.base.Result;
import io.weaviate.client.v1.rbac.model.Permission;
import io.weaviate.client.v1.rbac.model.Role;
import io.weaviate.client.v1.rbac.model.TenantsPermission;
import io.weaviate.client.v1.users.model.User;
import io.weaviate.integration.client.WeaviateDockerImage;
import io.weaviate.integration.client.WeaviateWithRbacContainer;
import io.weaviate.integration.tests.rbac.ClientRbacTestSuite;

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
          { (Supplier<Users>) () -> new io.weaviate.integration.client.users.ClientUsersTest(config(), API_KEY) },
          { (Supplier<Users>) () -> new io.weaviate.integration.client.async.users.ClientUsersTest(config(), API_KEY) }
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
  public void testGetUserRoles(Supplier<Users> userHandle) {
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
  public void testAssignRevokeRole(Supplier<Users> userHandle) {
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
      assertFalse(checkHasRole(roles, adminUser, myRole), "should not have " + myRole + " role");
    } finally {
      roles.deleteRole(myRole);
    }
  }

  /** Prefix the role with the name of the current test for easier debugging */
  private String roleName(String name) {
    return String.format("%s-%s", currentTest.getMethodName(), name);
  }

  private boolean checkHasRole(Users roles, String user, String role) {
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
  }
}
