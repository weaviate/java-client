package io.weaviate.integration;

import java.io.IOException;
import java.util.Arrays;

import org.assertj.core.api.Assertions;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.Test;

import io.weaviate.ConcurrentTest;
import io.weaviate.client6.v1.api.Authentication;
import io.weaviate.client6.v1.api.WeaviateClient;
import io.weaviate.client6.v1.api.rbac.AliasPermission;
import io.weaviate.client6.v1.api.rbac.BackupsPermission;
import io.weaviate.client6.v1.api.rbac.ClusterPermission;
import io.weaviate.client6.v1.api.rbac.CollectionsPermission;
import io.weaviate.client6.v1.api.rbac.DataPermission;
import io.weaviate.client6.v1.api.rbac.GroupsPermission;
import io.weaviate.client6.v1.api.rbac.NodesPermission;
import io.weaviate.client6.v1.api.rbac.Permission;
import io.weaviate.client6.v1.api.rbac.ReplicatePermission;
import io.weaviate.client6.v1.api.rbac.Role;
import io.weaviate.client6.v1.api.rbac.RolesPermission;
import io.weaviate.client6.v1.api.rbac.RolesPermission.Scope;
import io.weaviate.client6.v1.api.rbac.TenantsPermission;
import io.weaviate.client6.v1.api.rbac.UsersPermission;
import io.weaviate.client6.v1.api.rbac.roles.UserAssignment;
import io.weaviate.client6.v1.api.rbac.users.UserType;
import io.weaviate.containers.Weaviate;

public class RbacITest extends ConcurrentTest {
  private static final String ADMIN_USER = "admin-alex";
  private static final String API_KEY = "admin-alex-secret";

  /** Name of the root role, which exists by default. */
  private static final String ROOT_ROLE = "root";

  /** Name of the admin role, which exists by default. */
  private static final String ADMIN_ROLE = "admin";

  /** Name of the viewer role, which exists by default. */
  private static final String VIEWER_ROLE = "viewer";

  private static final WeaviateClient client = Weaviate.custom()
      .withAdminUsers(ADMIN_USER)
      .withApiKeys(API_KEY)
      .withRbac()
      .withOIDC( // Enable OIDC to have Weaviate return different user types (db, db_env, oidc)
          "wcs",
          "https://auth.wcs.api.weaviate.io/auth/realms/SeMI",
          "email",
          "groups")
      .build()
      .getClient(fn -> fn.authentication(Authentication.apiKey(API_KEY)));

  @Test
  public void test_roles_Lifecycle() throws IOException {
    // Arrange
    var myCollection = "Things";
    var nsRole = ns("VectorOwner");

    Permission[] permissions = new Permission[] {
        Permission.alias("ThingsAlias", myCollection, AliasPermission.Action.CREATE),
        Permission.backups(myCollection, BackupsPermission.Action.MANAGE),
        Permission.cluster(ClusterPermission.Action.READ),
        Permission.nodes(myCollection, NodesPermission.Action.READ),
        Permission.roles(VIEWER_ROLE, Scope.MATCH, RolesPermission.Action.CREATE),
        Permission.collections(myCollection, CollectionsPermission.Action.CREATE),
        Permission.data(myCollection, DataPermission.Action.UPDATE),
        Permission.groups("my-group", "oidc", GroupsPermission.Action.READ),
        Permission.tenants(myCollection, "my-tenant", TenantsPermission.Action.DELETE),
        Permission.users("my-user", UsersPermission.Action.READ),
        Permission.replicate(myCollection, "my-shard", ReplicatePermission.Action.READ),
    };

    // Act: create role
    client.roles.create(nsRole, permissions);

    var role = client.roles.get(nsRole);
    Assertions.assertThat(role)
        .as("created role")
        .returns(nsRole, Role::name)
        .extracting(Role::permissions, InstanceOfAssertFactories.list(Permission.class))
        .containsAll(Arrays.asList(permissions));

    // Act:: add extra permissions
    var extra = new Permission[] {
        Permission.data("Songs", DataPermission.Action.DELETE),
        Permission.users("john-doe", UsersPermission.Action.ASSIGN_AND_REVOKE),
    };
    client.roles.addPermissions(nsRole, extra);

    Assertions.assertThat(client.roles.hasPermission(nsRole, extra[0]))
        .as("has extra data permission")
        .isTrue();

    Assertions.assertThat(client.roles.hasPermission(nsRole, extra[1]))
        .as("has extra users permission")
        .isTrue();

    // Act: remove extra permissions
    client.roles.removePermissions(nsRole, extra);

    Assertions.assertThat(client.roles.hasPermission(nsRole, extra[0]))
        .as("extra data permission removed")
        .isFalse();

    Assertions.assertThat(client.roles.hasPermission(nsRole, extra[1]))
        .as("extra users permission removed")
        .isFalse();

    // Act: delete role
    client.roles.delete(nsRole);
    Assertions.assertThat(client.roles.exists(nsRole))
        .as("role is deleted")
        .isFalse();
  }

  @Test
  public void test_roles_list() throws IOException {
    Assertions.assertThat(client.roles.list())
        .extracting(Role::name)
        .contains(ROOT_ROLE, ADMIN_ROLE, VIEWER_ROLE);
  }

  @Test
  public void test_roles_assignedUsers() throws IOException {
    Assertions.assertThat(client.roles.assignedUserIds(ROOT_ROLE))
        .hasSize(1)
        .containsOnly(ADMIN_USER);
  }

  @Test
  public void test_roles_userAssignments() throws IOException {
    var assignments = client.roles.userAssignments(ROOT_ROLE);
    Assertions.assertThat(assignments)
        .hasSize(2)
        .extracting(UserAssignment::userId)
        .containsOnly(ADMIN_USER);

    Assertions.assertThat(assignments)
        .extracting(UserAssignment::userType)
        .containsOnly(UserType.DB_ENV_USER, UserType.OIDC);
  }

  @Test
  public void test_groups() throws IOException {
    var mediaGroup = "./media-group";
    var friendGroup = "./friend-group";

    client.groups.assignRoles(mediaGroup, VIEWER_ROLE);
    client.groups.assignRoles(friendGroup, ADMIN_ROLE, VIEWER_ROLE);

    Assertions.assertThat(client.groups.assignedRoles(friendGroup))
        .as("assigned to " + friendGroup)
        .extracting(Role::name)
        .containsOnly(ADMIN_ROLE, VIEWER_ROLE);

    Assertions.assertThat(client.groups.knownGroupNames())
        .as("known group names")
        .contains(mediaGroup, friendGroup);

    client.groups.revokeRoles(mediaGroup, VIEWER_ROLE);
    Assertions.assertThat(client.groups.knownGroupNames())
        .as("know group names (no root)")
        .doesNotContain(mediaGroup);

  }
}
