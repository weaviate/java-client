package io.weaviate.client.v1.rbac.api;

import io.weaviate.client.v1.rbac.model.BackupsPermission;
import io.weaviate.client.v1.rbac.model.ClusterPermission;
import io.weaviate.client.v1.rbac.model.CollectionsPermission;
import io.weaviate.client.v1.rbac.model.DataPermission;
import io.weaviate.client.v1.rbac.model.NodesPermission;
import io.weaviate.client.v1.rbac.model.Permission;
import io.weaviate.client.v1.rbac.model.RolesPermission;
import io.weaviate.client.v1.rbac.model.TenantsPermission;
import io.weaviate.client.v1.rbac.model.UsersPermission;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class WeaviatePermission {
  String action;
  BackupsPermission backups;
  ClusterPermission cluster;
  CollectionsPermission collections;
  DataPermission data;
  NodesPermission nodes;
  RolesPermission roles;
  TenantsPermission tenants;
  UsersPermission users;

  public WeaviatePermission(String action) {
    this.action = action;
  }

  public <P extends Permission<P>> WeaviatePermission(String action, Permission<P> perm) {
    this.action = action;
    if (perm instanceof BackupsPermission) {
      this.backups = (BackupsPermission) perm;
    } else if (perm instanceof ClusterPermission) {
      this.cluster = (ClusterPermission) perm;
    } else if (perm instanceof CollectionsPermission) {
      this.collections = (CollectionsPermission) perm;
    } else if (perm instanceof DataPermission) {
      this.data = (DataPermission) perm;
    } else if (perm instanceof NodesPermission) {
      this.nodes = (NodesPermission) perm;
    } else if (perm instanceof RolesPermission) {
      this.roles = (RolesPermission) perm;
    } else if (perm instanceof TenantsPermission) {
      this.tenants = (TenantsPermission) perm;
    } else if (perm instanceof UsersPermission) {
      this.users = (UsersPermission) perm;
    }
  }
}
