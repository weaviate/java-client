package io.weaviate.client.v1.rbac.api;

import java.util.List;

import io.weaviate.client.v1.rbac.model.Permission;
import io.weaviate.client.v1.rbac.model.Role;
import lombok.Getter;

/** API model for serializing/deserializing roles. */
@Getter
public class WeaviateRole {
  String name;
  List<WeaviatePermission> permissions;

  public WeaviateRole(String name, List<Permission<?>> permissions) {
    this.name = name;
    this.permissions = WeaviatePermission.mergePermissions(permissions);
  }

  /** Create {@link Role} from the API response object. */
  public Role toRole() {
    List<Permission<?>> permissions = this.permissions.stream()
        .<Permission<?>>map(perm -> Permission.fromWeaviate(perm)).toList();
    return new Role(this.name, permissions);
  }
}
