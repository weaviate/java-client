package io.weaviate.client.v1.rbac.api;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import java.util.Arrays;

import org.junit.Test;

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
import io.weaviate.client.v1.rbac.model.UsersPermission;

public class WeaviatePermissionTest {
  /**
   * When serialized to the API request body, permissions must be "flattened",
   * i.e. a single action per permission. When the response is deserialised,
   * permissions with for the same resource should be grouped together.
   */
  @Test
  public void testMergedPermissions() {
    WeaviatePermission[] apiPermissions = {
        // Create and delete PizzaAlias alias
        new WeaviatePermission("create_aliases", new AliasPermission("PizzaAlias", "Pizza")),
        new WeaviatePermission("delete_aliases", new AliasPermission("PizzaAlias", "Pizza")),

        // Manage Pizza backups
        new WeaviatePermission("manage_backups", new BackupsPermission("Pizza")),

        // Manage and read Pizza data
        new WeaviatePermission("manage_data", new DataPermission("Pizza")),
        new WeaviatePermission("read_data", new DataPermission("Pizza")),

        // Update and delete Songs data
        new WeaviatePermission("update_data", new DataPermission("Songs")),
        new WeaviatePermission("delete_data", new DataPermission("Songs")),

        // Read nodes with Pizza collection
        new WeaviatePermission("read_nodes", new NodesPermission("Pizza")),

        // Read nodes for any collection with verbosity="verbose"
        new WeaviatePermission("read_nodes", new NodesPermission(NodesPermission.Verbosity.VERBOSE)),

        // Read Reader role
        new WeaviatePermission("read_roles", new RolesPermission("Reader")),

        // Create and update CreatorUpdater role
        new WeaviatePermission("create_roles", new RolesPermission("CreatorUpdater", RolesPermission.Scope.ALL)),
        new WeaviatePermission("update_roles", new RolesPermission("CreatorUpdater", RolesPermission.Scope.ALL)),

        // Delete and update Pizza collection definition
        new WeaviatePermission("delete_collections", new CollectionsPermission("Pizza")),
        new WeaviatePermission("update_collections", new CollectionsPermission("Pizza")),

        // Read Songs collection definition
        new WeaviatePermission("read_collections", new CollectionsPermission("Songs")),

        // Read clusters
        new WeaviatePermission("read_cluster", new ClusterPermission()),

        // Create and update tenants
        new WeaviatePermission("create_tenants", new TenantsPermission()),
        new WeaviatePermission("update_tenants", new TenantsPermission()),

        // Read and delete users
        new WeaviatePermission("read_users", new UsersPermission()),
        new WeaviatePermission("assign_and_revoke_users", new UsersPermission()),
    };

    Permission<?>[] libraryPermissions = {
        new AliasPermission("PizzaAlias", "Pizza", AliasPermission.Action.CREATE, AliasPermission.Action.DELETE),
        new BackupsPermission("Pizza", BackupsPermission.Action.MANAGE),
        new DataPermission("Pizza", DataPermission.Action.MANAGE, DataPermission.Action.READ),
        new DataPermission("Songs", DataPermission.Action.UPDATE, DataPermission.Action.DELETE),
        new NodesPermission("Pizza", NodesPermission.Action.READ),
        new NodesPermission(NodesPermission.Verbosity.VERBOSE, NodesPermission.Action.READ),
        new RolesPermission("Reader", RolesPermission.Action.READ),
        new RolesPermission("CreatorUpdater", RolesPermission.Scope.ALL,
            RolesPermission.Action.CREATE, RolesPermission.Action.UPDATE),
        new CollectionsPermission("Pizza", CollectionsPermission.Action.DELETE, CollectionsPermission.Action.UPDATE),
        new CollectionsPermission("Songs", CollectionsPermission.Action.READ),
        new ClusterPermission(ClusterPermission.Action.READ),
        new TenantsPermission(TenantsPermission.Action.CREATE, TenantsPermission.Action.UPDATE),
        new UsersPermission(UsersPermission.Action.READ, UsersPermission.Action.ASSIGN_AND_REVOKE),
    };

    {
      WeaviateRole role = new WeaviateRole("TestRole",
          Arrays.asList(libraryPermissions));
      WeaviatePermission[] got = role.getPermissions().toArray(new WeaviatePermission[] {});
      assertArrayEquals(apiPermissions, got, "lib -> api conversion");
    }

    {
      WeaviateRole role = new WeaviateRole("TestRole", apiPermissions);
      Role libRole = role.toRole();
      Permission<?>[] got = libRole.permissions.toArray(new Permission<?>[] {});
      assertArrayEquals(libraryPermissions, got, "api -> lib conversion");
    }
  }
}
