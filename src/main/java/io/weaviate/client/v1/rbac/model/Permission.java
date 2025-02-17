package io.weaviate.client.v1.rbac.model;

import io.weaviate.client.v1.rbac.api.WeaviatePermission;
import io.weaviate.client.v1.rbac.model.NodesPermission.Verbosity;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@EqualsAndHashCode
public abstract class Permission<P extends Permission<P>> {
  @Getter
  final transient String action;

  Permission(RbacAction action) {
    this.action = action.getValue();
  }

  /** Convert the permission to {@link WeaviatePermission}. */
  public abstract WeaviatePermission toWeaviate();

  /**
   * Convert {@link WeaviatePermission} to concrete {@link Permission}.
   */
  public static Permission<?> fromWeaviate(WeaviatePermission perm) {
    String action = perm.getAction();
    if (perm.getBackups() != null) {
      return new BackupsPermission(perm.getBackups().getCollection(), action);
    } else if (perm.getCollections() != null) {
      return new CollectionsPermission(perm.getCollections().getCollection(), action);
    } else if (perm.getData() != null) {
      return new DataPermission(perm.getData().getCollection(), action);
    } else if (perm.getNodes() != null) {
      NodesPermission nodes = perm.getNodes();
      if (nodes.getCollection() != null) {
        return new NodesPermission(nodes.getCollection(), nodes.getVerbosity(), action);
      }
      return new NodesPermission(nodes.getVerbosity(), action);
    } else if (perm.getRoles() != null) {
      RolesPermission roles = perm.getRoles();
      return new RolesPermission(roles.getRole(), roles.getScope(), action);
    } else if (perm.getTenants() != null) {
      return new TenantsPermission(action);
    } else if (RbacAction.isValid(ClusterPermission.Action.class, action)) {
      return new ClusterPermission(action);
    } else if (RbacAction.isValid(UsersPermission.Action.class, action)) {
      return new UsersPermission(action);
    }
    return null;
  }

  /**
   * Create {@link BackupsPermission} for a collection.
   * <p>
   * Example:
   * {@code Permission.backups(BackupsPermission.Action.MANAGE, "Pizza") }
   */
  public static BackupsPermission[] backups(BackupsPermission.Action action, String collection) {
    return new BackupsPermission[] { new BackupsPermission(collection, action) };
  }

  /**
   * Create {@link ClusterPermission} permission.
   * <p>
   * Example: {@code Permission.cluster(ClusterPermission.Action.READ, "Pizza") }
   */
  public static ClusterPermission[] cluster(ClusterPermission.Action action) {
    return new ClusterPermission[] { new ClusterPermission(action) };
  }

  /**
   * Create permission for managing collection's configuration.
   * <p>
   * Example:
   * {@code Permission.collections("Pizza", CollectionsPermission.Action.READ) }
   */
  public static CollectionsPermission[] collections(String collection, CollectionsPermission.Action action) {
    return new CollectionsPermission[] { new CollectionsPermission(collection, action) };
  }

  /**
   * Create permission for collection's configuration.
   * <p>
   * Example:
   * {@code Permission.collections("Pizza", CollectionsPermission.Action.READ, CollectionsPermission.Action.UPDATE) }
   */
  public static CollectionsPermission[] collections(String collection, CollectionsPermission.Action... actions) {
    CollectionsPermission[] permissions = new CollectionsPermission[actions.length];
    for (int i = 0; i < actions.length; i++) {
      permissions[i] = new CollectionsPermission(collection, actions[i]);
    }
    return permissions;
  }

  /**
   * Create permissions for multiple actions for managing collection's data.
   * <p>
   * Example:
   * {@code Permission.data("Pizza", DataPermission.Action.READ, DataPermission.Action.UPDATE) }
   */
  public static DataPermission[] data(String collection, DataPermission.Action action) {
    return new DataPermission[] { new DataPermission(collection, action) };
  }

  /**
   * Create permissions for multiple actions for managing collection's
   * data.
   * <p>
   * Example:
   * {@code Permission.data("Pizza", DataPermission.Action.READ, DataPermission.Action.UPDATE) }
   */
  public static DataPermission[] data(String collection, DataPermission.Action... actions) {
    DataPermission[] permissions = new DataPermission[actions.length];
    for (int i = 0; i < actions.length; i++) {
      permissions[i] = new DataPermission(collection, actions[i]);
    }
    return permissions;
  }

  /**
   * Create {@link NodesPermission} scoped to all collections.
   * <p>
   * Example:
   * {@code Permission.nodes(NodesPermission.Verbosity.MINIMAL, NodesPermission.Action.READ) }
   */
  public static NodesPermission[] nodes(NodesPermission.Verbosity verbosity, NodesPermission.Action action) {
    return new NodesPermission[] { new NodesPermission(verbosity, action) };
  }

  /**
   * Create {@link NodesPermission} scoped to a specific collection. Verbosity is
   * set to {@link Verbosity#VERBOSE} by default.
   * <p>
   * Example:
   * {@code Permission.nodes("Pizza", NodesPermission.Action.READ) }
   */
  public static NodesPermission[] nodes(String collection, NodesPermission.Action action) {
    return new NodesPermission[] { new NodesPermission(collection, action) };
  }

  /**
   * Create {@link RolesPermission} for a role.
   * <p>
   * Example:
   * {@code Permission.roles("MyRole", RolesPermission.Action.READ) }
   */
  public static RolesPermission[] roles(String role, RolesPermission.Action action) {
    return new RolesPermission[] { new RolesPermission(role, action) };
  }

  /**
   * Create {@link RolesPermission} for multiple actions.
   * <p>
   * Example:
   * {@code Permission.roles("MyRole", RolesPermission.Action.READ, RolesPermission.Action.UPDATE) }
   */
  public static RolesPermission[] roles(String role, RolesPermission.Action... actions) {
    RolesPermission[] permissions = new RolesPermission[actions.length];
    for (int i = 0; i < actions.length; i++) {
      permissions[i] = new RolesPermission(role, actions[i]);
    }
    return permissions;
  }

  /**
   * Create {@link TenantsPermission} for a tenant.
   * <p>
   * Example:
   * {@code Permission.tenants(TenantsPermission.Action.READ) }
   */
  public static TenantsPermission[] tenants(TenantsPermission.Action action) {
    return new TenantsPermission[] { new TenantsPermission(action) };
  }

  /**
   * Create {@link UsersPermission}.
   * <p>
   * Example:
   * {@code Permission.users(UsersPermission.Action.READ) }
   */
  public static UsersPermission[] users(UsersPermission.Action action) {
    return new UsersPermission[] { new UsersPermission(action) };
  }

  public String toString() {
    return String.format("Permission<action=%s>", this.action);
  }
}
