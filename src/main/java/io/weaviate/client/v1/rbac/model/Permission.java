package io.weaviate.client.v1.rbac.model;

import io.weaviate.client.v1.rbac.api.WeaviatePermission;

public interface Permission<P extends Permission<P>> {
  WeaviatePermission toWeaviate();

  P fromWeaviate(WeaviatePermission perm);

  static ClusterPermission backups(ClusterPermission.Action action) {
    return new ClusterPermission(action);
  }

  static BackupsPermission backups(BackupsPermission.Action action, String collection) {
    return new BackupsPermission(action, collection);
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
