package io.weaviate.client.v1.rbac.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.function.Function;
import java.util.function.Supplier;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.testcontainers.shaded.org.hamcrest.Matcher;
import org.testcontainers.shaded.org.hamcrest.MatcherAssert;
import org.testcontainers.shaded.org.hamcrest.beans.SamePropertyValuesAs;

import com.google.gson.Gson;
import com.jparams.junit4.JParamsTestRunner;
import com.jparams.junit4.data.DataMethod;

import io.weaviate.client.v1.rbac.api.WeaviatePermission;
import io.weaviate.client.v1.rbac.model.NodesPermission.Verbosity;

@RunWith(JParamsTestRunner.class)
public class PermissionTest {
  public static Object[][] serializationTestCases() {
    UsersPermission users = new UsersPermission(UsersPermission.Action.MANAGE);
    BackupsPermission backups = new BackupsPermission(BackupsPermission.Action.MANAGE, "Pizza");
    DataPermission data = new DataPermission(DataPermission.Action.MANAGE, "Pizza");
    NodesPermission nodes = new NodesPermission(NodesPermission.Action.READ, Verbosity.MINIMAL, "Pizza");
    RolesPermission roles = new RolesPermission(RolesPermission.Action.MANAGE, "TestWriter");
    CollectionsPermission collections = new CollectionsPermission(CollectionsPermission.Action.MANAGE, "Pizza");
    ClusterPermission cluster = new ClusterPermission(ClusterPermission.Action.READ);
    TenantsPermission tenants = new TenantsPermission(TenantsPermission.Action.READ);

    return new Object[][] {
        {
            "user permission",
            (Supplier<Permission<?>>) () -> users,
            new WeaviatePermission("manage_users"),
        },
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
            new WeaviatePermission("manage_roles", roles),
        },
        {
            "collections permission",
            (Supplier<Permission<?>>) () -> collections,
            new WeaviatePermission("manage_collections", collections),
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
    };
  }

  @DataMethod(source = PermissionTest.class, method = "serializationTestCases")
  @Test
  public void testToWeaviate(String name, Supplier<Permission<?>> permFunc, WeaviatePermission expected)
      throws Exception {
    Permission<?> perm = permFunc.get();
    MatcherAssert.assertThat(name, perm.toWeaviate(), sameAs(expected));
  }

  private static <T> Matcher<T> sameAs(T expected) {
    return new SamePropertyValuesAs<T>(expected, new ArrayList<>());
  }

  @Test
  public void testDefaultDataPermission() {
    DataPermission perm = new DataPermission(DataPermission.Action.MANAGE, "Pizza");
    assertThat(perm).as("data permission must have object=* and tenant=*")
        .returns("*", DataPermission::getObject)
        .returns("*", DataPermission::getTenant);
  }

  @Test
  public void testDefaultCollectionsPermission() {
    CollectionsPermission perm = new CollectionsPermission(CollectionsPermission.Action.MANAGE, "Pizza");
    assertThat(perm).as("collection permission must have tenant=*")
        .returns("*", CollectionsPermission::getTenant);
  }

  @Test
  public void testDefaultNodesPermission() {
    NodesPermission perm = new NodesPermission(NodesPermission.Action.READ, NodesPermission.Verbosity.MINIMAL);
    assertThat(perm).as("nodes permission should affect all collections if one is not specified")
        .returns("*", NodesPermission::getCollection);
  }

  @Test
  public void testDefaultTenantsPermission() {
    TenantsPermission perm = new TenantsPermission(TenantsPermission.Action.READ);
    assertThat(perm).as("tenants permission must have tenant=*")
        .returns("*", TenantsPermission::getTenant);
  }

  @DataMethod(source = PermissionTest.class, method = "serializationTestCases")
  @Test
  public void testFromWeaviate(String name,
      Supplier<Permission<?>> expectedFunc, WeaviatePermission input)
      throws Exception {
    Permission<?> expected = expectedFunc.get();
    Permission<?> actual = Permission.fromWeaviate(input);
    MatcherAssert.assertThat(name, actual, sameAs(expected));
  }
}
