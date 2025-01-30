package io.weaviate.client.v1.rbac.model;

import io.weaviate.client.v1.rbac.api.WeaviatePermission;
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
      return new RolesPermission(perm.getRoles().getRole(), action);
    } else if (perm.getTenants() != null) {
      return new TenantsPermission(action);
    } else if (RbacAction.isValid(ClusterPermission.Action.class, action)) {
      return new ClusterPermission(action);
    }
    return null;
  }

  public static BackupsPermission[] backups(BackupsPermission.Action action, String collection) {
    return new BackupsPermission[] { new BackupsPermission(collection, action) };
  }

  public static ClusterPermission[] cluster(ClusterPermission.Action action) {
    return new ClusterPermission[] { new ClusterPermission(action) };
  }

  public static CollectionsPermission[] collections(String collection, CollectionsPermission.Action action) {
    return new CollectionsPermission[] { new CollectionsPermission(collection, action) };
  }

  public static CollectionsPermission[] collections(String collection, CollectionsPermission.Action... actions) {
    CollectionsPermission[] permissions = new CollectionsPermission[actions.length];
    for (int i = 0; i < actions.length; i++) {
      permissions[i] = new CollectionsPermission(collection, actions[i]);
    }
    return permissions;
  }

  public static DataPermission[] data(String collection, DataPermission.Action action) {
    return new DataPermission[] { new DataPermission(collection, action) };
  }

  public static DataPermission[] data(String collection, DataPermission.Action... actions) {
    DataPermission[] permissions = new DataPermission[actions.length];
    for (int i = 0; i < actions.length; i++) {
      permissions[i] = new DataPermission(collection, actions[i]);
    }
    return permissions;
  }

  public static NodesPermission[] nodes(NodesPermission.Verbosity verbosity, NodesPermission.Action action) {
    return new NodesPermission[] { new NodesPermission(verbosity, action) };
  }

  public static NodesPermission[] nodes(String collection, NodesPermission.Verbosity verbosity,
      NodesPermission.Action action) {
    return new NodesPermission[] { new NodesPermission(collection, verbosity, action) };
  }

  public static RolesPermission[] roles(String role, RolesPermission.Action action) {
    return new RolesPermission[] { new RolesPermission(role, action) };
  }

  public static RolesPermission[] roles(String role, RolesPermission.Action... actions) {
    RolesPermission[] permissions = new RolesPermission[actions.length];
    for (int i = 0; i < actions.length; i++) {
      permissions[i] = new RolesPermission(role, actions[i]);
    }
    return permissions;
  }

  public static TenantsPermission[] tenants(TenantsPermission.Action action) {
    return new TenantsPermission[] { new TenantsPermission(action) };
  }

  public String toString() {
    return String.format("Permission<action=%s>", this.action);
  }
}
