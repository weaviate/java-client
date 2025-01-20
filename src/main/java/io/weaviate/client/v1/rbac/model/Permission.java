package io.weaviate.client.v1.rbac.model;

import io.weaviate.client.v1.rbac.api.WeaviatePermission;

public interface Permission<P extends Permission<P>> {
  WeaviatePermission toWeaviate();

  @SuppressWarnings("unchecked")
  static <P extends Permission<P>> P fromWeaviate(WeaviatePermission perm) {
    String action = perm.getAction();
    if (perm.getBackups() != null) {
      return (P) new BackupsPermission(action, perm.getBackups().getCollection());
    } else if (perm.getCollections() != null) {
      return (P) new CollectionsPermission(action, perm.getCollections().getCollection());
    } else if (perm.getData() != null) {
      return (P) new DataPermission(action, perm.getData().getCollection());
    } else if (perm.getNodes() != null) {
      NodesPermission out;
      NodesPermission nodes = perm.getNodes();
      if (nodes.getCollection() != null) {
        out = new NodesPermission(action, perm.getNodes().getVerbosity(), nodes.getCollection());
      } else {
        out = new NodesPermission(action, perm.getNodes().getVerbosity());
      }
      return (P) out;
    } else if (perm.getRoles() != null) {
      return (P) new RolesPermission(action, perm.getRoles().getRole());
    } else if (perm.getTenants() != null) {
      return (P) new TenantsPermission(action);
    } else if (CustomAction.isValid(ClusterPermission.Action.class, action)) {
      System.out.println("cluster:" + action);
      return (P) new ClusterPermission(action);
    } else if (CustomAction.isValid(UsersPermission.Action.class, action)) {
      return (P) new UsersPermission(action);
    }
    return null;
  };

  static BackupsPermission backups(BackupsPermission.Action action, String collection) {
    return new BackupsPermission(action, collection);
  }

  static ClusterPermission cluster(ClusterPermission.Action action) {
    return new ClusterPermission(action);
  }

  static CollectionsPermission collections(CollectionsPermission.Action action, String collection) {
    return new CollectionsPermission(action, collection);
  }

  static DataPermission data(DataPermission.Action action, String collection) {
    return new DataPermission(action, collection);
  }

  static NodesPermission nodes(NodesPermission.Action action, NodesPermission.Verbosity verbosity) {
    return new NodesPermission(action, verbosity);
  }

  static NodesPermission nodes(NodesPermission.Action action, NodesPermission.Verbosity verbosity, String collection) {
    return new NodesPermission(action, verbosity, collection);
  }

  static RolesPermission roles(RolesPermission.Action action, String role) {
    return new RolesPermission(action, role);
  }

  static TenantsPermission tenants(TenantsPermission.Action action) {
    return new TenantsPermission(action);
  }

  static UsersPermission users(UsersPermission.Action action) {
    return new UsersPermission(action);
  }
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
