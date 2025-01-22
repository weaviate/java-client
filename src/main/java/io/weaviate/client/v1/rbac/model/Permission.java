package io.weaviate.client.v1.rbac.model;

import io.weaviate.client.v1.rbac.api.WeaviatePermission;
import lombok.Getter;

public abstract class Permission<P extends Permission<P>> {
  @Getter
  final transient String action;

  Permission(CustomAction action) {
    this.action = action.getValue();
  }

  public abstract WeaviatePermission toWeaviate();

  public static Permission<?> fromWeaviate(WeaviatePermission perm) {
    String action = perm.getAction();
    if (perm.getBackups() != null) {
      return new BackupsPermission(action, perm.getBackups().getCollection());
    } else if (perm.getCollections() != null) {
      return new CollectionsPermission(action, perm.getCollections().getCollection());
    } else if (perm.getData() != null) {
      return new DataPermission(action, perm.getData().getCollection());
    } else if (perm.getNodes() != null) {
      NodesPermission nodes = perm.getNodes();
      if (nodes.getCollection() != null) {
        return new NodesPermission(action, perm.getNodes().getVerbosity(), nodes.getCollection());
      }
      return new NodesPermission(action, perm.getNodes().getVerbosity());
    } else if (perm.getRoles() != null) {
      return new RolesPermission(action, perm.getRoles().getRole());
    } else if (perm.getTenants() != null) {
      return new TenantsPermission(action);
    } else if (CustomAction.isValid(ClusterPermission.Action.class, action)) {
      return new ClusterPermission(action);
    } else if (CustomAction.isValid(UsersPermission.Action.class, action)) {
      return new UsersPermission(action);
    }
    return null;
  };

  public static BackupsPermission backups(BackupsPermission.Action action, String collection) {
    return new BackupsPermission(action, collection);
  }

  public static ClusterPermission cluster(ClusterPermission.Action action) {
    return new ClusterPermission(action);
  }

  public static CollectionsPermission collections(CollectionsPermission.Action action, String collection) {
    return new CollectionsPermission(action, collection);
  }

  public static DataPermission data(DataPermission.Action action, String collection) {
    return new DataPermission(action, collection);
  }

  public static NodesPermission nodes(NodesPermission.Action action, NodesPermission.Verbosity verbosity) {
    return new NodesPermission(action, verbosity);
  }

  public static NodesPermission nodes(NodesPermission.Action action, NodesPermission.Verbosity verbosity,
      String collection) {
    return new NodesPermission(action, verbosity, collection);
  }

  public static RolesPermission roles(RolesPermission.Action action, String role) {
    return new RolesPermission(action, role);
  }

  public static TenantsPermission tenants(TenantsPermission.Action action) {
    return new TenantsPermission(action);
  }

  // public static UsersPermission users(UsersPermission.Action action) {
  // return new UsersPermission(action);
  // }
}

interface CustomAction {
  String getValue();

  static <E extends Enum<E> & CustomAction> E fromString(Class<E> enumClass, String value) {
    for (E action : enumClass.getEnumConstants()) {
      if (action.getValue().equals(value)) {
        return action;
      }
    }
    throw new IllegalArgumentException("No enum constant for value: " + value);
  }

  static <A extends CustomAction> boolean isValid(Class<A> enumClass, String value) {
    for (CustomAction action : enumClass.getEnumConstants()) {
      if (action.getValue().equals(value)) {
        return true;
      }
    }
    return false;
  }
}
