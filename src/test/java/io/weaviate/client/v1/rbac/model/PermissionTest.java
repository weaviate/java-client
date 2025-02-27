package io.weaviate.client.v1.rbac.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import java.util.ArrayList;
import java.util.function.Supplier;

import org.junit.Test;
import org.junit.function.ThrowingRunnable;
import org.junit.runner.RunWith;
import org.testcontainers.shaded.org.hamcrest.Matcher;
import org.testcontainers.shaded.org.hamcrest.MatcherAssert;
import org.testcontainers.shaded.org.hamcrest.beans.SamePropertyValuesAs;

import com.jparams.junit4.JParamsTestRunner;
import com.jparams.junit4.data.DataMethod;
import com.jparams.junit4.description.Name;

import io.weaviate.client.v1.rbac.api.WeaviatePermission;

@RunWith(JParamsTestRunner.class)
public class PermissionTest {
  public static Object[][] serializationTestCases() {
    BackupsPermission backups = new BackupsPermission("Pizza", BackupsPermission.Action.MANAGE);
    DataPermission data = new DataPermission("Pizza", DataPermission.Action.MANAGE);
    NodesPermission nodes = new NodesPermission("Pizza", NodesPermission.Action.READ);
    RolesPermission roles = new RolesPermission("TestWriter", RolesPermission.Action.CREATE);
    CollectionsPermission collections = new CollectionsPermission("Pizza", CollectionsPermission.Action.CREATE);
    ClusterPermission cluster = new ClusterPermission(ClusterPermission.Action.READ);
    TenantsPermission tenants = new TenantsPermission(TenantsPermission.Action.READ);
    UsersPermission users = new UsersPermission(UsersPermission.Action.READ);

    return new Object[][] {
        {
            "backup permission",
            (Supplier<Permission<?>>) () -> backups,
            new WeaviatePermission("manage_backups", backups),
        },
        {
            "data permission",
            (Supplier<Permission<?>>) () -> data,
            new WeaviatePermission("manage_data", data),
        },
        {
            "nodes permission",
            (Supplier<Permission<?>>) () -> nodes,
            new WeaviatePermission("read_nodes", nodes),
        },
        {
            "roles permission",
            (Supplier<Permission<?>>) () -> roles,
            new WeaviatePermission("create_roles", roles),
        },
        {
            "collections permission",
            (Supplier<Permission<?>>) () -> collections,
            new WeaviatePermission("create_collections", collections),
        },
        {
            "cluster permission",
            (Supplier<Permission<?>>) () -> cluster,
            new WeaviatePermission("read_cluster"),
        },
        {
            "tenants permission",
            (Supplier<Permission<?>>) () -> tenants,
            new WeaviatePermission("read_tenants", tenants),
        },
        {
            "users permission",
            (Supplier<Permission<?>>) () -> users,
            new WeaviatePermission("read_users", users),
        },
    };
  }

  @DataMethod(source = PermissionTest.class, method = "serializationTestCases")
  @Name("{0}")
  @Test
  public void testFirstToWeaviate(String name, Supplier<Permission<?>> permFunc, WeaviatePermission expected)
      throws Exception {
    Permission<?> perm = permFunc.get();
    MatcherAssert.assertThat(name, perm.firstToWeaviate(), sameAs(expected));
  }

  private static <T> Matcher<T> sameAs(T expected) {
    return new SamePropertyValuesAs<T>(expected, new ArrayList<>());
  }

  @Test
  public void testDefaultNodesPermission() {
    NodesPermission perm = new NodesPermission(NodesPermission.Verbosity.MINIMAL, NodesPermission.Action.READ);
    assertThat(perm).as("nodes permission should affect all collections if one is not specified")
        .returns("*", NodesPermission::getCollection);
  }

  @Test
  public void testDefaultRolesPermission() {
    RolesPermission perm = new RolesPermission("ExampleRole", RolesPermission.Action.READ);
    assertThat(perm).as("roles permission must have scope=null")
        .returns(null, RolesPermission::getScope);
  }

  @DataMethod(source = PermissionTest.class, method = "serializationTestCases")
  @Name("{0}")
  @Test
  public void testFromWeaviate(String name,
      Supplier<Permission<?>> expectedFunc, WeaviatePermission input)
      throws Exception {
    Permission<?> expected = expectedFunc.get();
    Permission<?> actual = Permission.fromWeaviate(input);
    MatcherAssert.assertThat(name, actual, sameAs(expected));
  }

  /**
   * groupedConstructors returns test cases for overloaded factory methods, which
   * allow creating multiple permission entries for the same resource.
   *
   * Permission types which only have 1 possible action (e.g. backup/cluster
   * permissions) are omitted.
   */
  public static Object[][] groupedConstructors() {
    return new Object[][] {
        {
            Permission.collections("Pizza",
                CollectionsPermission.Action.CREATE,
                CollectionsPermission.Action.READ,
                CollectionsPermission.Action.DELETE),
            new String[] {
                "create_collections",
                "read_collections",
                "delete_collections",
            },
        },
        {
            Permission.data("Pizza",
                DataPermission.Action.CREATE,
                DataPermission.Action.READ,
                DataPermission.Action.DELETE),
            new String[] {
                "create_data",
                "read_data",
                "delete_data",
            },
        },
        {
            Permission.roles("TestRole",
                RolesPermission.Action.READ,
                RolesPermission.Action.UPDATE),
            new String[] {
                "read_roles",
                "update_roles",
            },
        },
    };
  }

  @DataMethod(source = PermissionTest.class, method = "groupedConstructors")
  @Name("{0}")
  @Test
  public void testGroupedConstructors(Permission<? extends Permission<?>> permission, String[] expectedActions) {
    Object[] actualActions = permission.getActions().toArray();
    assertArrayEquals(expectedActions, actualActions, "set of allowed actions do not match");
  }

  public static Object[][] deprecatedActions() {
    return new Object[][] {
        { (ThrowingRunnable) () -> Permission.roles("AnyRole", RolesPermission.Action.MANAGE) },
    };
  }

  /**
   * Passing deprecated actions, e.g {@link RolesPermission.Action.MANAGE}, will
   * result in an error, and we can prevent that sooner.
   */
  @DataMethod(source = PermissionTest.class, method = "deprecatedActions")
  @Test
  public void testDeprecatedActions(ThrowingRunnable r) {
    assertThrows(IllegalArgumentException.class, r);

  }
}
