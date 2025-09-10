package io.weaviate.integration.tests.groups;

import java.util.List;
import java.util.function.Supplier;

import org.assertj.core.api.Assertions;
import org.assertj.core.api.InstanceOfAssertFactories;
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
import io.weaviate.client.v1.rbac.model.GroupAssignment;
import io.weaviate.client.v1.rbac.model.Role;
import io.weaviate.integration.client.WeaviateDockerImage;
import io.weaviate.integration.client.WeaviateWithRbacContainer;
import io.weaviate.integration.tests.rbac.ClientRbacTestSuite;

@RunWith(JParamsTestRunner.class)
public class ClientGroupsTestSuite {

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
              (Supplier<Oidc>) () -> new io.weaviate.integration.client.groups.ClientGroupsTest(config(), API_KEY) },
          { "async",
              (Supplier<Oidc>) () -> new io.weaviate.integration.client.async.groups.ClientGroupsTest(config(),
                  API_KEY) }
      };
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @DataMethod(source = ClientGroupsTestSuite.class, method = "clients")
  @Name("{0}")
  @Test
  public void testAssignGetRevoke(String _kind, Supplier<Oidc> oidcHandle) {
    Oidc oidc = oidcHandle.get();
    String groupId = "./assign-group";
    String[] roles = new String[] { "viewer", "admin" };

    oidc.revokeRoles(groupId, roles);
    Assertions.assertThat(oidc.getAssignedRoles(groupId))
        .extracting(Result::getResult, InstanceOfAssertFactories.LIST)
        .isEmpty();

    oidc.assignRoles(groupId, roles);
    Assertions.assertThat(oidc.getAssignedRoles(groupId))
        .extracting(Result::getResult, InstanceOfAssertFactories.list(Role.class))
        .extracting(Role::getName).containsOnly(roles);

    oidc.revokeRoles(groupId, roles);
    Assertions.assertThat(oidc.getAssignedRoles(groupId).getResult()).isEmpty();
  }

  @DataMethod(source = ClientGroupsTestSuite.class, method = "clients")
  @Name("{0}")
  @Test
  public void testGetAllKnownRoleGroups(String _kind, Supplier<Oidc> oidcHandle) {
    Oidc oidc = oidcHandle.get();
    String group1 = "./group-1";
    String group2 = "./group-2";

    oidc.assignRoles(group1, "viewer");
    oidc.assignRoles(group2, "viewer");

    Assertions.assertThat(oidc.getKnownGroupNames())
        .extracting(Result::getResult, InstanceOfAssertFactories.list(String.class))
        .containsOnly(group1, group2);

    oidc.revokeRoles(group1, "viewer");
    oidc.revokeRoles(group2, "viewer");

    Assertions.assertThat(oidc.getKnownGroupNames())
        .extracting(Result::getResult, InstanceOfAssertFactories.LIST)
        .isEmpty();
  }

  @DataMethod(source = ClientGroupsTestSuite.class, method = "clients")
  @Name("{0}")
  @Test
  public void testGetGroupAssignments(String _kind, Supplier<Oidc> oidcHandle) {
    Oidc oidc = oidcHandle.get();
    String role = roleName("testGroupAssignmentsRole");

    oidc.deleteRole(role);
    oidc.createRole(role);

    Assertions.assertThat(oidc.getGroupAssignments(role))
        .extracting(Result::getResult, InstanceOfAssertFactories.LIST)
        .isEmpty();

    oidc.assignRoles("./group-1", role);
    oidc.assignRoles("./group-2", role);

    Assertions.assertThat(oidc.getGroupAssignments(role))
        .extracting(Result::getResult, InstanceOfAssertFactories.list(GroupAssignment.class))
        .extracting(GroupAssignment::getGroupId)
        .containsOnly("./group-1", "./group-2");

    oidc.revokeRoles("./group-1", role);
    oidc.revokeRoles("./group-2", role);
    Assertions.assertThat(oidc.getGroupAssignments(role))
        .extracting(Result::getResult, InstanceOfAssertFactories.LIST)
        .isEmpty();
  }

  /** Prefix the role with the name of the current test for easier debugging */
  private String roleName(String name) {
    return String.format("%s-%s", currentTest.getMethodName(), name);
  }

  /**
   * Sync and async test suits should provide an implementation of this interface.
   * This way the test suite can be written once with very little
   * boilerplate/overhead.
   *
   * Extends {@link ClientRbacTestSuite.Rbac} because many tests require the
   * functionality for creating / deleting / verifying roles.
   */
  public interface Oidc extends ClientRbacTestSuite.Rbac {
    Result<List<Role>> getAssignedRoles(String groupId);

    Result<List<String>> getKnownGroupNames();

    Result<?> assignRoles(String groupId, String... roles);

    Result<?> revokeRoles(String groupId, String... roles);
  }
}
